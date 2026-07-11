package com.pixelknights.bridgesgame.client.util

import com.pixelknights.bridgesgame.client.game.entity.GameColor
import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.util.DyeColor

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

/**
 * Maps a dye color to its team, for team-colored blocks whose color is exposed as a single
 * DyeColor rather than encoded in a block id (e.g. a break banner's base color, read via
 * BannerBlockEntity.getColorForState()). Non-team dyes (including black) resolve to no team.
 */
fun getTeamColorForDye(dye: DyeColor?): GameColor? {
    if (dye == null) {
        return null
    }
    return GameColor.entries.firstOrNull { it.isTeam && it.blockColor == dye.asString() }
}