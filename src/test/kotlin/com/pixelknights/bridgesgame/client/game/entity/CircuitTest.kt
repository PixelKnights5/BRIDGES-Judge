package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CircuitTest {

    private val stubScoring = TowerScoring(emptyMap())

    // ---- helpers --------------------------------------------------------

    private fun makeFloor(
        num: Int,
        tower: Tower,
        captureColor: GameColor? = null,
        isBase: Boolean = false,
    ): Floor {
        val floor = Floor(
            floorNumber = num,
            tower = tower,
            worldCenter = BlockPos(0, num * 10, 0),
            captureColor = captureColor,
            isBase = isBase,
        )
        floor.nodes = NodeSide.entries.map { side ->
            Node(side = side, isOpen = side != NodeSide.CENTER, floor = floor, worldPosition = BlockPos(side.ordinal, num * 10, 0))
        }
        return floor
    }

    private fun Floor.perimeterNode(side: NodeSide) = nodes.first { it.side == side }
    private fun Floor.centerNode() = nodes.first { it.side == NodeSide.CENTER }

    private fun makeBaseTeamTower(color: GameColor): Tower {
        val tower = Tower(row = 0, column = 0, numFloors = 2, color = color, isBase = true)
        val floor0 = makeFloor(0, tower, captureColor = color, isBase = true)
        val floor1 = makeFloor(1, tower, captureColor = color)
        tower.floors = listOf(floor0, floor1)
        return tower
    }

    private fun makeThreeFloorTower(color: GameColor, floor1Color: GameColor?, floor2Color: GameColor?): Tower {
        val tower = Tower(row = 0, column = 0, numFloors = 3, color = color, isBase = true)
        val floor0 = makeFloor(0, tower, captureColor = color, isBase = true)
        val floor1 = makeFloor(1, tower, captureColor = floor1Color)
        val floor2 = makeFloor(2, tower, captureColor = floor2Color)
        tower.floors = listOf(floor0, floor1, floor2)
        return tower
    }

    // ---- canTeamUse tests -----------------------------------------------

    @Test
    fun `Circuit canTeamUse returns true for any team when connected`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]
        val circuit = Circuit(
            nodeA = floor0.perimeterNode(NodeSide.N),
            nodeB = floor1.perimeterNode(NodeSide.S),
            segments = emptyList(),
        )

        assertTrue(circuit.canTeamUse(GameColor.ORANGE))
        assertTrue(circuit.canTeamUse(GameColor.RED))
        assertTrue(circuit.canTeamUse(GameColor.GREEN))
    }

    @Test
    fun `Circuit canTeamUse returns false when nodeB is null`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val circuit = Circuit(
            nodeA = floor0.perimeterNode(NodeSide.N),
            nodeB = null,
            segments = emptyList(),
            errors = listOf(ConnectionError.CIRCUIT_TO_CLOSED_NODE),
        )

        assertFalse(circuit.canTeamUse(GameColor.ORANGE))
        assertFalse(circuit.canTeamUse(GameColor.RED))
    }

    @Test
    fun `Circuit owner and painter are always null`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val circuit = Circuit(
            nodeA = floor0.perimeterNode(NodeSide.N),
            nodeB = null,
            segments = emptyList(),
        )

        assertEquals(null, circuit.owner)
        assertEquals(null, circuit.painter)
    }

    // ---- equals/hashCode commutative ------------------------------------

    @Test
    fun `Circuit equals is commutative`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val n1 = tower.floors[0].perimeterNode(NodeSide.N)
        val n2 = tower.floors[1].perimeterNode(NodeSide.S)

        val circuit1 = Circuit(nodeA = n1, nodeB = n2, segments = emptyList())
        val circuit2 = Circuit(nodeA = n2, nodeB = n1, segments = emptyList())

        assertEquals(circuit1, circuit2)
        assertEquals(circuit1.hashCode(), circuit2.hashCode())
    }

    // ---- Path traversal tests -------------------------------------------

    @Test
    fun `buildPath traverses circuit connecting two floors`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]

        val nodeA = floor0.perimeterNode(NodeSide.N)
        val nodeB = floor1.perimeterNode(NodeSide.S)
        val circuit = Circuit(nodeA = nodeA, nodeB = nodeB, segments = emptyList())
        nodeA.connections += circuit
        nodeB.connections += circuit

        val path = Path(pathOwner = GameColor.ORANGE, scoring = stubScoring)
        path.buildPath(floor0, listOf(tower))

        assertTrue(floor0 in path.floors, "floor0 should be in path")
        assertTrue(floor1 in path.floors, "floor1 should be in path")
        assertTrue(circuit in path.connections, "circuit should be in path connections")
    }

    @Test
    fun `buildPath skips circuit where nodeB is null (CIRCUIT_TO_CLOSED_NODE)`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]

        val danglingCircuit = Circuit(
            nodeA = floor0.perimeterNode(NodeSide.N),
            nodeB = null,
            segments = emptyList(),
            errors = listOf(ConnectionError.CIRCUIT_TO_CLOSED_NODE),
        )
        floor0.perimeterNode(NodeSide.N).connections += danglingCircuit

        val path = Path(pathOwner = GameColor.ORANGE, scoring = stubScoring)
        path.buildPath(floor0, listOf(tower))

        assertEquals(setOf(floor0), path.floors)
    }

    // ---- multi-node circuit traversal (allowMultiNodeCircuits) -----------

    @Test
    fun `buildPath traverses all branches of a multi-node circuit when allowMultiNodeCircuits is true`() {
        val tower = makeThreeFloorTower(color = GameColor.ORANGE, floor1Color = GameColor.ORANGE, floor2Color = null)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]
        val floor2 = tower.floors[2]

        val nodeA = floor0.perimeterNode(NodeSide.N)
        val nodeB = floor1.perimeterNode(NodeSide.S)
        val nodeC = floor2.perimeterNode(NodeSide.E)

        // K3 network: every pair of nodeA/nodeB/nodeC is directly connected, mirroring one
        // sculk network touching all three nodes at once.
        val circuitAB = Circuit(nodeA = nodeA, nodeB = nodeB, segments = emptyList())
        val circuitBC = Circuit(nodeA = nodeB, nodeB = nodeC, segments = emptyList())
        val circuitAC = Circuit(nodeA = nodeA, nodeB = nodeC, segments = emptyList())
        nodeA.connections += circuitAB
        nodeA.connections += circuitAC
        nodeB.connections += circuitAB
        nodeB.connections += circuitBC
        nodeC.connections += circuitBC
        nodeC.connections += circuitAC

        val path = Path(pathOwner = GameColor.ORANGE, scoring = stubScoring, allowMultiNodeCircuits = true)
        path.buildPath(floor0, listOf(tower))

        assertEquals(setOf(floor0, floor1, floor2), path.floors)
        assertEquals(setOf<Connection>(circuitAB, circuitBC, circuitAC), path.connections)
    }

    @Test
    fun `buildPath picks a single circuit branch when allowMultiNodeCircuits is false`() {
        val tower = makeThreeFloorTower(color = GameColor.ORANGE, floor1Color = GameColor.ORANGE, floor2Color = null)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]
        val floor2 = tower.floors[2]

        val nodeA = floor0.perimeterNode(NodeSide.N)
        val nodeToFloor1 = floor1.perimeterNode(NodeSide.S)
        val nodeToFloor2 = floor2.perimeterNode(NodeSide.E)

        // floor1 is captured by ORANGE (scores) while floor2 is not, so choose-best must
        // deterministically prefer the branch to floor1.
        val circuitToFloor1 = Circuit(nodeA = nodeA, nodeB = nodeToFloor1, segments = emptyList())
        val circuitToFloor2 = Circuit(nodeA = nodeA, nodeB = nodeToFloor2, segments = emptyList())
        nodeA.connections += circuitToFloor1
        nodeA.connections += circuitToFloor2
        nodeToFloor1.connections += circuitToFloor1
        nodeToFloor2.connections += circuitToFloor2

        val path = Path(pathOwner = GameColor.ORANGE, scoring = stubScoring, allowMultiNodeCircuits = false)
        path.buildPath(floor0, listOf(tower))

        assertEquals(setOf(floor0, floor1), path.floors)
        assertEquals(setOf<Connection>(circuitToFloor1), path.connections)
    }

    @Test
    fun `buildPath with allowMultiNodeCircuits skips dangling circuit branch`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]

        val nodeA = floor0.perimeterNode(NodeSide.N)
        val nodeB = floor1.perimeterNode(NodeSide.S)

        val validCircuit = Circuit(nodeA = nodeA, nodeB = nodeB, segments = emptyList())
        val danglingCircuit = Circuit(
            nodeA = nodeA,
            nodeB = null,
            segments = emptyList(),
            errors = listOf(ConnectionError.CIRCUIT_TO_CLOSED_NODE),
        )
        nodeA.connections += validCircuit
        nodeA.connections += danglingCircuit
        nodeB.connections += validCircuit

        val path = Path(pathOwner = GameColor.ORANGE, scoring = stubScoring, allowMultiNodeCircuits = true)
        path.buildPath(floor0, listOf(tower))

        assertEquals(setOf(floor0, floor1), path.floors)
        assertEquals(setOf<Connection>(validCircuit), path.connections)
    }
}