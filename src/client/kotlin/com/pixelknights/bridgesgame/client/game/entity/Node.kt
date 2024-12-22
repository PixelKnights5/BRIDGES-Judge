package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i

enum class NodeSide(val vector: Vec3i) {
    NORTH(Vec3i(0, 0, -1)),
    EAST(Vec3i(1, 0, 0)),
    SOUTH(Vec3i(0, 0, 1)),
    WEST(Vec3i(-1, 0, 0)),
    NORTHEAST(Vec3i(1, 0, -1)),
    SOUTHEAST(Vec3i(1, 0, 1)),
    SOUTHWEST(Vec3i(-1, 0, 1)),
    NORTHWEST(Vec3i(-1, 0, -1));
}

data class Node(
    val side: NodeSide,
    val isOpen: Boolean,
    val floor: Floor,
    val worldCoords: BlockPos,
) {
    lateinit var connectedBridges: Set<Bridge>

    companion object {
        const val DISTANCE_FROM_CENTER = 2
    }

}