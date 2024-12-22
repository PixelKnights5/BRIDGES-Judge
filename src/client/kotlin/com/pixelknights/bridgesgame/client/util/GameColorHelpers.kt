package com.pixelknights.bridgesgame.client.util

import com.pixelknights.bridgesgame.client.game.entity.GameColor
import net.minecraft.block.Block
import net.minecraft.registry.Registries

fun getTeamColorForBlock(block: Block?): GameColor? {
    if (block == null) {
        return null
    }

    val blockId = Registries.BLOCK.getId(block).toString()
    if (!blockId.containsAny("glass", "carpet")) {
        return null
    }

    return GameColor.entries
        .firstOrNull {
            // contains() can't handle nullables, so default to a string that will cause the result to be false.
            blockId.contains(it.blockColor ?: "!!NO_MATCH!!")
        }
}