package com.pixelknights.bridgesgame.client.game.entity
import com.pixelknights.bridgesgame.client.config.BoardConfig
import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.config.TowerConfig
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TowerTest {

    @Test
    fun `validate tower world coords in negative x and negative z directions`() {
        val config = ModConfig(
            boardConfig = BoardConfig(
                width = 13,
                height = 11,
                blocksBetweenTowers = 5,
                towerDiameter = 5
            ),
            towerConfig = TowerConfig(),
        )

        val centerPos = BlockPos(534, 0, 314)
        // row=2, col=1: centerRow=5, centerCol=6; rowsFromCenter=-3, colsFromCenter=-5, spacing=10
        val expectedCoords = BlockPos(484, 0, 284)
        val tower = Tower(
            row = 2,
            column = 1,
            numFloors = 1,
            color = GameColor.GREY,
            isBase = false,
        )

        val actualCoords = tower.worldCoordinates(centerPos, config)
        assertEquals(expectedCoords, actualCoords)
    }

    @Test
    fun `validate tower world coords in positive x and positive z directions`() {

        val centerPos = BlockPos(534, 0, 314)
        // row=8, col=11: centerRow=5, centerCol=6; rowsFromCenter=3, colsFromCenter=5, spacing=10
        val expectedCoords = BlockPos(584, 0, 344)
        val tower = Tower(
            row = 8,
            column = 11,
            numFloors = 1,
            color = GameColor.GREY,
            isBase = false,
        )

        val actualCoords = tower.worldCoordinates(centerPos, config)
        assertEquals(expectedCoords, actualCoords)
    }

    @Test
    fun `tower is captured automatically once all floors are owned and validated by the same team`() {
        val tower = Tower(row = 0, column = 0, numFloors = 2, color = GameColor.GREY, isBase = false)
        tower.floors = listOf(
            floorOwnedBy(tower, floorNumber = 0, team = GameColor.RED, validated = true),
            floorOwnedBy(tower, floorNumber = 1, team = GameColor.RED, validated = true),
        )

        tower.setCapturingTeam()

        assertEquals(GameColor.RED, tower.capturingTeam)
    }

    @Test
    fun `tower is not captured when floors are owned by different teams`() {
        val tower = Tower(row = 0, column = 0, numFloors = 2, color = GameColor.GREY, isBase = false)
        tower.floors = listOf(
            floorOwnedBy(tower, floorNumber = 0, team = GameColor.RED, validated = true),
            floorOwnedBy(tower, floorNumber = 1, team = GameColor.ORANGE, validated = true),
        )

        tower.setCapturingTeam()

        assertEquals(null, tower.capturingTeam)
    }

    @Test
    fun `tower is not captured when a floor owned by the team is not validated`() {
        val tower = Tower(row = 0, column = 0, numFloors = 2, color = GameColor.GREY, isBase = false)
        tower.floors = listOf(
            floorOwnedBy(tower, floorNumber = 0, team = GameColor.RED, validated = true),
            floorOwnedBy(tower, floorNumber = 1, team = GameColor.RED, validated = false),
        )

        tower.setCapturingTeam()

        assertEquals(null, tower.capturingTeam)
    }

    @Test
    fun `tower is not captured when no floors are claimed`() {
        val tower = Tower(row = 0, column = 0, numFloors = 2, color = GameColor.GREY, isBase = false)
        tower.floors = listOf(
            floorOwnedBy(tower, floorNumber = 0, team = null, validated = false),
            floorOwnedBy(tower, floorNumber = 1, team = null, validated = false),
        )

        tower.setCapturingTeam()

        assertEquals(null, tower.capturingTeam)
    }

    private fun floorOwnedBy(tower: Tower, floorNumber: Int, team: GameColor?, validated: Boolean): Floor {
        val floor = Floor(
            floorNumber = floorNumber,
            tower = tower,
            worldCenter = BlockPos(0, 0, 0),
            captureColor = team,
            isBase = false,
        )
        floor.isCaptureValidated = validated
        return floor
    }

    companion object {
        private val config = ModConfig(
            boardConfig = BoardConfig(
                width = 13,
                height = 11,
                blocksBetweenTowers = 5,
                towerDiameter = 5
            ),
            towerConfig = TowerConfig(),
        )
    }

}