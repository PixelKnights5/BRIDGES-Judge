package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.game.entity.Bridge
import com.pixelknights.bridgesgame.client.game.entity.BridgeError
import com.pixelknights.bridgesgame.client.game.entity.Node
import com.pixelknights.bridgesgame.client.game.entity.NodeSide
import com.pixelknights.bridgesgame.client.game.rules.BridgeTemplate
import com.pixelknights.bridgesgame.client.util.plus
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos

class BridgeScanner (
    private val mc: MinecraftClient,
) {

    fun getBridgesForNode(node: Node, allNodes: List<Node>): Set<Bridge> {
        return BridgeTemplate.ALL_BRIDGE_COMBINATIONS.flatten().map { template ->

            // Don't check against the wrong set of templates.
            if (node.side in CORNER_NODES && !template.isCornerTemplate) {
                return@map null
            } else if (node.side !in CORNER_NODES && template.isCornerTemplate) {
                return@map null
            }

            val errors = mutableListOf<BridgeError>()
            val owner = template.findBridgeOwner(mc, node.worldCoords.down(2))

            // If there is no bridge, don't continue.
            @Suppress("FoldInitializerAndIfToElvis")
            if (owner == null) {
                return@map null
            }

            val painter = template.findBridgeOwner(mc, node.worldCoords.down(1))
            val endNode = allNodes.filter { it.worldCoords == (node.worldCoords + template.targetNodeOffset) }

            if (endNode.isEmpty()) {
                errors += BridgeError.BRIDGE_TO_CLOSED_NODE
            }

            return@map Bridge(
                blocks = template.blockCoords.map(::BlockPos),
                startNode = node,
                endNode = endNode.firstOrNull(),
                owner = owner,
                painter = painter,
                errors = errors,
            )
        }.filterNotNull().toSet()
    }


    companion object {
        private val CORNER_NODES = arrayOf(NodeSide.NORTHEAST, NodeSide.SOUTHEAST, NodeSide.NORTHWEST, NodeSide.SOUTHWEST)
    }
}

