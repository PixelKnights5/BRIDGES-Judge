package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.config.TowerLayoutConfig
import com.pixelknights.bridgesgame.client.game.entity.Tower
import net.minecraft.util.math.BlockPos
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TowerScanner(
    private val config: ModConfig,
    private val floorScanner: FloorScanner
): KoinComponent {

    private val layout: TowerLayoutConfig by inject()

    private fun getTower(row: Int, col: Int, centerCoordinate: BlockPos): Tower? {
        val numFloors = layout.getHeight(row, col) ?: return null
        val color = layout.getColor(row, col) ?: return null
        val isBase = layout.isTeamBase(row, col)

        val tower = Tower(
            row = row,
            column = col,
            numFloors = numFloors,
            color = color,
            isBase = isBase,
        )

        tower.floors = (0 until numFloors).map { floor ->
            floorScanner.getFloor(tower, centerCoordinate, floor)
        }.toList()

        return tower
    }

    fun getTowers(centerCoordinate: BlockPos): List<List<Tower>> {
        val towerList = mutableListOf<MutableList<Tower>>()
        for (row in 0..<config.boardConfig.height) {
            val rowTowers = mutableListOf<Tower>()
            for (col in 0..<config.boardConfig.width) {
                getTower(row, col, centerCoordinate)
                    ?.let { rowTowers.add(it) }
            }
            towerList.add(rowTowers)
        }

        return towerList
    }

}