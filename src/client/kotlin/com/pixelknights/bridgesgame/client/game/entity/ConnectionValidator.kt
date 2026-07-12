package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos

object ConnectionValidator {

    data class Intersection(val a: Connection, val b: Connection, val point: BlockPos)

    /**
     * Returns all unordered pairs of distinct connections whose segments physically cross.
     * Pairs that share an endpoint node are excluded — those are reported by
     * [findOverloadedNodes] instead.
     */
    fun findIntersections(
        connections: Collection<Connection>,
        allowCircuitCrossings: Boolean = false,
    ): List<Intersection> {
        val list = connections.toList()
        val result = mutableListOf<Intersection>()
        for (i in list.indices) {
            for (j in i + 1 until list.size) {
                val a = list[i]
                val b = list[j]
                if (sharesNode(a, b)) {
                    continue
                }
                if (allowCircuitCrossings && a is Circuit && b is Circuit) {
                    continue
                }
                val point = firstCrossingPoint(a, b) ?: continue
                result += Intersection(a, b, point)
            }
        }
        return result
    }

    private fun firstCrossingPoint(a: Connection, b: Connection): BlockPos? {
        for (segA in a.segments) {
            for (segB in b.segments) {
                val point = segA.intersectionWith(segB)
                if (point != null) {
                    return point
                }
            }
        }
        return null
    }

    private fun sharesNode(a: Connection, b: Connection): Boolean {
        val aNodes = setOfNotNull(a.nodeA, a.nodeB)
        val bNodes = setOfNotNull(b.nodeA, b.nodeB)
        return aNodes.any { it in bNodes }
    }

    /**
     * Returns all nodes that have more than one connection attached and are not exclusively
     * connected to ladders. A CENTER node with ladders going up and down is legitimate, but any
     * node that mixes in a bridge or circuit alongside another connection is flagged.
     */
    fun findOverloadedNodes(connections: Collection<Connection>): List<Node> {
        val allNodes = connections.flatMap { listOfNotNull(it.nodeA, it.nodeB) }.toSet()
        return allNodes.filter { node ->
            node.connections.size > 1 && node.connections.any { it !is Ladder }
        }
    }
}
