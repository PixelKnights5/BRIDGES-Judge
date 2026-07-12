package com.pixelknights.bridgesgame.client.util

import com.pixelknights.bridgesgame.FabricExtension
import com.pixelknights.bridgesgame.client.game.entity.GameColor
import net.minecraft.block.Blocks
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(FabricExtension::class)
class GameColorHelpersTest {

    @Test
    fun `team banners map to their team color`() {
        assertEquals(GameColor.RED, getTeamColorForBlock(Blocks.RED_BANNER))
        assertEquals(GameColor.ORANGE, getTeamColorForBlock(Blocks.ORANGE_BANNER))
        assertEquals(GameColor.YELLOW, getTeamColorForBlock(Blocks.YELLOW_BANNER))
        assertEquals(GameColor.PURPLE, getTeamColorForBlock(Blocks.PURPLE_BANNER))
        assertEquals(GameColor.LIGHT_BLUE, getTeamColorForBlock(Blocks.LIGHT_BLUE_BANNER))
    }

    @Test
    fun `green team uses lime banner, not green banner`() {
        assertEquals(GameColor.GREEN, getTeamColorForBlock(Blocks.LIME_BANNER))
        assertNull(getTeamColorForBlock(Blocks.GREEN_BANNER))
    }

    @Test
    fun `team glass and carpet still map to their team color`() {
        assertEquals(GameColor.RED, getTeamColorForBlock(Blocks.RED_STAINED_GLASS))
        assertEquals(GameColor.ORANGE, getTeamColorForBlock(Blocks.ORANGE_CARPET))
    }

    @Test
    fun `non-team blocks map to no team`() {
        assertNull(getTeamColorForBlock(Blocks.BLACK_BANNER))
        assertNull(getTeamColorForBlock(Blocks.STONE))
        assertNull(getTeamColorForBlock(null))
    }
}
