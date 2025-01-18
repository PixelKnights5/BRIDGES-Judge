package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3i
import com.pixelknights.bridgesgame.client.util.plus

enum class NodeSide(val vector: Vec3i) {
    N(Direction.NORTH.vector),
    E(Direction.EAST.vector),
    S(Direction.SOUTH.vector),
    W(Direction.WEST.vector),
    NE(Direction.NORTH.vector + Direction.EAST.vector),
    SE(Direction.SOUTH.vector + Direction.EAST.vector),
    SW(Direction.SOUTH.vector + Direction.WEST.vector),
    NW(Direction.NORTH.vector + Direction.WEST.vector);
}

data class Node(
    val side: NodeSide,
    val isOpen: Boolean,
    val floor: Floor,
    val worldPosition: BlockPos,
) {
    val connectedBridges: MutableSet<Bridge> = mutableSetOf()

    val coords = "${floor.coords}${side.name}"
    val worldCoords = "(${worldPosition.x}, ${worldPosition.y}, ${worldPosition.z})"

    companion object {
        const val DISTANCE_FROM_CENTER = 2
    }

    override fun toString(): String {
        return "Node(coords=$coords, isOpen=$isOpen, floor=$floor, worldCoords=$worldCoords"
    }

}