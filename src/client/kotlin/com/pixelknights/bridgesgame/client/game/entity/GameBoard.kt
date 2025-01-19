package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.di.Channels
import com.pixelknights.bridgesgame.client.game.entity.scanner.BridgeScanner
import com.pixelknights.bridgesgame.client.game.entity.scanner.TowerScanner
import com.pixelknights.bridgesgame.client.render.*
import com.pixelknights.bridgesgame.client.util.plus
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import org.apache.logging.log4j.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.concurrent.BlockingQueue
import kotlin.getValue

class GameBoard(
    private val towerScanner: TowerScanner,
    private val bridgeScanner: BridgeScanner,
    private val dotRenderer: DotRenderer,
    private val lineRenderer: LineRenderer,
    private val config: ModConfig,
    private val mc: MinecraftClient,
    private val logger: Logger,
    private val textRenderer: HoveringTextRenderer,
) : KoinComponent {

    private var towers: MutableList<MutableList<Tower>> = mutableListOf<MutableList<Tower>>()
    private var bridges: MutableSet<Bridge> = mutableSetOf()
    private val paths: MutableList<Path> = mutableListOf()
    private val errorChannel: BlockingQueue<String> by inject<BlockingQueue<String>>(named(Channels.MultipleBridgeDetectedErrorChannel))

    val teams: MutableMap<GameColor, Team> = mutableMapOf()


    fun scanGame(centerCoordinate: BlockPos) {
        resetGame()

        teams += GameColor.entries.associate { color ->
            color to Team().apply { baseColor = color }
        }

        towers = towerScanner.getTowers(centerCoordinate)

//        val centerTower = towers[9][9]
//        val position = centerTower.worldCoordinates(centerCoordinate, config) + Vec3i(1, 100, 1)
//        println("position = $position")
//        textRenderer.textToRender += HoveringText(position)
//            .addLine("§nCenter Tower§r", Color.BLUE)
//            .addLine("Another Line", Color.RED)
//            .addLine("Another Line Again", Color.GREEN)

        // Get a mapping from floorNum -> every node on that floor
        val nodeMap = towers
            .asSequence()
            .flatten()
            .flatMap { it.floors }
            .flatMap { it.nodes }
            .toList()


        nodeMap.forEach { node ->
            bridges += bridgeScanner.getBridgesForNode(node, nodeMap)
        }

        connectBridges()
        calculateScores()
        createDebugLines()
        createTowerStatsText(towers.flatten().toList(), centerCoordinate)
    }

    fun resetGame() {
        bridges.clear()
        paths.clear()
        teams.clear()
        towers.clear()
        lineRenderer.linesToRender.clear()
        dotRenderer.dotsToRender.clear()
        textRenderer.textToRender.clear()
    }

    fun createDebugLines() {
        // TODO: Consider moving this to a separate class

        paths.forEach { path ->
            val lines = path.createDebugLines(mc.world!!, config)
            lineRenderer.linesToRender += lines
            dotRenderer.dotsToRender += lines.flatMap { it.dots }
        }

    }

    private fun connectBridges() {
        bridges.forEach { bridge ->
            val startNode = bridge.startNode
            val endNode = bridge.endNode

            startNode.connectedBridges += bridge
            endNode?.connectedBridges += bridge

            // Draw debug lines/dots
//            if (endNode != null) {
//                val line = DebugLine(startNode.worldCoords, endNode.worldCoords, Color.fromHex(bridge.owner?.rgba ?: 0))
//                lineRenderer.linesToRender += line
//                dotRenderer.dotsToRender += line.dots
//            } else {
//                dotRenderer.dotsToRender += DebugDot(startNode.worldCoords, Color.WHITE, 0f)
//            }
        }
    }

    private fun calculateScores() {
        logger.info("Validating game...")

        val world = mc.world
        if (world == null) {
            logger.error("World is null")
            return
        }

        // TODO: generate paths for disconnected bridge networks
        buildTeamPaths()
        validateTowerCaptures(world)

        // Calculate scores
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
            val numBridgeClaims = bridges.filter { BridgeError.BRIDGE_TO_CLOSED_NODE !in it.errors }.count { it.owner == teamColor }
            val numBridgePaints = bridges.filter { BridgeError.BRIDGE_TO_CLOSED_NODE !in it.errors }.count { it.painter == teamColor }
            val floorClaims = towers.flatten().flatMap { it.floors }.count { it.captureColor == teamColor }
            val floorPaints = towers.flatten().flatMap { it.floors }.count { it.paintColor == teamColor }
            val towerClaims = towers.flatten().count { it.getAttemptedClaimingTeam(world, config) == teamColor }
            // Scrapes should be added here, but the mod has no "memory" of previous days so this is not possible to count.

            team.moves = numBridgeClaims + numBridgePaints + floorClaims + floorPaints + towerClaims
        }

        logger.info("Teams = $teams")

        // report floor errors
        towers
            .asSequence()
            .flatten()
            .flatMap { it.floors }
            .filter { (it.isCaptured && it.isCaptureValidated != true) || (it.isPainted && it.isPaintValidated != true) }
            .forEach {
                if(it.isCaptured && it.isCaptureValidated != true) {
                    errorChannel += "Floor ${it.coords} ${it.worldCoords} disconnected from ${it.captureColor} network"
                }
                if(it.isPainted && it.isPaintValidated != true) {
                    errorChannel += "Floor ${it.coords} ${it.worldCoords} disconnected from ${it.paintColor} network"
                }
            }

        //TODO: report bridge errors
    }

    /**
     * Validate tower captures.
     * Must be called AFTER paths are generated
     */
    private fun validateTowerCaptures(world: ClientWorld) {
        towers.flatten().forEach { tower ->
            tower.setCapturingTeam(world, config)
        }
    }

    private fun createTowerStatsText(towerList: List<Tower>, centerCoordinate: BlockPos) {
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
                val pointValue = tower.getCapturePoints(tower.capturingTeam!!)
                textBlock.addLine("Tower captured by: ${tower.capturingTeam} (+${pointValue})", color)
            }

            textBlock.addLine("§nFloors:", Color.WHITE)
            tower.floors
                .groupBy { it.owner }.forEach { team, groupFloors ->
                    val floorNumbers = groupFloors.map { it.floorNumber + 1 }
                    if (team == null) {
                        textBlock.addLine("Uncaptured floors: $floorNumbers", Color.WHITE)
                    } else {
                        textBlock.addLine("Team $team captured: $floorNumbers", Color.fromHex(team.rgba))
                    }
                }
            val unvalidatedFloors = tower.floors.filter { it.owner != null && !it.isOwnerValidated }.toList()
            if (!unvalidatedFloors.isEmpty()) {
                textBlock.addLine("⚠ Disconnected Floors: ${unvalidatedFloors.map { it.floorNumber + 1 }.toList()} ⚠", Color.WHITE)
            }


            textRenderer.textToRender.add(textBlock)
        }

    }

    /**
     * Generate paths starting from the base tower for each team
     */
    private fun buildTeamPaths() {
        paths += GameColor.entries
            .filter { it.isTeam }
            .map { team -> Path(team) }
            .toList()

        val allTowers = towers.asSequence().flatten()

        paths.forEach { path ->

            val baseFloor = allTowers
                .filter { tower -> tower.color == path.pathOwner && tower.isBase }
                .flatMap { tower -> tower.floors }
                .first { floor -> floor.isBase }

            path.buildPath(baseFloor, allTowers.toList(), errorChannel)
        }
    }

}

