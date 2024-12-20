package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos

class Floor (
    val floorNumber: Int,
    val hasLadder: Boolean,
    val center: BlockPos
) {
    var claimColor: GameColor? = null
    var paintColor: GameColor? = null
}