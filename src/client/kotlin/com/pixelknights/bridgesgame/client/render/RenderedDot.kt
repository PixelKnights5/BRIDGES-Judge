package com.pixelknights.bridgesgame.client.render

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

data class RenderedDot(
    val position: BlockPos,
    val color: Color,
    val noise: Vec3d,
)
