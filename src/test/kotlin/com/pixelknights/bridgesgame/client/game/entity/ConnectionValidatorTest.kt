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

    private fun bridge(nodeA: Node, nodeB: Node, segment: ConnectionSegment): Bridge =
        bridge(nodeA, nodeB, listOf(segment))

    private fun bridge(nodeA: Node, nodeB: Node, segments: List<ConnectionSegment>): Bridge {
        return Bridge(
            segments = segments,
            nodeA = nodeA,
            nodeB = nodeB,
            owner = GameColor.ORANGE,
            painter = null,
        )
    }

    private fun ladder(nodeA: Node, nodeB: Node, segment: ConnectionSegment): Ladder {
        return Ladder(nodeA = nodeA, nodeB = nodeB, segments = listOf(segment))
    }

    private fun circuit(nodeA: Node, nodeB: Node, segment: ConnectionSegment): Circuit =
        circuit(nodeA, nodeB, listOf(segment))

    private fun circuit(nodeA: Node, nodeB: Node, segments: List<ConnectionSegment>): Circuit {
        return Circuit(nodeA = nodeA, nodeB = nodeB, segments = segments)
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

    @Test
    fun `findIntersections still flags two crossing circuits by default`() {
        // Circuit A: (0,0,0) → (4,0,4);  Circuit B: (0,0,4) → (4,0,0) — cross at (2,0,2)
        val a1 = makeNode(0, 0, 0, NodeSide.N)
        val a2 = makeNode(4, 0, 4, NodeSide.S)
        val b1 = makeNode(0, 0, 4, NodeSide.E)
        val b2 = makeNode(4, 0, 0, NodeSide.W)

        val circuitA = circuit(a1, a2, ConnectionSegment(a1.worldPosition, a2.worldPosition))
        val circuitB = circuit(b1, b2, ConnectionSegment(b1.worldPosition, b2.worldPosition))
        register(circuitA)
        register(circuitB)

        val intersections = ConnectionValidator.findIntersections(listOf(circuitA, circuitB))

        assertEquals(1, intersections.size)
    }

    @Test
    fun `findIntersections does not flag two crossing circuits when allowCircuitCrossings is true`() {
        val a1 = makeNode(0, 0, 0, NodeSide.N)
        val a2 = makeNode(4, 0, 4, NodeSide.S)
        val b1 = makeNode(0, 0, 4, NodeSide.E)
        val b2 = makeNode(4, 0, 0, NodeSide.W)

        val circuitA = circuit(a1, a2, ConnectionSegment(a1.worldPosition, a2.worldPosition))
        val circuitB = circuit(b1, b2, ConnectionSegment(b1.worldPosition, b2.worldPosition))
        register(circuitA)
        register(circuitB)

        val intersections = ConnectionValidator.findIntersections(
            listOf(circuitA, circuitB),
            allowCircuitCrossings = true,
        )

        assertTrue(intersections.isEmpty())
    }

    @Test
    fun `findIntersections still flags a bridge crossing a circuit when allowCircuitCrossings is true`() {
        val a1 = makeNode(0, 0, 0, NodeSide.N)
        val a2 = makeNode(4, 0, 4, NodeSide.S)
        val b1 = makeNode(0, 0, 4, NodeSide.E)
        val b2 = makeNode(4, 0, 0, NodeSide.W)

        val bridgeA = bridge(a1, a2, ConnectionSegment(a1.worldPosition, a2.worldPosition))
        val circuitB = circuit(b1, b2, ConnectionSegment(b1.worldPosition, b2.worldPosition))
        register(bridgeA)
        register(circuitB)

        val intersections = ConnectionValidator.findIntersections(
            listOf(bridgeA, circuitB),
            allowCircuitCrossings = true,
        )

        assertEquals(1, intersections.size)
    }

    @Test
    fun `findIntersections detects a bridge crossing a circuit at real block level`() {
        // Regression test: BridgeScanner used to build segments at the node's head-height
        // position instead of the real glass floor 2 below, so a bridge passing directly
        // over a circuit's sculk path was never detected as crossing it.
        val bridgeNodeA = makeNode(92, -36, 67, NodeSide.N)
        val bridgeNodeB = makeNode(92, -36, 73, NodeSide.S)
        val circuitNodeA = makeNode(88, -36, 65, NodeSide.E)
        val circuitNodeB = makeNode(97, -36, 73, NodeSide.W)

        val bridgeConn = bridge(bridgeNodeA, bridgeNodeB, ConnectionSegment(BlockPos(92, -38, 67), BlockPos(92, -38, 73)))
        val circuitConn = circuit(circuitNodeA, circuitNodeB, ConnectionSegment(BlockPos(88, -38, 69), BlockPos(97, -38, 69)))
        register(bridgeConn)
        register(circuitConn)

        val intersections = ConnectionValidator.findIntersections(listOf(bridgeConn, circuitConn))

        assertEquals(1, intersections.size)
        assertEquals(BlockPos(92, -38, 69), intersections[0].point)
    }

    @Test
    fun `findIntersections does not flag a diagonal bridge that only touches a circuit's corner`() {
        // Regression test: a diagonal bridge's real block footprint runs parallel to, but
        // offset from, the idealized straight node-to-node line. The idealized line would
        // cross straight through the circuit's corner block; the real footprint passes it
        // by, mirroring the live (90,75)->(82,83) bridge vs. (86,79) circuit corner case.
        val bridgeNodeA = makeNode(14, 0, 6, NodeSide.N)
        val bridgeNodeB = makeNode(6, 0, 14, NodeSide.S)
        val circuitNodeA = makeNode(10, 0, 20, NodeSide.E)
        val circuitNodeB = makeNode(10, 0, 30, NodeSide.W)

        val bridgeFootprint = listOf(
            BlockPos(13, 0, 6), BlockPos(12, 0, 7), BlockPos(11, 0, 8), BlockPos(10, 0, 9),
            BlockPos(9, 0, 10), BlockPos(8, 0, 11), BlockPos(7, 0, 12), BlockPos(6, 0, 13),
        ).zipWithNext { a, b -> ConnectionSegment(a, b) }
        val circuitSegments = listOf(
            ConnectionSegment(BlockPos(10, 0, 10), BlockPos(20, 0, 10)),
            ConnectionSegment(BlockPos(10, 0, 10), BlockPos(10, 0, 11)),
        )

        val bridgeConn = bridge(bridgeNodeA, bridgeNodeB, bridgeFootprint)
        val circuitConn = circuit(circuitNodeA, circuitNodeB, circuitSegments)
        register(bridgeConn)
        register(circuitConn)

        val intersections = ConnectionValidator.findIntersections(listOf(bridgeConn, circuitConn))

        assertTrue(intersections.isEmpty())
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
