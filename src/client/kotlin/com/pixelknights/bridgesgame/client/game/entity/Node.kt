package com.pixelknights.bridgesgame.client.game.entity

enum class NodeSide(xDirection: Int, zDirection: Int) {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0),
    NORTHEAST(1, -1),
    SOUTHEAST(1, 1),
    SOUTHWEST(-1, 1),
    NORTHWEST(-1, -1),
}

class Node(
    val isOpen: Boolean,
    val connectedBridges: Set<Bridge>,
    val side: NodeSide,
) {
}