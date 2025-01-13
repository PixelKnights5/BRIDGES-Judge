package com.pixelknights.bridgesgame.client.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.game.entity.GameBoard
import com.pixelknights.bridgesgame.client.game.entity.GameColor
import com.pixelknights.bridgesgame.client.render.DotRenderer
import com.pixelknights.bridgesgame.client.render.LineRenderer
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class JudgeGameCommand (
    private val gameBoard: GameBoard,
    private val dotRenderer: DotRenderer,
    private val lineRenderer: LineRenderer,
) : Command<FabricClientCommandSource>, KoinComponent {

    private val mc: MinecraftClient by inject()
    private val config: ModConfig by inject()

    override fun run(ctx: CommandContext<FabricClientCommandSource>): Int {
        val action = StringArgumentType.getString(ctx, "action")

        return when (action) {
            "scan" -> handleScanAction(ctx)
            "setCenterTower" -> handleSetCenterTowerAction(ctx)
            "clear" -> handleClearAction(ctx)
            "showPathLines" -> handlePathLineVisibility(ctx, true)
            "hidePathLines" -> handlePathLineVisibility(ctx, false)
            else -> commandNotImplemented(ctx)
        }
    }

    private fun handleScanAction(ctx: CommandContext<FabricClientCommandSource>): Int {
        val centerPosition = config.playerSettings.centerCoordinate

        dotRenderer.dotsToRender.clear()
        lineRenderer.linesToRender.clear()

        gameBoard.scanGame(centerPosition)
        ctx.source.sendFeedback(getScoreText())

        return 0
    }

    fun handleClearAction(ctx: CommandContext<FabricClientCommandSource>): Int {
        gameBoard.resetGame()
        ctx.source.sendFeedback(Text.of("Game state cleared"))
        return 0
    }

    private fun handlePathLineVisibility(ctx: CommandContext<FabricClientCommandSource>, isVisible: Boolean): Int {
        config.playerSettings.showBridgePaths = isVisible
        config.save()

        if (isVisible) {
            ctx.source.sendFeedback(Text.of("Showing path lines"))
        } else {
            ctx.source.sendFeedback(Text.of("Hiding path lines"))
        }

        return 0
    }

    private fun handleSetCenterTowerAction(ctx: CommandContext<FabricClientCommandSource>): Int {
        val playerPosition = mc.player?.pos

        if (playerPosition == null) {
            ctx.source.sendError(Text.of("Player position is null"))
            return -1
        }

        config.playerSettings.centerCoordinate = BlockPos.ofFloored(playerPosition)
        config.save()
        ctx.source.sendFeedback(Text.of("Center tower coordinates set to: ${config.playerSettings.centerCoordinate}"))

        return 0
    }

    private fun getScoreText(): Text {
        // https://minecraft.fandom.com/wiki/Formatting_codes
        val results = """
            §l§nBRIDGES Scores:§r 
            §l§cRed:
                Towers: ${gameBoard.teams[GameColor.RED]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.RED]?.points ?: "N/A"} §r
            §l§3Cyan:
                Towers: ${gameBoard.teams[GameColor.CYAN]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.CYAN]?.points ?: "N/A"} §r
            §l§eYellow:
                Towers: ${gameBoard.teams[GameColor.YELLOW]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.YELLOW]?.points ?: "N/A"} §r
            §l§2Green:
                Towers: ${gameBoard.teams[GameColor.GREEN]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.GREEN]?.points ?: "N/A"} §r
            §l§dMagenta:
                Towers: ${gameBoard.teams[GameColor.MAGENTA]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.MAGENTA]?.points ?: "N/A"} §r
            §l§6Orange:
                Towers: ${gameBoard.teams[GameColor.ORANGE]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.ORANGE]?.points ?: "N/A"} §r
        """.trimIndent()
        return Text.literal(results)
    }

    private fun commandNotImplemented(ctx: CommandContext<FabricClientCommandSource>): Int {
        ctx.source.sendError(Text.of("Not yet implemented"))
        return -999
    }

}
