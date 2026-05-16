package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import java.util.Objects

data class Ladder(
    override val nodeA: Node,
    override val nodeB: Node?,
    override val blocks: List<BlockPos>,
) : Connection {

    override val owner: GameColor? = null
    override val painter: GameColor? = null
    override val errors: List<ConnectionError> = emptyList()

    override fun canTeamUse(team: GameColor): Boolean = nodeB != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ladder) return false

        return ((nodeA == other.nodeA && nodeB == other.nodeB) ||
                (nodeA == other.nodeB && nodeB == other.nodeA))
    }

    override fun hashCode() = Objects.hash(nodeA, nodeB) + Objects.hash(nodeB, nodeA)

    override fun toString(): String {
        return "Ladder(nodeA=$nodeA, nodeB=$nodeB)"
    }
}
