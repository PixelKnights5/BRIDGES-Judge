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
    private var bridges = mutableSetOf<Bridge>()

    fun scanGame(centerCoordinate: BlockPos) {
        bridges.clear()
        towers = towerScanner.getTowers(centerCoordinate)

        // Get a mapping from floorNum -> every node on that floor
        val nodeMap = towers
            .asSequence()
            .flatten()
            .map { it.floors }
            .flatten()
            .map { it.nodes }
            .flatten()
            .toList()


        nodeMap.forEach { node ->
            bridges += bridgeScanner.getBridgesForNode(node, nodeMap)
        }

        validateGame()
        createDebugLines()
        println("Bridges! Found ${bridges.size} bridges!")
    }

    fun createDebugLines() {
        // TODO: Consider moving this to a separate class
        lineRenderer.linesToRender.clear()
        dotRenderer.dotsToRender.clear()
        bridges.forEach { bridge ->
            val startNode = bridge.startNode
            val endNode = bridge.endNode

            startNode.connectedBridges += bridge
            endNode?.connectedBridges += bridge

            // Draw debug lines/dots
            if (endNode != null) {
                val line = DebugLine(startNode.worldCoords, endNode.worldCoords, Color.BLACK)
                lineRenderer.linesToRender += line
                dotRenderer.dotsToRender += line.dots
            } else {
                dotRenderer.dotsToRender += DebugDot(startNode.worldCoords, Color.WHITE, 0f)
            }
        }

        val ladderFloors = towers
            .asSequence()
            .flatten()
            .map { it.floors }
            .flatten()
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


    fun validateGame() {

        logger.info("Validating game...")
    }

}

