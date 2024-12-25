package com.pixelknights.bridgesgame.client.util

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World

fun World.getBlockState(vec3i: Vec3i) = getBlockState(BlockPos(vec3i))