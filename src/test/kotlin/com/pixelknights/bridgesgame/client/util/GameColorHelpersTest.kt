package com.pixelknights.bridgesgame.client.util

import com.pixelknights.bridgesgame.client.game.entity.GameColor
import net.minecraft.util.DyeColor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameColorHelpersTest {

    @Test
    fun `team dyes map to their team color`() {
        assertEquals(GameColor.RED, getTeamColorForDye(DyeColor.RED))
        assertEquals(GameColor.ORANGE, getTeamColorForDye(DyeColor.ORANGE))
        assertEquals(GameColor.YELLOW, getTeamColorForDye(DyeColor.YELLOW))
        assertEquals(GameColor.PURPLE, getTeamColorForDye(DyeColor.PURPLE))
        assertEquals(GameColor.LIGHT_BLUE, getTeamColorForDye(DyeColor.LIGHT_BLUE))
    }

    @Test
    fun `green team uses lime dye, not green dye`() {
        assertEquals(GameColor.GREEN, getTeamColorForDye(DyeColor.LIME))
        assertNull(getTeamColorForDye(DyeColor.GREEN))
    }

    @Test
    fun `non-team dyes map to no team`() {
        assertNull(getTeamColorForDye(DyeColor.BLACK))
        assertNull(getTeamColorForDye(DyeColor.BLUE))
        assertNull(getTeamColorForDye(DyeColor.WHITE))
        assertNull(getTeamColorForDye(null))
    }
}
