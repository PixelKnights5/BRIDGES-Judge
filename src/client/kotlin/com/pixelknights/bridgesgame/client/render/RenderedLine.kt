package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.util.randomFloat
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class RenderedLine(
    val start: BlockPos,
    val end: BlockPos,
    val color: Color = COLOR,
    val noise: Float = (-0.5..0.5).randomFloat(),
    noiseVectorOverride: Vec3d? = null,
) {

    val noiseVector: Vec3d = noiseVectorOverride ?: run {
        val doubleNoise = noise.toDouble() / 2
        if (start.x == end.x && start.y == end.y) {
            Vec3d(0.0, doubleNoise, 0.0)
        } else {
            Vec3d(doubleNoise, 0.0, doubleNoise)
        }
    }

    val dots: List<RenderedDot> = listOf(
        RenderedDot(start, color, noiseVector),
        RenderedDot(end, color, noiseVector)
    )

    companion object {
        private val COLOR = Color(0.5f, 0.5f, 0.5f, 0.5f)
    }
}
