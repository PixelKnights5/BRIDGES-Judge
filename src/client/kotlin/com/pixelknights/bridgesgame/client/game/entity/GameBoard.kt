package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.game.entity.scanner.BridgeScanner
import com.pixelknights.bridgesgame.client.game.entity.scanner.TowerScanner
import com.pixelknights.bridgesgame.client.render.Color
import com.pixelknights.bridgesgame.client.render.DebugLine
import com.pixelknights.bridgesgame.client.render.DotRenderer
import com.pixelknights.bridgesgame.client.render.LineRenderer
import net.minecraft.util.math.BlockPos
import org.koin.core.component.KoinComponent

class GameBoard constructor(
    private val towerScanner: TowerScanner,
    private val bridgeScanner: BridgeScanner,
    private val dotRenderer: DotRenderer,
    private val lineRenderer: LineRenderer,
) : KoinComponent {

    private var towers: List<List<Tower>> = mutableListOf<MutableList<Tower>>()
    private var bridges = mutableListOf<Bridge>()

    fun scanGame(centerCoordinate: BlockPos) {
        bridges.clear()
        towers = towerScanner.getTowers(centerCoordinate)

        // Get a mapping from floorNum -> every node on that floor
        val nodeMap = towers.asSequence().flatten().map { it.floors }.flatten().map { it.nodes }.flatten().toList()
//            .groupBy { it.floorNumber }
//            .map {
//                val key = it.key
//                val value = it.value.map { floor ->
//                    floor.nodes
//                }.flatten()
//                return@map key to value
//            }.toMap()

//        nodeMap.forEach { kv ->
//            val floor = kv.key
//            val nodes = kv.value
//            val floorBridges = nodes.map { node ->
//                val floorNodes = nodeMap[floor] ?: throw NullPointerException()
//                val bridges = bridgeScanner.getBridgesForNode(node, floorNodes)
//
//                if (bridges.size > 1) {
//                    logger.error("Found a node with multiple bridges: $node")
//                }
//                return@map bridges
//            }.flatten()
//            bridges += floorBridges
//        }
        nodeMap.forEach { node ->
            bridges += bridgeScanner.getBridgesForNode(node, nodeMap)
        }

        DebugLine.LINES.clear()
        dotRenderer.dotsToRender.clear()
        bridges.forEach { bridge ->
            if (bridge.errors.isEmpty()) {
                val start = bridge.startNode.worldCoords
                val end = bridge.endNode?.worldCoords

                dotRenderer.dotsToRender += start
                if (end != null) {
                    dotRenderer.dotsToRender += end
                    lineRenderer.linesToRender += DebugLine(start, end, Color(0f, 0f, 0f, 1f))
                }
            }
        }

        println("Bridges! Found ${bridges.size} bridges!")
    }

    fun validateGame() {
        TODO()
    }

}

