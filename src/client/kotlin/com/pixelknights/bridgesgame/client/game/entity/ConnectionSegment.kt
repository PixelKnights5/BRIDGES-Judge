package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos

data class ConnectionSegment(val start: BlockPos, val end: BlockPos) {

    /**
     * Returns the BlockPos at or nearest to where this segment and [other] cross, or null if
     * they do not intersect. Uses exact integer arithmetic — no floating-point approximation.
     *
     * Handles three cases:
     *  - Parallel/collinear: checks 1-D parameter-range overlap along the shared axis.
     *  - Coplanar non-parallel: solves for parameters t ∈ [0,1] and u ∈ [0,1].
     *  - Skew (non-coplanar): returns null.
     */
    fun intersectionWith(other: ConnectionSegment): BlockPos? {
        val p1x = start.x.toLong()
        val p1y = start.y.toLong()
        val p1z = start.z.toLong()
        val d1x = end.x.toLong() - p1x
        val d1y = end.y.toLong() - p1y
        val d1z = end.z.toLong() - p1z

        val q1x = other.start.x.toLong()
        val q1y = other.start.y.toLong()
        val q1z = other.start.z.toLong()
        val d2x = other.end.x.toLong() - q1x
        val d2y = other.end.y.toLong() - q1y
        val d2z = other.end.z.toLong() - q1z

        // qp = Q1 − P1
        val qpx = q1x - p1x
        val qpy = q1y - p1y
        val qpz = q1z - p1z

        // cross = d1 × d2
        val cx = d1y * d2z - d1z * d2y
        val cy = d1z * d2x - d1x * d2z
        val cz = d1x * d2y - d1y * d2x
        val crossLenSq = cx * cx + cy * cy + cz * cz

        if (crossLenSq == 0L) {
            // Parallel — check collinear via qp × d1 == 0
            val qpCd1x = qpy * d1z - qpz * d1y
            val qpCd1y = qpz * d1x - qpx * d1z
            val qpCd1z = qpx * d1y - qpy * d1x
            if (qpCd1x != 0L || qpCd1y != 0L || qpCd1z != 0L) {
                return null // Parallel, not collinear
            }
            val d1LenSq = d1x * d1x + d1y * d1y + d1z * d1z
            if (d1LenSq == 0L) {
                return null // Degenerate zero-length segment
            }
            // Map Q1 and Q2 onto the P1→P2 parameter axis (numerators scaled by d1LenSq)
            val t0Num = qpx * d1x + qpy * d1y + qpz * d1z
            val t1Num = t0Num + (d2x * d1x + d2y * d1y + d2z * d1z)
            val overlapMin = maxOf(minOf(t0Num, t1Num), 0L)
            val overlapMax = minOf(maxOf(t0Num, t1Num), d1LenSq)
            if (overlapMin > overlapMax) {
                return null
            }
            // Midpoint of overlap interval, scaled by 2 * d1LenSq to avoid fractional division
            val tMidNum = overlapMin + overlapMax // sum; divide by 2 * d1LenSq
            val denom = 2L * d1LenSq
            val x = (p1x * denom + tMidNum * d1x) / denom
            val y = (p1y * denom + tMidNum * d1y) / denom
            val z = (p1z * denom + tMidNum * d1z) / denom
            return BlockPos(x.toInt(), y.toInt(), z.toInt())
        }

        // Non-parallel — check coplanar: qp · cross == 0
        val dotQpCross = qpx * cx + qpy * cy + qpz * cz
        if (dotQpCross != 0L) {
            return null // Skew lines
        }

        // Solve t and u as numerators over crossLenSq
        val qpCd2x = qpy * d2z - qpz * d2y
        val qpCd2y = qpz * d2x - qpx * d2z
        val qpCd2z = qpx * d2y - qpy * d2x
        val tNum = qpCd2x * cx + qpCd2y * cy + qpCd2z * cz

        val qpCd1x = qpy * d1z - qpz * d1y
        val qpCd1y = qpz * d1x - qpx * d1z
        val qpCd1z = qpx * d1y - qpy * d1x
        val uNum = qpCd1x * cx + qpCd1y * cy + qpCd1z * cz

        if (tNum < 0L || tNum > crossLenSq || uNum < 0L || uNum > crossLenSq) {
            return null
        }

        val x = (p1x * crossLenSq + tNum * d1x) / crossLenSq
        val y = (p1y * crossLenSq + tNum * d1y) / crossLenSq
        val z = (p1z * crossLenSq + tNum * d1z) / crossLenSq
        return BlockPos(x.toInt(), y.toInt(), z.toInt())
    }

    fun intersectsWith(other: ConnectionSegment): Boolean = intersectionWith(other) != null
}
