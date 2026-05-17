package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import java.util.Objects

data class Bridge(
    override val blocks: List<BlockPos>,
    override val nodeA: Node,
    override val nodeB: Node?,
    override val owner: GameColor?,
    override val painter: GameColor?,
    override val errors: List<ConnectionError> = mutableListOf()
) : Connection {

    override fun canTeamUse(team: GameColor): Boolean {
        return nodeB != null && (owner == team || painter == team)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Bridge) {
            return false
        }

        return ((nodeA == other.nodeA && nodeB == other.nodeB) ||
                (nodeA == other.nodeB && nodeB == other.nodeA))
    }

    override fun hashCode() = Objects.hash(nodeA, nodeB) + Objects.hash(nodeB, nodeA)

    override fun toString(): String {
        return "Bridge(blocks=$blocks, nodeA=$nodeA, nodeB=$nodeB, owner=$owner, painter=$painter)"
    }

}
