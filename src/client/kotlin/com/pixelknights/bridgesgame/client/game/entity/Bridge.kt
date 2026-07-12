package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import java.util.Objects

data class Bridge(
    override val segments: List<ConnectionSegment>,
    override val nodeA: Node,
    override val nodeB: Node?,
    override val owner: GameColor?,
    override val painter: GameColor?,
    override val errors: List<ConnectionError> = emptyList()
) : Connection {

    // Segments track the real (possibly multi-block, diagonal) glass footprint, not a
    // single line, so the midpoint is derived from the nodes directly at head height.
    override val midpoint: BlockPos = nodeB?.let {
        BlockPos((nodeA.worldPosition.x + it.worldPosition.x) / 2, nodeA.worldPosition.y, (nodeA.worldPosition.z + it.worldPosition.z) / 2)
    } ?: nodeA.worldPosition


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
        return "Bridge(segments=$segments, nodeA=$nodeA, nodeB=$nodeB, owner=$owner, painter=$painter)"
    }

}
