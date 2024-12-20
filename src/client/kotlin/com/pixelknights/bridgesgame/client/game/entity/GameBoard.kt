package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import net.minecraft.util.math.BlockPos

class GameBoard private constructor(
    val centerCoordinate: BlockPos,
    var towers: List<List<Tower>>
) {



    init {

        val config = ModConfig.INSTANCE

        // towerDiameter is not divided by 2, since we use half of each adjacent tower to reach te center.
        val spaceBetweenCenters = config.boardConfig.blocksBetweenTowers + config.boardConfig.towerDiameter


    }


    class Builder(
        private val center: BlockPos
    ) {

        // load towers

    }

}

