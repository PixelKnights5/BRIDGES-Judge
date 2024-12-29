package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.util.randomFloat
import net.minecraft.util.math.BlockPos


class DebugLine (
    val start: BlockPos,
    val end: BlockPos,
    val color: Color = COLOR,
    val noise: Float = (-0.5 .. 0.5).randomFloat(),
) {

    val dots: List<DebugDot> = listOf(
        DebugDot(start, Color.GREEN, noise),
        DebugDot(end, Color.BLUE, noise)
    )

    companion object {
        private val COLOR = Color(0.5f, 0.5f, 0.5f, 0.5f)
        val LINES = mutableListOf<DebugLine>()
    }
}