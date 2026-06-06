package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConnectionValidatorTest {

    // ---- helpers --------------------------------------------------------

    private fun makeNode(x: Int, y: Int, z: Int, side: NodeSide = NodeSide.N): Node {
        val tower = Tower(row = 0, column = 0, numFloors = 1, color = GameColor.WHITE, isBase = false)
        val floor = Floor(floorNumber = 0, tower = tower, worldCenter = BlockPos(x, y, z), isBase = false)
        val node = Node(side = side, isOpen = true, floor = floor, worldPosition = BlockPos(x, y, z))
        floor.nodes = listOf(node)
        return node
    }

    /** Registers [connection] on both of its endpoint nodes, mirroring what GameBoard.scanGame does. */
    private fun register(connection: Connection) {
        connection.nodeA.connections += connection
        connection.nodeB?.connections += connection
    }

    private fun bridge(nodeA: Node, nodeB: Node, segment: ConnectionSegment): Bridge {
        return Bridge(
            segments = listOf(segment),
            nodeA = nodeA,
            nodeB = nodeB,
            owner = GameColor.ORANGE,
            painter = null,
        )
    }

    private fun ladder(nodeA: Node, nodeB: Node, segment: ConnectionSegment): Ladder {
        return Ladder(nodeA = nodeA, nodeB = nodeB, segments = listOf(segment))
    }

    // ---- findIntersections tests ----------------------------------------

    @Test
    fun `findIntersections detects two crossing bridges`() {
        // Bridge A: (0,0,0) → (4,0,4);  Bridge B: (0,0,4) → (4,0,0) — cross at (2,0,2)
        val a1 = makeNode(0, 0, 0, NodeSide.N)
        val a2 = makeNode(4, 0, 4, NodeSide.S)
        val b1 = makeNode(0, 0, 4, NodeSide.E)
        val b2 = makeNode(4, 0, 0, NodeSide.W)

        val bridgeA = bridge(a1, a2, ConnectionSegment(a1.worldPosition, a2.worldPosition))
        val bridgeB = bridge(b1, b2, ConnectionSegment(b1.worldPosition, b2.worldPosition))
        register(bridgeA)
        register(bridgeB)

        val intersections = ConnectionValidator.findIntersections(listOf(bridgeA, bridgeB))

        assertEquals(1, intersections.size)
        assertEquals(BlockPos(2, 0, 2), intersections[0].point)
    }

    @Test
    fun `findIntersections ignores pairs that share a node`() {
        // Both bridges fan out from the same nodeA — they touch at nodeA but don't cross mid-span
        val shared = makeNode(0, 0, 0, NodeSide.N)
        val end1 = makeNode(4, 0, 0, NodeSide.S)
        val end2 = makeNode(0, 0, 4, NodeSide.E)

        val bridgeA = bridge(shared, end1, ConnectionSegment(shared.worldPosition, end1.worldPosition))
        val bridgeB = bridge(shared, end2, ConnectionSegment(shared.worldPosition, end2.worldPosition))
        register(bridgeA)
        register(bridgeB)

        val intersections = ConnectionValidator.findIntersections(listOf(bridgeA, bridgeB))

        assertTrue(intersections.isEmpty())
    }

    @Test
    fun `findIntersections returns empty when no connections cross`() {
        // Two parallel bridges on different Z tracks
        val a1 = makeNode(0, 0, 0, NodeSide.N)
        val a2 = makeNode(4, 0, 0, NodeSide.S)
        val b1 = makeNode(0, 0, 4, NodeSide.E)
        val b2 = makeNode(4, 0, 4, NodeSide.W)

        val bridgeA = bridge(a1, a2, ConnectionSegment(a1.worldPosition, a2.worldPosition))
        val bridgeB = bridge(b1, b2, ConnectionSegment(b1.worldPosition, b2.worldPosition))
        register(bridgeA)
        register(bridgeB)

        assertTrue(ConnectionValidator.findIntersections(listOf(bridgeA, bridgeB)).isEmpty())
    }

    // ---- findOverloadedNodes tests --------------------------------------

    @Test
    fun `findOverloadedNodes flags a node with two bridges`() {
        val shared = makeNode(0, 0, 0, NodeSide.N)
        val end1 = makeNode(4, 0, 0, NodeSide.S)
        val end2 = makeNode(0, 0, 4, NodeSide.E)

        val bridgeA = bridge(shared, end1, ConnectionSegment(shared.worldPosition, end1.worldPosition))
        val bridgeB = bridge(shared, end2, ConnectionSegment(shared.worldPosition, end2.worldPosition))
        register(bridgeA)
        register(bridgeB)

        val overloaded = ConnectionValidator.findOverloadedNodes(listOf(bridgeA, bridgeB))

        assertTrue(overloaded.contains(shared))
    }

    @Test
    fun `findOverloadedNodes does not flag a CENTER node with only ladders`() {
        val center0 = makeNode(0, 0, 0, NodeSide.CENTER)
        val center1 = makeNode(0, 10, 0, NodeSide.CENTER)
        val center2 = makeNode(0, 20, 0, NodeSide.CENTER)

        val ladderDown = ladder(center0, center1, ConnectionSegment(center0.worldPosition, center1.worldPosition))
        val ladderUp = ladder(center1, center2, ConnectionSegment(center1.worldPosition, center2.worldPosition))
        register(ladderDown)
        register(ladderUp)

        val overloaded = ConnectionValidator.findOverloadedNodes(listOf(ladderDown, ladderUp))

        assertTrue(overloaded.isEmpty())
    }

    @Test
    fun `findOverloadedNodes flags a node with a bridge and a ladder`() {
        val mixedNode = makeNode(0, 0, 0, NodeSide.N)
        val bridgeEnd = makeNode(4, 0, 0, NodeSide.S)
        val ladderEnd = makeNode(0, 10, 0, NodeSide.CENTER)

        val b = bridge(mixedNode, bridgeEnd, ConnectionSegment(mixedNode.worldPosition, bridgeEnd.worldPosition))
        val l = ladder(mixedNode, ladderEnd, ConnectionSegment(mixedNode.worldPosition, ladderEnd.worldPosition))
        register(b)
        register(l)

        val overloaded = ConnectionValidator.findOverloadedNodes(listOf(b, l))

        assertTrue(overloaded.contains(mixedNode))
    }
}
