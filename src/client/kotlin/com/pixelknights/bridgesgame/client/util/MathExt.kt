package com.pixelknights.bridgesgame.client.util

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt


operator fun BlockPos.plus(other: Vec3i): BlockPos = this.add(other)
operator fun BlockPos.minus(other: Vec3i): BlockPos = this.subtract(other)
operator fun BlockPos.times(scale: Int): BlockPos = this.multiply(scale)
operator fun Vec3i.plus(other: Vec3i): Vec3i = this.add(other)
operator fun Vec3i.times(scale: Int): Vec3i = this.multiply(scale)
operator fun Vec3i.minus(other: Vec3i): Vec3i = this.subtract(other)

fun Vec3i.distanceTo(other: Vec3i): Double {
    return sqrt(this.getSquaredDistance(other))
}


/**
 * Returns a sign (1 or -1) for each axis
 */
fun Vec3i.sign(): Vec3i = Vec3i(x.sign, y.sign, z.sign)


fun Vec3i.abs(): Vec3i {
    return Vec3i(
        abs(x),
        abs(y),
        abs(z)
    )
}