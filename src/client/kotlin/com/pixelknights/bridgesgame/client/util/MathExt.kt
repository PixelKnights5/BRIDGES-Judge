package com.pixelknights.bridgesgame.client.util

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i


operator fun BlockPos.plus(other: Vec3i): BlockPos = this.add(other)
operator fun BlockPos.minus(other: Vec3i): BlockPos = this.subtract(other)
operator fun BlockPos.times(scale: Int): BlockPos = this.multiply(scale)
