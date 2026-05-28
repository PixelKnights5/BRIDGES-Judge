package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos

data class ConnectionSegment(val start: BlockPos, val end: BlockPos) {

    /**
     * Returns true if the bounding boxes of this segment and [other] overlap on all three axes.
     * This is exact for axis-aligned segments and an approximation for diagonal ones.
     */
    fun intersectsWith(other: ConnectionSegment): Boolean {
        return overlaps(
            minOf(start.x, end.x), maxOf(start.x, end.x),
            minOf(other.start.x, other.end.x), maxOf(other.start.x, other.end.x),
        ) && overlaps(
            minOf(start.y, end.y), maxOf(start.y, end.y),
            minOf(other.start.y, other.end.y), maxOf(other.start.y, other.end.y),
        ) && overlaps(
            minOf(start.z, end.z), maxOf(start.z, end.z),
            minOf(other.start.z, other.end.z), maxOf(other.start.z, other.end.z),
        )
    }

    private fun overlaps(minA: Int, maxA: Int, minB: Int, maxB: Int) = minA <= maxB && minB <= maxA
}
