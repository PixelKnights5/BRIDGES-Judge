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

    val isCaptured: Boolean
        get() = captureColor != null

    val isPainted: Boolean
        get() = paintColor != null

    val owner: GameColor?
        get() = paintColor ?: captureColor

    val isOwnerValidated: Boolean
        get() {
            if (owner == null) {
                return false
            }
            return if (isPainted) {
                isPaintValidated == true
            } else {
                isCaptureValidated == true
            }
        }

    var isCaptureValidated: Boolean? = null
    var isPaintValidated: Boolean? = null
    lateinit var nodes: List<Node>

    // TODO: The value '2' should be pulled from config. Thanks for fixing that, future me.
    val worldGround: BlockPos = worldCenter.down(2)

    val coords: String = "$floorNumber${tower.coords}"
    val worldCoords: String = "(${worldGround.x}, ${worldGround.y}, ${worldGround.z})"

    override fun toString(): String {
        return "Floor(coords=$coords, tower=$tower, worldCoords=$worldCoords)"
    }

}