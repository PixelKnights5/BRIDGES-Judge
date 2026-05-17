package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.game.entity.Circuit
import com.pixelknights.bridgesgame.client.game.entity.ConnectionError
import com.pixelknights.bridgesgame.client.game.entity.Node
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos

class CircuitScanner(private val mc: MinecraftClient) {

    fun getCircuitsForNode(node: Node, allNodes: List<Node>): Set<Circuit> {
        if (!node.isOpen) {
            return emptySet()
        }

        val world = mc.world ?: return emptySet()

        // Sculk runs at ground level (2 below the node) but adjacent to the node, not directly under it
        val groundPos = node.worldPosition.down(2)
        val startBlocks = groundPos.cardinalNeighbors().filter {
            world.getBlockState(it).block == Blocks.SCULK
        }
        if (startBlocks.isEmpty()) {
            return emptySet()
        }

        // Map each node's adjacent ground positions to that node for endpoint detection
        val endpointAnchors: Map<BlockPos, Node> = allNodes
            .filter { it.isOpen }
            .flatMap { n -> n.worldPosition.down(2).cardinalNeighbors().map { it to n } }
            .toMap()

        val visited = linkedSetOf<BlockPos>().also { it += startBlocks }
        val queue = ArrayDeque<BlockPos>().also { it += startBlocks }
        var endNode: Node? = null

        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            val curBlock = world.getBlockState(cur).block
            for (neighbor in cur.adjacentFaceNeighbors()) {
                if (neighbor in visited) continue
                val neighborBlock = world.getBlockState(neighbor).block
                if (!blocksConnect(curBlock, neighborBlock, cur, neighbor)) continue

                visited += neighbor
                queue += neighbor

                endpointAnchors[neighbor]?.takeIf { it != node }?.let { endNode = it }
            }
        }

        val errors = if (endNode == null) listOf(ConnectionError.CIRCUIT_TO_CLOSED_NODE) else emptyList()
        return setOf(
            Circuit(
                nodeA = node,
                nodeB = endNode,
                blocks = visited.toList(),
                errors = errors,
            ),
        )
    }

    companion object {
        private val CIRCUIT_BLOCKS = setOf(Blocks.SCULK, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT)

        private fun blocksConnect(a: Block, b: Block, posA: BlockPos, posB: BlockPos): Boolean {
            if (a !in CIRCUIT_BLOCKS || b !in CIRCUIT_BLOCKS) return false
            return if (posA.y != posB.y) {
                true // any sculk/vine combo connects vertically
            } else {
                // Horizontal: at least one must be sculk.
                // Allows sculk-sculk (ground path) and vine-sculk (transition at top of vine chain).
                a == Blocks.SCULK || b == Blocks.SCULK
            }
        }

        private fun BlockPos.cardinalNeighbors(): List<BlockPos> =
            listOf(north(), south(), east(), west())

        private fun BlockPos.adjacentFaceNeighbors(): List<BlockPos> =
            listOf(north(), south(), east(), west(), up(), down())
    }
}