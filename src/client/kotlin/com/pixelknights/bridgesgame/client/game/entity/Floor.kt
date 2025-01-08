package com.pixelknights.bridgesgame.client.game.entity

import net.minecraft.util.math.BlockPos

data class Floor (
    val floorNumber: Int,
    val hasLadder: Boolean,
    val tower: Tower,
    /**
     * The location at node/ladder level, not ground level
     */
    val worldCenter: BlockPos,
    val captureColor: GameColor? = null,
    val paintColor: GameColor? = null,
) {

    val isPainted: Boolean
        get() = paintColor != null

    val owner: GameColor?
        get() = paintColor ?: captureColor

    var isCaptureValidated: Boolean? = null
    var isPaintValidated: Boolean? = null
    lateinit var nodes: List<Node>

    // TODO: The value '2' should be pulled from config. Thanks for fixing that, future me.
    val worldGround: BlockPos = worldCenter.down(2)


}