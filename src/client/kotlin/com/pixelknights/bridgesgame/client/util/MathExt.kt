package com.pixelknights.bridgesgame.client.util

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.joml.Matrix4fStack
import org.joml.Vector3f
import kotlin.math.*


fun ClosedFloatingPointRange<Double>.randomFloat(): Float {
    return (this.start + Math.random() * (this.endInclusive - this.start)).toFloat()
}

operator fun BlockPos.plus(other: Vec3i): BlockPos = this.add(other)
operator fun BlockPos.minus(other: Vec3i): BlockPos = this.subtract(other)
operator fun BlockPos.times(scale: Int): BlockPos = this.multiply(scale)

operator fun Vec3i.plus(other: Vec3i): Vec3i = this.add(other)
operator fun Vec3i.times(scale: Int): Vec3i = this.multiply(scale)
operator fun Vec3i.minus(other: Vec3i): Vec3i = this.subtract(other)

operator fun Vec3d.plus(other: Vec3d): Vec3d = this.add(other)
operator fun Vec3d.minus(other: Vec3d): Vec3d = this.subtract(other)
operator fun Vec3d.times(other: Vec3d): Vec3d = this.multiply(other)

operator fun Vector3f.plus(other: Vector3f): Vector3f = this.add(other)
operator fun Vector3f.minus(other: Vector3f): Vector3f = this.sub(other)
operator fun Vector3f.times(other: Float): Vector3f = this.mul(other)

fun Vec3i.toVector3f(): Vector3f = Vector3f(this.x.toFloat(), this.y.toFloat(), this.z.toFloat())

fun Vec3i.distanceTo(other: Vec3i): Double {
    return sqrt(this.getSquaredDistance(other))
}

fun Vec3d.toInt(): Vec3i = Vec3i(this.x.toInt(), this.y.toInt(), this.z.toInt())

/**
 * Rotate a 2D vector by a given number of degrees.
 * This ONLY works if the degrees are divisible by 90.
 */
fun Vec3i.rotateBy(degrees: Int): Vec3i {
    // 2D rotation matrix:
    // [x', z'] = | cos(theta)  -sin(theta) | * | x |
    //            | sin(theta)   cos(theta) |   | z |
    val rad = Math.toRadians(degrees.toDouble())
    // Convert to integers to avoid floating point errors
    // This will work for numbers divisible by 90 because the result of sin and cos will be -1, 0, or 1
    val sinAngle = sin(rad).toInt()
    val cosAngle = cos(rad).toInt()
    val x = ((this.x * cosAngle) - (this.z * sinAngle))
    val z = ((this.x * sinAngle) + (this.z * cosAngle))
    return Vec3i(x, this.y, z)
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

fun Matrix4fStack.translate(x: Double, y: Double, z: Double) {
    this.translate(x.toFloat(), y.toFloat(), z.toFloat())
}