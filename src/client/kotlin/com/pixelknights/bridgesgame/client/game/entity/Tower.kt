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

    var floors: List<Floor> = mutableListOf()
    var capturingTeam: GameColor? = null

    var coords: String = "${'A' + column}${1 + row}"

    fun worldCoordinates(centerTowerPos: BlockPos, config: ModConfig): BlockPos {
        val centerTowerRow = config.boardConfig.height / 2
        val centerTowerCol = config.boardConfig.width / 2
        val rowsFromCenter = row - centerTowerRow
        val colsFromCenter = column - centerTowerCol
        val worldX = centerTowerPos.x + (config.spaceBetweenCenters * colsFromCenter)
        val worldZ = centerTowerPos.z + (config.spaceBetweenCenters * rowsFromCenter)

        return BlockPos(worldX, centerTowerPos.y, worldZ)
    }

    fun setCapturingTeam() {
        val firstColor = floors.firstOrNull()?.captureColor
        if (firstColor == null) {
            return
        }

        val areAllFloorsCaptured = floors.all { it.owner == firstColor && it.isOwnerValidated }

        capturingTeam = if (areAllFloorsCaptured) {
            firstColor
        } else {
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Tower) {
            return false
        }

        if (row != other.row) {
            return false
        }
        if (column != other.column) {
            return false
        }

        return true
    }

    override fun hashCode() = Objects.hashCode(row, column)

    override fun toString(): String {
        return "Tower(coords=$coords)"
    }

}