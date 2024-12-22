package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.config.TowerLayoutConfig
import com.pixelknights.bridgesgame.client.game.entity.scanner.TowerScanner
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.Logger
import org.koin.core.component.KoinComponent

class GameBoard constructor(
    private val logger: Logger,
    private val config: ModConfig,
    private val layout: TowerLayoutConfig,
    private val towerScanner: TowerScanner
) : KoinComponent {


    private var towers: List<List<Tower>> = mutableListOf<MutableList<Tower>>()


    fun scanGame(centerCoordinate: BlockPos) {
        towers = towerScanner.getTowers(centerCoordinate)
    }

}

