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
                width = 19,
                height = 19,
                blocksBetweenTowers = 5,
                towerDiameter = 5
            ),
            towerConfig = TowerConfig(),
        )

        val centerPos = BlockPos(534, 0, 314)
        val expectedCoords = BlockPos(454, 0, 264)
        val tower = Tower(
            row = 4,
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
        val expectedCoords = BlockPos(614, 0, 364)
        val tower = Tower(
            row = 14,
            column = 17,
            numFloors = 1,
            color = GameColor.GREY,
            isBase = false,
        )

        val actualCoords = tower.worldCoordinates(centerPos, config)
        assertEquals(expectedCoords, actualCoords)
    }

    companion object {
        private val config = ModConfig(
            boardConfig = BoardConfig(
                width = 19,
                height = 19,
                blocksBetweenTowers = 5,
                towerDiameter = 5
            ),
            towerConfig = TowerConfig(),
        )
    }

}