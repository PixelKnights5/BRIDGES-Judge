package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3i
import com.pixelknights.bridgesgame.client.util.plus

enum class NodeSide(val vector: Vec3i) {
    NORTH(Direction.NORTH.vector),
    EAST(Direction.EAST.vector),
    SOUTH(Direction.SOUTH.vector),
    WEST(Direction.WEST.vector),
    NORTHEAST(Direction.NORTH.vector + Direction.EAST.vector),
    SOUTHEAST(Direction.SOUTH.vector + Direction.EAST.vector),
    SOUTHWEST(Direction.SOUTH.vector + Direction.WEST.vector),
    NORTHWEST(Direction.NORTH.vector + Direction.WEST.vector);
}

data class Node(
    val side: NodeSide,
    val isOpen: Boolean,
    val floor: Floor,
    val worldCoords: BlockPos,
) {
    val connectedBridges: MutableSet<Bridge> = mutableSetOf()

    companion object {
        const val DISTANCE_FROM_CENTER = 2
    }

    override fun toString(): String {
        return "Node(side=$side, isOpen=$isOpen, floor=$floor, worldCoords=$worldCoords)"
    }


}