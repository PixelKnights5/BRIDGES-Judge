package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos

data class Floor (
    val floorNumber: Int,
    val hasLadder: Boolean,
    val worldCenter: BlockPos,
    val captureColor: GameColor? = null,
    val paintColor: GameColor? = null,
) {

    var isCaptureValidated: Boolean? = null
    var isPaintValidated: Boolean? = null

}