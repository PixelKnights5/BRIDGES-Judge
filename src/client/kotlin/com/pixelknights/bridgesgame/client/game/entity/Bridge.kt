package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import java.util.Objects

enum class BridgeError {
    BRIDGE_TO_CLOSED_NODE,
    MULTIPLE_BRIDGES_TO_SINGLE_NODE,
    INCORRECT_SHAPE,
}

data class Bridge (
    val blocks: List<BlockPos>,
    val startNode: Node,
    val endNode: Node?,
    val owner: GameColor?,
    val painter: GameColor?,
    val errors: List<BridgeError> = mutableListOf()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Bridge) return false

        return ((startNode == other.startNode && endNode == other.endNode) ||
                (startNode == other.endNode   && endNode == other.startNode))
    }

    override fun hashCode() = Objects.hash(startNode, endNode)

    override fun toString(): String {
        return "Bridge(blocks=$blocks, startNode=$startNode, endNode=$endNode, owner=$owner, painter=$painter)"
    }


}