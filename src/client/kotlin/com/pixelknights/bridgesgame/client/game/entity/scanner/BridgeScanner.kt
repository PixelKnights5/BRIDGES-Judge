package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.game.entity.Bridge
import com.pixelknights.bridgesgame.client.game.entity.ConnectionError
import com.pixelknights.bridgesgame.client.game.entity.ConnectionSegment
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
        if (node.side == NodeSide.CENTER) {
            return emptySet()
        }

        return BridgeTemplate.ALL_BRIDGE_COMBINATIONS.flatten().map { template ->

            // Don't check against the wrong set of templates.
            if (node.side in CORNER_NODES && !template.isCornerTemplate) {
                return@map null
            } else if (node.side !in CORNER_NODES && template.isCornerTemplate) {
                return@map null
            }

            val errors = mutableListOf<ConnectionError>()
            val owner = template.findBridgeOwner(mc, node.worldPosition.down(2))

            // If there is no bridge, don't continue.
            @Suppress("FoldInitializerAndIfToElvis")
            if (owner == null) {
                return@map null
            }

            val painter = template.findBridgePainter(mc, node.worldPosition.down(1))
            val targetPosition = node.worldPosition + template.targetNodeOffset
            val endNode = allNodes.filter { it.worldPosition == targetPosition }

            // Segments follow the real glass footprint (the same blocks findBridgeOwner
            // scans), not an idealized node-to-node line. Diagonal formations step through
            // blocks that aren't face-adjacent, so an idealized straight line can cut
            // through — or miss — blocks the real bridge doesn't occupy.
            val segments = template.translate(node.worldPosition.down(2)).blockCoords
                .map { BlockPos(it) }
                .zipWithNext { a, b -> ConnectionSegment(a, b) }

            // No node at all at the target position means the corner template is matching blocks
            // that belong to a nearby bridge — not a real connection.
            if (endNode.isEmpty()) {
                return@map null
            }

            if (!node.isOpen) {
                errors += ConnectionError.BRIDGE_TO_CLOSED_NODE
                // If the target is open, this bridge is already detected from the open side — skip to avoid double-reporting.
                if (endNode.any { it.isOpen }) {
                    return@map null
                }
            }

            if (endNode.none { it.isOpen }) {
                errors += ConnectionError.BRIDGE_TO_CLOSED_NODE
            }

            return@map Bridge(
                segments = segments,
                nodeA = node,
                nodeB = endNode.firstOrNull { it.isOpen },
                owner = owner,
                painter = painter,
                errors = errors,
            )
        }.filterNotNull().toSet()
    }


    companion object {
        private val CORNER_NODES = arrayOf(NodeSide.NE, NodeSide.SE, NodeSide.NW, NodeSide.SW)
    }
}

