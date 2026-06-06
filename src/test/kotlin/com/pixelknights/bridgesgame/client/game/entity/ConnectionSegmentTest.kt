package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConnectionSegmentTest {

    @Test
    fun `intersectsWith returns true for collinear overlapping segments`() {
        val a = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(5, 0, 0))
        val b = ConnectionSegment(BlockPos(3, 0, 0), BlockPos(8, 0, 0))

        assertTrue(a.intersectsWith(b))
        assertTrue(b.intersectsWith(a))
    }

    @Test
    fun `intersectsWith returns true for identical segments`() {
        val seg = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(5, 0, 0))

        assertTrue(seg.intersectsWith(seg))
    }

    @Test
    fun `intersectsWith returns true for T-intersection`() {
        val xRun = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(5, 0, 0))
        val zRun = ConnectionSegment(BlockPos(3, 0, -2), BlockPos(3, 0, 2))

        assertTrue(xRun.intersectsWith(zRun))
        assertTrue(zRun.intersectsWith(xRun))
    }

    @Test
    fun `intersectsWith returns true for perpendicular crossing in Y`() {
        val vertical = ConnectionSegment(BlockPos(3, 0, 3), BlockPos(3, 5, 3))
        val horizontal = ConnectionSegment(BlockPos(0, 2, 3), BlockPos(6, 2, 3))

        assertTrue(vertical.intersectsWith(horizontal))
        assertTrue(horizontal.intersectsWith(vertical))
    }

    @Test
    fun `intersectsWith returns false for parallel non-overlapping segments on Z axis`() {
        val a = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(5, 0, 0))
        val b = ConnectionSegment(BlockPos(0, 0, 2), BlockPos(5, 0, 2))

        assertFalse(a.intersectsWith(b))
    }

    @Test
    fun `intersectsWith returns false for collinear non-touching segments`() {
        val a = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(5, 0, 0))
        val b = ConnectionSegment(BlockPos(7, 0, 0), BlockPos(10, 0, 0))

        assertFalse(a.intersectsWith(b))
    }

    @Test
    fun `intersectsWith returns false for segments separated on Y axis`() {
        val lower = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(5, 0, 0))
        val upper = ConnectionSegment(BlockPos(0, 5, 0), BlockPos(5, 5, 0))

        assertFalse(lower.intersectsWith(upper))
    }

    @Test
    fun `intersectsWith returns true for segments sharing a single endpoint`() {
        val a = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(3, 0, 0))
        val b = ConnectionSegment(BlockPos(3, 0, 0), BlockPos(6, 0, 0))

        assertTrue(a.intersectsWith(b))
    }

    // ---- exact diagonal tests ----

    @Test
    fun `intersectsWith returns true for crossing diagonal segments`() {
        val a = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(4, 0, 4))
        val b = ConnectionSegment(BlockPos(0, 0, 4), BlockPos(4, 0, 0))

        assertTrue(a.intersectsWith(b))
        assertTrue(b.intersectsWith(a))
    }

    @Test
    fun `intersectionWith returns correct crossing point for diagonal segments`() {
        val a = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(4, 0, 4))
        val b = ConnectionSegment(BlockPos(0, 0, 4), BlockPos(4, 0, 0))

        assertEquals(BlockPos(2, 0, 2), a.intersectionWith(b))
    }

    @Test
    fun `intersectsWith returns false for parallel diagonal segments whose bounding boxes overlap`() {
        // Both go in the same direction; their AABB shares z=4 but lines never meet
        val a = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(4, 0, 4))
        val b = ConnectionSegment(BlockPos(0, 0, 4), BlockPos(4, 0, 8))

        assertFalse(a.intersectsWith(b))
    }

    @Test
    fun `intersectsWith returns false for skew segments`() {
        // Segments in different y-planes — skew, cannot intersect
        val a = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(4, 0, 4))
        val b = ConnectionSegment(BlockPos(2, 1, 0), BlockPos(2, 1, 4))

        assertFalse(a.intersectsWith(b))
    }

    @Test
    fun `intersectionWith returns null for non-intersecting segments`() {
        val a = ConnectionSegment(BlockPos(0, 0, 0), BlockPos(5, 0, 0))
        val b = ConnectionSegment(BlockPos(7, 0, 0), BlockPos(10, 0, 0))

        assertNull(a.intersectionWith(b))
    }
}
