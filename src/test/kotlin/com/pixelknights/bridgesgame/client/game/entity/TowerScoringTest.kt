package com.pixelknights.bridgesgame.client.game.entity

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TowerScoringTest {

    // Base positions read from config/tower_color_layout.txt (uppercase letters = home bases)
    private val basePositions = mapOf(
        GameColor.RED to (0 to 3),
        GameColor.PURPLE to (0 to 9),
        GameColor.LIGHT_BLUE to (5 to 11),
        GameColor.GREEN to (10 to 9),
        GameColor.YELLOW to (10 to 3),
        GameColor.ORANGE to (5 to 0),
    )

    private val scoring = TowerScoring(basePositions)

    @Test
    fun `red own color scores 0`() {
        assertEquals(0, scoring.getCapturePoints(GameColor.RED, GameColor.RED))
    }

    @Test
    fun `red adjacent colors score 1`() {
        // RED home base is at (0,3); nearest neighbors are MAGENTA (0,9) and ORANGE (5,0)
        assertEquals(1, scoring.getCapturePoints(GameColor.RED, GameColor.PURPLE))
        assertEquals(1, scoring.getCapturePoints(GameColor.RED, GameColor.ORANGE))
    }

    @Test
    fun `red far colors score 2`() {
        // CYAN and YELLOW are the middle-distance bases from RED
        assertEquals(2, scoring.getCapturePoints(GameColor.RED, GameColor.LIGHT_BLUE))
        assertEquals(2, scoring.getCapturePoints(GameColor.RED, GameColor.YELLOW))
    }

    @Test
    fun `red opposite color scores 3`() {
        // GREEN is the farthest base from RED
        assertEquals(3, scoring.getCapturePoints(GameColor.RED, GameColor.GREEN))
    }

    @Test
    fun `cyan opposite color scores 3`() {
        // ORANGE is the farthest base from CYAN
        assertEquals(3, scoring.getCapturePoints(GameColor.LIGHT_BLUE, GameColor.ORANGE))
    }

    @Test
    fun `white center towers always score 2`() {
        GameColor.entries.filter { it.isTeam }.forEach { team ->
            assertEquals(2, scoring.getCapturePoints(team, GameColor.WHITE), "Expected WHITE=2 for team $team")
        }
    }

    @Test
    fun `grey neutral towers always score 1`() {
        GameColor.entries.filter { it.isTeam }.forEach { team ->
            assertEquals(1, scoring.getCapturePoints(team, GameColor.GREY), "Expected GREY=1 for team $team")
        }
    }
}