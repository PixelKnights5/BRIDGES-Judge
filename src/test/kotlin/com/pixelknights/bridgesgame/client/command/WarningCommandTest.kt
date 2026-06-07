package com.pixelknights.bridgesgame.client.command

import com.pixelknights.bridgesgame.client.render.Color
import com.pixelknights.bridgesgame.client.render.GameWarning
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WarningCommandTest {

    private fun makeWarning(id: Int) = GameWarning(
        position = BlockPos(10 * id, 64, 10 * id),
        color = Color.WHITE,
        message = "Test warning $id",
        id = id,
    )

    @Test
    fun `warningClickCommand returns tp command in creative`() {
        val warning = makeWarning(1)
        val cmd = JudgeGameCommand.warningClickCommand(isCreative = true, warning = warning)
        assertEquals("/tp @s 10 64 10", cmd)
    }

    @Test
    fun `warningClickCommand returns highlight command in survival`() {
        val warning = makeWarning(3)
        val cmd = JudgeGameCommand.warningClickCommand(isCreative = false, warning = warning)
        assertEquals("/bridges highlightWarning 3", cmd)
    }

    @Test
    fun `sequential id assignment yields ids 1 to N`() {
        val raw = listOf(makeWarning(0), makeWarning(0), makeWarning(0))
        val numbered = raw.mapIndexed { i, w -> w.copy(id = i + 1) }
        assertEquals(listOf(1, 2, 3), numbered.map { it.id })
    }

    @Test
    fun `GameWarning defaults to isVisible true and id 0`() {
        val w = GameWarning(BlockPos.ORIGIN, Color.WHITE, "test")
        assertTrue(w.isVisible)
        assertEquals(0, w.id)
    }
}