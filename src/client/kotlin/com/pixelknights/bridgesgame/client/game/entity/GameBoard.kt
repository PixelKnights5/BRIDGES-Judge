package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GameBoard private constructor(
    val centerCoordinate: BlockPos,
    var towers: List<List<Tower>>
) : KoinComponent {

    private val logger: Logger by inject()
    private val config: ModConfig by inject()

    init {

        // towerDiameter is not divided by 2, since we use half of each adjacent tower to reach te center.
        val spaceBetweenCenters = config.boardConfig.blocksBetweenTowers + config.boardConfig.towerDiameter


    }


    class Builder(
        private val center: BlockPos
    ) {

        // load towers

    }

}

