package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos

enum class ConnectionError {
    BRIDGE_TO_CLOSED_NODE,
    MULTIPLE_BRIDGES_TO_SINGLE_NODE,
    INCORRECT_SHAPE,
    CIRCUIT_TO_CLOSED_NODE,
}

sealed interface Connection {
    val nodeA: Node
    val nodeB: Node?
    val owner: GameColor?
    val painter: GameColor?
    val errors: List<ConnectionError>
    val segments: List<ConnectionSegment>
    val midpoint: BlockPos

    fun otherEnd(node: Node): Node? = when (node) {
        nodeA -> nodeB
        nodeB -> nodeA
        else -> null
    }

    fun canTeamUse(team: GameColor): Boolean
}
