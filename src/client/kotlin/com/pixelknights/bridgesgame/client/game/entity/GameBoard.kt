package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.game.entity.scanner.BridgeScanner
import com.pixelknights.bridgesgame.client.game.entity.scanner.TowerScanner
import com.pixelknights.bridgesgame.client.render.*
import net.minecraft.block.LadderBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import org.koin.core.component.KoinComponent
import com.pixelknights.bridgesgame.client.util.plus
import net.minecraft.client.world.ClientWorld
import org.apache.logging.log4j.Logger

class GameBoard(
    private val towerScanner: TowerScanner,
    private val bridgeScanner: BridgeScanner,
    private val dotRenderer: DotRenderer,
    private val lineRenderer: LineRenderer,
    private val config: ModConfig,
    private val mc: MinecraftClient,
    private val logger: Logger
) : KoinComponent {

    private var towers: List<List<Tower>> = mutableListOf<MutableList<Tower>>()
    private var bridges: MutableSet<Bridge> = mutableSetOf()
    private val paths: MutableList<Path> = mutableListOf()
    private val teams: MutableMap<GameColor, Team> = mutableMapOf()

    fun scanGame(centerCoordinate: BlockPos) {
        bridges.clear()
        paths.clear()
        teams.clear()
        lineRenderer.linesToRender.clear()
        dotRenderer.dotsToRender.clear()

        teams += GameColor.entries.associate { color ->
            color to Team().apply { baseColor = color }
        }

        towers = towerScanner.getTowers(centerCoordinate)

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
        println("Bridges! Found ${bridges.size} bridges!")
    }

    fun createDebugLines() {
        // TODO: Consider moving this to a separate class

        paths.forEach { path ->
            val lines = path.createDebugLines()
            lineRenderer.linesToRender += lines
            dotRenderer.dotsToRender += lines.flatMap { it.dots }
        }

        val ladderFloors = towers
            .asSequence()
            .flatten()
            .flatMap { it.floors }
            .filter { it.hasLadder }
            .toList()

        ladderFloors.forEach { floor ->
            // offset the coords by 1 block in the direction the ladder is facing so the line doesn't get hidden
            // inside of the beacon beam
            val ladderBlock = mc.world?.getBlockState(floor.worldCenter)
            val facing = ladderBlock?.get(LadderBlock.FACING) ?: Direction.NORTH
            val startPos = floor.worldCenter + facing.vector
            val endPath = startPos.up(config.towerConfig.blocksBetweenFloors)

            val line = DebugLine(startPos, endPath, Color.BLACK, noise = 0f)
            lineRenderer.linesToRender += line
            dotRenderer.dotsToRender += line.dots

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

        logger.info("Teams = $teams")
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

    /**
     * Generate paths starting from the base tower for each team
     */
    private fun buildTeamPaths() {
        paths += GameColor.entries
            .filter { it.isTeam }
            .map { team -> Path(team) }
            .toList()

        paths.forEach { path ->
            val allTowers = towers.asSequence().flatten()

            val baseFloor = allTowers
                .filter { tower -> tower.color == path.pathOwner && tower.isBase }
                .flatMap { tower -> tower.floors }
                .first { floor -> floor.floorNumber == 0 }


            path.buildPath(baseFloor, allTowers.toList())
        }
    }

}

