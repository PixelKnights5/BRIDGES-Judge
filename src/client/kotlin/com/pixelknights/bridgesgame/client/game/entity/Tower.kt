package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import net.minecraft.util.math.BlockPos

class Tower(
    val row: Int,
    val column: Int,
    val numFloors: Int,
    val color: GameColor,
    val isBase: Boolean,
) {

    var capturingTeam: Team? = null
    var floors: List<Floor> = mutableListOf()


    fun worldCoordinates(centerTowerPos: BlockPos, config: ModConfig): BlockPos {
        val centerTowerRow = config.boardConfig.width / 2
        val centerTowerCol = config.boardConfig.height / 2
        val rowsFromCenter = row - centerTowerRow
        val colsFromCenter = column - centerTowerCol
        val worldX = centerTowerPos.x + (config.spaceBetweenCenters * colsFromCenter)
        val worldZ = centerTowerPos.z + (config.spaceBetweenCenters * rowsFromCenter)

        return BlockPos(worldX, centerTowerPos.y, worldZ)
    }

}