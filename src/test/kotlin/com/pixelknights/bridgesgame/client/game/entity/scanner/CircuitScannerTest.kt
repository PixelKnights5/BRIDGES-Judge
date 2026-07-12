package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.FabricExtension
import com.pixelknights.bridgesgame.client.game.entity.ConnectionError
import com.pixelknights.bridgesgame.client.game.entity.Floor
import com.pixelknights.bridgesgame.client.game.entity.GameColor
import com.pixelknights.bridgesgame.client.game.entity.Node
import com.pixelknights.bridgesgame.client.game.entity.NodeSide
import com.pixelknights.bridgesgame.client.game.entity.Tower
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(FabricExtension::class)
class CircuitScannerTest {

    // ---- helpers --------------------------------------------------------

    private fun makeNode(x: Int, y: Int, z: Int, side: NodeSide = NodeSide.N): Node {
        val tower = Tower(row = 0, column = 0, numFloors = 1, color = GameColor.WHITE, isBase = false)
        val floor = Floor(floorNumber = 0, tower = tower, worldCenter = BlockPos(x, y, z), isBase = false)
        val node = Node(side = side, isOpen = true, floor = floor, worldPosition = BlockPos(x, y, z))
        floor.nodes = listOf(node)
        return node
    }

    // ---- buildCircuits tests ---------------------------------------------

    @Test
    fun `buildCircuits emits one circuit per endpoint`() {
        val scanned = makeNode(0, 0, 0, NodeSide.N)
        val end1 = makeNode(4, 0, 0, NodeSide.S)
        val end2 = makeNode(0, 0, 4, NodeSide.E)
        val end3 = makeNode(4, 0, 4, NodeSide.W)
        val blocks = setOf(BlockPos(1, 0, 0), BlockPos(2, 0, 0))

        val circuits = CircuitScanner.buildCircuits(scanned, setOf(end1, end2, end3), blocks)

        assertEquals(3, circuits.size)
        assertTrue(circuits.all { it.nodeA == scanned })
        assertEquals(setOf(end1, end2, end3), circuits.map { it.nodeB }.toSet())
        assertTrue(circuits.all { it.errors.isEmpty() })
        assertTrue(circuits.map { it.segments.toSet() }.toSet().size == 1)
    }

    @Test
    fun `buildCircuits emits dangling circuit when no endpoints found`() {
        val scanned = makeNode(0, 0, 0, NodeSide.N)
        val blocks = setOf(BlockPos(1, 0, 0))

        val circuits = CircuitScanner.buildCircuits(scanned, emptySet(), blocks)

        assertEquals(1, circuits.size)
        val circuit = circuits.first()
        assertEquals(scanned, circuit.nodeA)
        assertNull(circuit.nodeB)
        assertEquals(listOf(ConnectionError.CIRCUIT_TO_CLOSED_NODE), circuit.errors)
    }

    @Test
    fun `circuits from opposite scans dedup in a set`() {
        val a = makeNode(0, 0, 0, NodeSide.N)
        val b = makeNode(4, 0, 0, NodeSide.S)
        val blocks = setOf(BlockPos(1, 0, 0), BlockPos(2, 0, 0))

        val fromA = CircuitScanner.buildCircuits(a, setOf(b), blocks)
        val fromB = CircuitScanner.buildCircuits(b, setOf(a), blocks)

        assertEquals(1, (fromA + fromB).toSet().size)
    }
}
