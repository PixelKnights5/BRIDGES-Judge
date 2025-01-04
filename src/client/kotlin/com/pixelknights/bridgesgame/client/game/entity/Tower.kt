package com.pixelknights.bridgesgame.client.game.entity

import com.google.common.base.Objects
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
    var isCaptureValidated: Boolean? = null
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

    /**
     * Calculate the points that a a tower is worth for a given team. Does not validate if the tower is captured.
     */
    fun getCapturePoints(team: GameColor): Int {
        return when (color) {
            GameColor.WHITE -> 3
            team -> 1
            else -> 2
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tower) return false

        if (row != other.row) return false
        if (column != other.column) return false

        return true
    }

    override fun hashCode() = Objects.hashCode(row, column)



}