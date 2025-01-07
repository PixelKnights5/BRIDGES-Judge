package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.util.randomFloat
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d


class DebugLine (
    val start: BlockPos,
    val end: BlockPos,
    val color: Color = COLOR,
    val noise: Float = (-0.5 .. 0.5).randomFloat(),
) {

    val noiseVector: Vec3d
        get() {
            val doubleNoise = noise.toDouble()
            return if (start.x == end.x && start.y == end.y) {
                Vec3d(0.0, doubleNoise, 0.0)
            } else {
                Vec3d(doubleNoise, 0.0, doubleNoise)
            }
        }

    val dots: List<DebugDot> = listOf(
        DebugDot(start, Color.GREEN, noiseVector),
        DebugDot(end, Color.BLUE, noiseVector)
    )

    companion object {
        private val COLOR = Color(0.5f, 0.5f, 0.5f, 0.5f)
        val LINES = mutableListOf<DebugLine>()
    }
}