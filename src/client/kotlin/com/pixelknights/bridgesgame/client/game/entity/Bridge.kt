package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import java.util.Objects

data class Bridge (
    val blocks: List<BlockPos>,
    val startNode: Node,
    val endNode: Node,
    val owner: GameColor?,
    val painter: GameColor?,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Bridge) return false

        if (startNode != other.startNode) return false
        if (endNode != other.endNode) return false

        return true
    }

    override fun hashCode() = Objects.hash(startNode, endNode)

    override fun toString(): String {
        return "Bridge(blocks=$blocks, startNode=$startNode, endNode=$endNode, owner=$owner, painter=$painter)"
    }


}