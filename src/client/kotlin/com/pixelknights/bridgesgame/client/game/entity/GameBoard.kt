package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.config.TowerLayoutConfig
import com.pixelknights.bridgesgame.client.game.entity.scanner.BridgeScanner
import com.pixelknights.bridgesgame.client.game.entity.scanner.CircuitScanner
import com.pixelknights.bridgesgame.client.game.entity.scanner.LadderScanner
import com.pixelknights.bridgesgame.client.game.entity.scanner.TowerScanner
import com.pixelknights.bridgesgame.client.render.*
import com.pixelknights.bridgesgame.client.util.plus
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import org.apache.logging.log4j.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GameBoard(
    private val towerScanner: TowerScanner,
    private val bridgeScanner: BridgeScanner,
    private val ladderScanner: LadderScanner,
    private val circuitScanner: CircuitScanner,
    private val dotRenderer: DotRenderer,
    private val lineRenderer: LineRenderer,
    private val config: ModConfig,
    private val mc: MinecraftClient,
    private val logger: Logger,
    private val textRenderer: HoveringTextRenderer,
    private val warningIconRenderer: WarningIconRenderer,
) : KoinComponent {

    private val layoutConfig: TowerLayoutConfig by inject()
    private val towerScoring: TowerScoring by lazy { TowerScoring(layoutConfig.getBasePositions()) }

    private var towers: MutableList<MutableList<Tower>> = mutableListOf()
    private var bridges: MutableSet<Bridge> = mutableSetOf()
    private var ladders: MutableSet<Ladder> = mutableSetOf()
    private var circuits: MutableSet<Circuit> = mutableSetOf()
    private val paths: MutableList<Path> = mutableListOf()
    val teams: MutableMap<GameColor, Team> = mutableMapOf()

    /**
     * Scans the game board and returns all computed state as a [ScanResult].
     * Safe to call from a background thread — only reads world state, writes nothing to GameBoard
     * or renderer fields. Call [applyScanResult] on the client thread to commit the result.
     */
    fun scanGame(centerCoordinate: BlockPos): ScanResult {
        val localTeams: MutableMap<GameColor, Team> = GameColor.entries.associate { color ->
            color to Team().apply { baseColor = color }
        }.toMutableMap()

        val localTowers = towerScanner.getTowers(centerCoordinate)

        val nodeMap = localTowers
            .asSequence()
            .flatten()
            .flatMap { it.floors }
            .flatMap { it.nodes }
            .toList()

        val localBridges = mutableSetOf<Bridge>()
        nodeMap.forEach { node ->
            localBridges += bridgeScanner.getBridgesForNode(node, nodeMap)
        }

        val localLadders = mutableSetOf<Ladder>()
        localLadders += localTowers.flatten().flatMap { ladderScanner.getLaddersForTower(it) }

        val localCircuits = mutableSetOf<Circuit>()
        nodeMap.forEach { node ->
            localCircuits += circuitScanner.getCircuitsForNode(node, nodeMap)
        }

        val allConnections: Set<Connection> = localBridges + localLadders + localCircuits
        allConnections.forEach { connection ->
            connection.nodeA.connections += connection
            connection.nodeB?.connections += connection
        }

        val localPaths = mutableListOf<Path>()
        val localWarnings = mutableListOf<GameWarning>()

        calculateScores(
            towers = localTowers,
            bridges = localBridges,
            connections = allConnections,
            paths = localPaths,
            teams = localTeams,
            warnings = localWarnings,
        )

        val localLinesToRender = mutableListOf<RenderedLine>()
        val localDotsToRender = mutableListOf<RenderedDot>()
        mc.world?.let { world ->
            localPaths.forEach { path ->
                val lines = path.createRenderedLines(world, config)
                localLinesToRender += lines
                localDotsToRender += lines.flatMap { it.dots }
            }
        }

        val localTextsToRender = buildTowerStatsText(localTowers.flatten().toList(), centerCoordinate)

        val numberedWarnings = localWarnings.mapIndexed { i, w -> w.copy(id = i + 1) }

        return ScanResult(
            towers = localTowers,
            bridges = localBridges,
            ladders = localLadders,
            circuits = localCircuits,
            paths = localPaths,
            teams = localTeams,
            linesToRender = localLinesToRender,
            dotsToRender = localDotsToRender,
            textsToRender = localTextsToRender,
            warnings = numberedWarnings,
        )
    }

    /**
     * Applies a completed [ScanResult] to GameBoard state and all renderer collections.
     * Must be called on the Minecraft client thread.
     */
    fun applyScanResult(result: ScanResult) {
        towers = result.towers
        bridges = result.bridges
        ladders = result.ladders
        circuits = result.circuits
        paths.clear()
        paths += result.paths
        teams.clear()
        teams += result.teams

        lineRenderer.linesToRender.clear()
        lineRenderer.linesToRender += result.linesToRender
        dotRenderer.dotsToRender.clear()
        dotRenderer.dotsToRender += result.dotsToRender
        textRenderer.textToRender.clear()
        textRenderer.textToRender += result.textsToRender
        warningIconRenderer.warnings.clear()
        warningIconRenderer.warnings += result.warnings
    }

    fun resetGame() {
        bridges.clear()
        ladders.clear()
        circuits.clear()
        paths.clear()
        teams.clear()
        towers.clear()
        lineRenderer.linesToRender.clear()
        dotRenderer.dotsToRender.clear()
        textRenderer.textToRender.clear()
        warningIconRenderer.warnings.clear()
    }

    private fun calculateScores(
        towers: List<List<Tower>>,
        bridges: Set<Bridge>,
        connections: Set<Connection>,
        paths: MutableList<Path>,
        teams: MutableMap<GameColor, Team>,
        warnings: MutableList<GameWarning>,
    ) {
        logger.info("Validating game...")

        val world = mc.world
        if (world == null) {
            logger.error("World is null")
            return
        }

        buildTeamPaths(towers, paths)

        towers.flatten().forEach { tower ->
            tower.setCapturingTeam(world, config)
        }

        paths.forEach { path ->
            if (path.pathOwner != null) {
                val numCapturedTowers = towers
                    .asSequence()
                    .flatten()
                    .count { it.capturingTeam == path.pathOwner }

                teams[path.pathOwner]?.capturedTowers = numCapturedTowers
                teams[path.pathOwner]?.points = path.calculateScore()
            }
        }

        // Calculate moves
        teams.forEach { (teamColor, team) ->
            val numBridgeClaims = bridges.filter { ConnectionError.BRIDGE_TO_CLOSED_NODE !in it.errors }.count { it.owner == teamColor }
            val numBridgePaints = bridges.filter { ConnectionError.BRIDGE_TO_CLOSED_NODE !in it.errors }.count { it.painter == teamColor }
            val floorClaims = towers.flatten().flatMap { it.floors }.count { it.captureColor == teamColor }
            val floorPaints = towers.flatten().flatMap { it.floors }.count { it.paintColor == teamColor }
            val floorBlocks = towers.flatten().flatMap { it.floors }.count { it.blockingTeamColor == teamColor }
            val towerClaims = towers.flatten().count { it.getAttemptedClaimingTeam(world, config) == teamColor }
            val nodeBreaks = towers.flatten().flatMap { it.floors }.flatMap { it.nodes }.count { it.brokenByTeam == teamColor }
            // Scrapes should be added here, but the mod has no "memory" of previous days so this is not possible to count.

            team.brokenNodes = nodeBreaks
            team.moves = numBridgeClaims + numBridgePaints + floorClaims + floorPaints + floorBlocks + towerClaims + nodeBreaks
        }

        logger.info("Teams = $teams")

        // report floor errors
        towers
            .asSequence()
            .flatten()
            .flatMap { it.floors }
            .filter { (it.isCaptured && it.isCaptureValidated != true) || (it.isPainted && it.isPaintValidated != true) }
            .forEach {
                if (it.isCaptured && it.isCaptureValidated != true) {
                    warnings += GameWarning(
                        position = it.worldCenter,
                        color = Color.fromHex(it.captureColor!!.rgba),
                        message = "Floor ${it.coords} ${it.worldCoords} disconnected from ${it.captureColor} network",
                    )
                }
                if (it.isPainted && it.isPaintValidated != true) {
                    warnings += GameWarning(
                        position = it.worldCenter,
                        color = Color.fromHex(it.paintColor!!.rgba),
                        message = "Floor ${it.coords} ${it.worldCoords} disconnected from ${it.paintColor} network",
                    )
                }
            }

        // Report floors blocked by a team that does not own them
        towers
            .asSequence()
            .flatten()
            .flatMap { it.floors }
            .filter { it.isBlocked && it.blockingTeamColor != it.owner }
            .forEach {
                warnings += GameWarning(
                    position = it.worldCenter,
                    color = Color.fromHex(it.blockingTeamColor!!.rgba),
                    message = "Floor ${it.coords} ${it.worldCoords} blocked by ${it.blockingTeamColor} but owned by ${it.owner ?: "no team"}",
                )
            }

        // Report nodes broken by a team that does not own the floor
        towers
            .asSequence()
            .flatten()
            .flatMap { it.floors }
            .flatMap { it.nodes }
            .filter { it.brokenByTeam != null && it.brokenByTeam != it.floor.owner }
            .forEach {
                warnings += GameWarning(
                    position = it.worldPosition,
                    color = Color.fromHex(it.brokenByTeam!!.rgba),
                    message = "Node ${it.side} on floor ${it.floor.coords} broken by ${it.brokenByTeam} but floor owned by ${it.floor.owner ?: "no team"}",
                )
            }

        // Report bridges that connect to/from a closed node
        bridges
            .filter { ConnectionError.BRIDGE_TO_CLOSED_NODE in it.errors }
            .distinctBy { it.segments }
            .forEach { bridge ->
                val team = bridge.owner ?: bridge.painter ?: return@forEach
                warnings += GameWarning(
                    position = bridge.midpoint,
                    color = Color.fromHex(team.rgba),
                    message = "Bridge ${bridge.nodeA.coords} to ${bridge.nodeB?.coords ?: "?"} ($team) connects to a closed node",
                )
            }

        // Report bridges that belong to a team but are not reachable from their home base
        paths.forEach { path ->
            val team = path.pathOwner ?: return@forEach
            bridges
                .filter { ConnectionError.BRIDGE_TO_CLOSED_NODE !in it.errors }
                .filter { it.owner == team || it.painter == team }
                .filter { it !in path.connections }
                .forEach { bridge ->
                    warnings += GameWarning(
                        position = bridge.midpoint,
                        color = Color.fromHex(team.rgba),
                        message = "Bridge ${bridge.nodeA.coords} to ${bridge.nodeB?.coords ?: "?"} ($team) is not connected to $team's home base",
                    )
                }
        }

        // Report connections that physically cross each other
        ConnectionValidator.findIntersections(connections).forEach { (a, b, point) ->
            warnings += GameWarning(
                position = warningPosition(point, a, b),
                color = Color.WHITE,
                message = "Connection ${a.nodeA.coords}-${a.nodeB?.coords ?: "?"} intersects ${b.nodeA.coords}-${b.nodeB?.coords ?: "?"}",
            )
        }

        // Report nodes with more than one connection (excluding ladder-only nodes)
        ConnectionValidator.findOverloadedNodes(connections).forEach { node ->
            warnings += GameWarning(
                position = node.worldPosition,
                color = Color.WHITE,
                message = "Node ${node.coords} has multiple connections attached",
            )
        }
    }

    private fun buildTeamPaths(
        towers: List<List<Tower>>,
        paths: MutableList<Path>,
    ) {
        paths += GameColor.entries
            .filter { it.isTeam }
            .map { team -> Path(team, towerScoring) }
            .toList()

        val allTowers = towers.asSequence().flatten()

        paths.forEach { path ->
            val baseFloor = allTowers
                .filter { tower -> tower.color == path.pathOwner && tower.isBase }
                .flatMap { tower -> tower.floors }
                .first { floor -> floor.isBase }

            path.buildPath(baseFloor, allTowers.toList())
        }
    }

    private fun buildTowerStatsText(towerList: List<Tower>, centerCoordinate: BlockPos): List<HoveringText> {
        val result = mutableListOf<HoveringText>()
        for (tower in towerList) {
            if (tower.capturingTeam == null && tower.floors.all { it.captureColor == null }) {
                continue
            }

            val topFloor = tower.floors.maxBy { it.floorNumber }
            val textHeight = (topFloor.floorNumber + 1) * config.towerConfig.blocksBetweenFloors + 7
            val position = tower.worldCoordinates(centerCoordinate, config) + Vec3i(1, textHeight, 1)

            val textBlock = HoveringText(position)
            if (tower.capturingTeam == null) {
                textBlock.addLine("Tower not captured", Color.WHITE)
            } else {
                val color = Color.fromHex(tower.capturingTeam!!.rgba)
                val pointValue = towerScoring.getCapturePoints(tower.capturingTeam!!, tower.color)
                textBlock.addLine("Tower captured by: ${tower.capturingTeam} (+${pointValue})", color)
            }

            tower.floors
                .groupBy { it.owner }
                .forEach { team, groupFloors ->
                    val floorNumbers = groupFloors.map { it.floorNumber + 1 }
                    if (team == null) {
                        textBlock.addLine("Uncaptured floors: $floorNumbers", Color.WHITE)
                    } else {
                        val blockedNumbers = groupFloors.filter { it.blockingTeamColor == team }.map { it.floorNumber + 1 }
                        val blockedSuffix = if (blockedNumbers.isNotEmpty()) " and blocked $blockedNumbers" else ""
                        textBlock.addLine("Team $team captured $floorNumbers$blockedSuffix", Color.fromHex(team.rgba))
                    }
                }

            val unvalidatedFloors = tower.floors.filter { it.owner != null && !it.isOwnerValidated }.toList()
            if (unvalidatedFloors.isNotEmpty()) {
                textBlock.addLine("⚠ Disconnected Floors: ${unvalidatedFloors.map { it.floorNumber + 1 }.toList()} ⚠", Color.WHITE)
            }

            result += textBlock
        }
        return result
    }

    // Circuit segments run through solid blocks, so shift the icon up one block to stay visible.
    private fun warningPosition(pos: BlockPos, vararg connections: Connection): BlockPos {
        return if (connections.any { it is Circuit }) pos.up() else pos
    }

}
