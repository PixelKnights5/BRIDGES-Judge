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
 * Break banners are black banners with a team-colored pattern applied, so the team is identified by
 * dye color rather than by the banner's own (black) block id.
 */
fun getTeamColorForDye(dye: DyeColor?): GameColor? {
    if (dye == null) {
        return null
    }
    return GameColor.entries.firstOrNull { it.isTeam && it.blockColor == dye.asString() }
}

/**
 * Distinct team colors referenced by a banner's pattern layers. The black base color and any
 * non-team dye (e.g. plain green, which is not a team color - team green uses lime) are ignored.
 */
fun getTeamColorsForBannerLayers(patternColors: List<DyeColor>): Set<GameColor> =
    patternColors.mapNotNull { getTeamColorForDye(it) }.toSet()