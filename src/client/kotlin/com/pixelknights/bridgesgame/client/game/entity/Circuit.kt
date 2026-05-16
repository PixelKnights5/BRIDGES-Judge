package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import java.util.Objects

/**
 * Placeholder for season 11 circuits: arbitrary 3D paths of sculk and twisting vines connecting nodes.
 * Scanner and rendering are not yet implemented.
 */
data class Circuit(
    override val nodeA: Node,
    override val nodeB: Node?,
    override val owner: GameColor?,
    override val painter: GameColor?,
    override val blocks: List<BlockPos>,
    override val errors: List<ConnectionError> = emptyList(),
) : Connection {

    override fun canTeamUse(team: GameColor): Boolean {
        return nodeB != null && (owner == team || painter == team)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Circuit) return false

        return ((nodeA == other.nodeA && nodeB == other.nodeB) ||
                (nodeA == other.nodeB && nodeB == other.nodeA))
    }

    override fun hashCode() = Objects.hash(nodeA, nodeB) + Objects.hash(nodeB, nodeA)
}
