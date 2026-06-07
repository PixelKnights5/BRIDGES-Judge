package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConnectionTest {

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
        // Create 9 nodes (8 perimeter + CENTER); none connected yet
        floor.nodes = NodeSide.entries.map { side ->
            Node(side = side, isOpen = side != NodeSide.CENTER, floor = floor, worldPosition = BlockPos(side.ordinal, num * 10, 0))
        }
        return floor
    }

    private fun Floor.centerNode() = nodes.first { it.side == NodeSide.CENTER }
    private fun Floor.perimeterNode(side: NodeSide) = nodes.first { it.side == side }

    private fun makeBaseTeamTower(color: GameColor): Tower {
        val tower = Tower(row = 0, column = 0, numFloors = 2, color = color, isBase = true)
        val floor0 = makeFloor(0, tower, captureColor = color, isBase = true)
        val floor1 = makeFloor(1, tower, captureColor = color)
        tower.floors = listOf(floor0, floor1)
        return tower
    }

    // ---- canTeamUse tests -----------------------------------------------

    @Test
    fun `Ladder canTeamUse returns true for any team when connected`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]
        val ladder = Ladder(nodeA = floor0.centerNode(), nodeB = floor1.centerNode(), segments = emptyList())

        assertTrue(ladder.canTeamUse(GameColor.ORANGE))
        assertTrue(ladder.canTeamUse(GameColor.RED))
        assertTrue(ladder.canTeamUse(GameColor.GREEN))
    }

    @Test
    fun `Ladder canTeamUse returns false when nodeB is null`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val ladder = Ladder(nodeA = floor0.centerNode(), nodeB = null, segments = emptyList())

        assertFalse(ladder.canTeamUse(GameColor.ORANGE))
    }

    @Test
    fun `Bridge canTeamUse allows only owner and painter`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]
        val nodeA = floor0.perimeterNode(NodeSide.N)
        val nodeB = floor1.perimeterNode(NodeSide.S)

        val bridge = Bridge(
            segments = emptyList(),
            nodeA = nodeA,
            nodeB = nodeB,
            owner = GameColor.ORANGE,
            painter = GameColor.RED,
        )

        assertTrue(bridge.canTeamUse(GameColor.ORANGE))
        assertTrue(bridge.canTeamUse(GameColor.RED))
        assertFalse(bridge.canTeamUse(GameColor.GREEN))
    }

    @Test
    fun `Bridge canTeamUse returns false when nodeB is null`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val nodeA = floor0.perimeterNode(NodeSide.N)

        val bridge = Bridge(
            segments = emptyList(),
            nodeA = nodeA,
            nodeB = null,
            owner = GameColor.ORANGE,
            painter = null,
        )

        assertFalse(bridge.canTeamUse(GameColor.ORANGE))
    }

    // ---- otherEnd tests -------------------------------------------------

    @Test
    fun `otherEnd returns correct opposite node`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]
        val n1 = floor0.centerNode()
        val n2 = floor1.centerNode()
        val ladder = Ladder(nodeA = n1, nodeB = n2, segments = emptyList())

        assertEquals(n2, ladder.otherEnd(n1))
        assertEquals(n1, ladder.otherEnd(n2))
    }

    @Test
    fun `otherEnd returns null for unrelated node`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]
        val n1 = floor0.centerNode()
        val n2 = floor1.centerNode()
        val unrelated = floor0.perimeterNode(NodeSide.N)
        val ladder = Ladder(nodeA = n1, nodeB = n2, segments = emptyList())

        assertNull(ladder.otherEnd(unrelated))
    }

    // ---- equals/hashCode commutative ------------------------------------

    @Test
    fun `Ladder equals is commutative`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]
        val n1 = floor0.centerNode()
        val n2 = floor1.centerNode()

        val ladder1 = Ladder(nodeA = n1, nodeB = n2, segments = emptyList())
        val ladder2 = Ladder(nodeA = n2, nodeB = n1, segments = emptyList())

        assertEquals(ladder1, ladder2)
        assertEquals(ladder1.hashCode(), ladder2.hashCode())
    }

    @Test
    fun `Bridge equals is commutative`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val n1 = tower.floors[0].perimeterNode(NodeSide.N)
        val n2 = tower.floors[1].perimeterNode(NodeSide.S)

        val bridge1 = Bridge(segments = emptyList(), nodeA = n1, nodeB = n2, owner = GameColor.ORANGE, painter = null)
        val bridge2 = Bridge(segments = emptyList(), nodeA = n2, nodeB = n1, owner = GameColor.ORANGE, painter = null)

        assertEquals(bridge1, bridge2)
        assertEquals(bridge1.hashCode(), bridge2.hashCode())
    }

    // ---- Path traversal tests -------------------------------------------

    @Test
    fun `buildPath traverses ladder connecting two floors`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]

        val ladder = Ladder(nodeA = floor0.centerNode(), nodeB = floor1.centerNode(), segments = emptyList())
        floor0.centerNode().connections += ladder
        floor1.centerNode().connections += ladder

        val path = Path(pathOwner = GameColor.ORANGE, scoring = stubScoring)
        path.buildPath(floor0, listOf(tower))

        assertTrue(floor0 in path.floors, "floor0 should be in path")
        assertTrue(floor1 in path.floors, "floor1 should be in path")
        assertTrue(ladder in path.connections, "ladder should be in path connections")
    }

    @Test
    fun `buildPath validates owned floors when base is reachable`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]

        val ladder = Ladder(nodeA = floor0.centerNode(), nodeB = floor1.centerNode(), segments = emptyList())
        floor0.centerNode().connections += ladder
        floor1.centerNode().connections += ladder

        val path = Path(pathOwner = GameColor.ORANGE, scoring = stubScoring)
        path.buildPath(floor0, listOf(tower))

        assertTrue(floor0.isCaptureValidated == true, "floor0 should be capture-validated")
        assertTrue(floor1.isCaptureValidated == true, "floor1 should be capture-validated")
    }

    @Test
    fun `buildPath skips bridge where nodeB is null (BRIDGE_TO_CLOSED_NODE)`() {
        // When BridgeScanner sets BRIDGE_TO_CLOSED_NODE, nodeB is always null —
        // canTeamUse returns false, so the bridge is never traversed.
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]

        val brokenBridge = Bridge(
            segments = emptyList(),
            nodeA = floor0.perimeterNode(NodeSide.N),
            nodeB = null,
            owner = GameColor.ORANGE,
            painter = null,
            errors = listOf(ConnectionError.BRIDGE_TO_CLOSED_NODE),
        )
        floor0.perimeterNode(NodeSide.N).connections += brokenBridge

        val path = Path(pathOwner = GameColor.ORANGE, scoring = stubScoring)
        path.buildPath(floor0, listOf(tower))

        // Only floor0 is reachable; the broken bridge leads nowhere
        assertEquals(setOf(floor0), path.floors)
    }

    @Test
    fun `buildPath does not reach floor without connection`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]
        val floor1 = tower.floors[1]
        // No ladder or bridge added — floor1 is isolated

        val path = Path(pathOwner = GameColor.ORANGE, scoring = stubScoring)
        path.buildPath(floor0, listOf(tower))

        assertTrue(floor0 in path.floors)
        assertFalse(floor1 in path.floors, "floor1 has no connection so should not be reachable")
        assertFalse(floor1.isCaptureValidated == true, "disconnected floor1 should not be validated")
    }

    @Test
    fun `CENTER node is present on each floor with worldPosition matching worldCenter`() {
        val tower = makeBaseTeamTower(GameColor.ORANGE)
        val floor0 = tower.floors[0]

        val centerNode = floor0.nodes.first { it.side == NodeSide.CENTER }
        assertEquals(floor0.worldCenter, centerNode.worldPosition)
    }
}
