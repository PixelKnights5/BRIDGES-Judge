package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NodeTest {

    private fun makeFloor(captureColor: GameColor? = null, paintColor: GameColor? = null): Floor {
        val tower = Tower(row = 0, column = 0, numFloors = 1, color = GameColor.WHITE, isBase = false)
        return Floor(
            floorNumber = 0,
            tower = tower,
            worldCenter = BlockPos(0, 0, 0),
            captureColor = captureColor,
            paintColor = paintColor,
            isBase = false,
        )
    }

    private fun makeNode(floor: Floor, brokenByTeam: GameColor?): Node {
        return Node(
            side = NodeSide.N,
            isOpen = true,
            floor = floor,
            worldPosition = BlockPos(0, 0, 0),
            brokenByTeam = brokenByTeam,
        )
    }

    @Test
    fun `break by the floor's capturing team is valid`() {
        val floor = makeFloor(captureColor = GameColor.RED)
        val node = makeNode(floor, brokenByTeam = GameColor.RED)

        assertTrue(node.isValidBreak)
    }

    @Test
    fun `break by the floor's painting team is valid`() {
        val floor = makeFloor(captureColor = GameColor.RED, paintColor = GameColor.LIGHT_BLUE)
        val node = makeNode(floor, brokenByTeam = GameColor.LIGHT_BLUE)

        assertTrue(node.isValidBreak)
    }

    @Test
    fun `break by a team that does not own the floor is invalid`() {
        val floor = makeFloor(captureColor = GameColor.RED)
        val node = makeNode(floor, brokenByTeam = GameColor.LIGHT_BLUE)

        assertFalse(node.isValidBreak)
    }

    @Test
    fun `break on an unowned floor is invalid`() {
        val floor = makeFloor()
        val node = makeNode(floor, brokenByTeam = GameColor.RED)

        assertFalse(node.isValidBreak)
    }

    @Test
    fun `a node that was not broken is not a valid break`() {
        val floor = makeFloor(captureColor = GameColor.RED)
        val node = makeNode(floor, brokenByTeam = null)

        assertFalse(node.isValidBreak)
    }
}
