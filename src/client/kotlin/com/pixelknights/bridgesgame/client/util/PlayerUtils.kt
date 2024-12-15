package com.pixelknights.bridgesgame.client.util

import com.pixelknights.bridgesgame.client.MOD_LOGGER
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos

/**
 * Get the block that the player is standing on.
 *
 * @return The block that the player is standing on, or null if the player is not standing on a block.
 */
fun getBlockBelowPlayer(): BlockState? {
    val client = MinecraftClient.getInstance()
    val playerPosition = client.player?.pos
    if (playerPosition != null) {
        val block = client.world?.getBlockState(BlockPos.ofFloored(playerPosition.x, playerPosition.y - 1, playerPosition.z))
        return block
    } else {
        MOD_LOGGER.info("Could not get block below player - Player was null")
    }

    return null
}