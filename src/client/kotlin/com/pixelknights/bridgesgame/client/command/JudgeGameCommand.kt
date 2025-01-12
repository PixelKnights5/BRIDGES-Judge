package com.pixelknights.bridgesgame.client.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.pixelknights.bridgesgame.client.game.entity.GameBoard
import com.pixelknights.bridgesgame.client.game.entity.GameColor
import com.pixelknights.bridgesgame.client.render.DotRenderer
import com.pixelknights.bridgesgame.client.render.LineRenderer
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.CompletableFuture


class JudgeGameCommand (
    private val gameBoard: GameBoard,
    private val dotRenderer: DotRenderer,
    private val lineRenderer: LineRenderer,
) : Command<FabricClientCommandSource>, KoinComponent {

    private val mc: MinecraftClient by inject()

    override fun run(ctx: CommandContext<FabricClientCommandSource>): Int {
        val action = StringArgumentType.getString(ctx, "action")

        return when (action) {
            "scan" -> handleScanAction(ctx)
            else -> -999
        }
    }

    private fun handleScanAction(ctx: CommandContext<FabricClientCommandSource>): Int {
        val playerPosition = Vec3d(279.0, -35.0, 159.0)

        dotRenderer.dotsToRender.clear()
        lineRenderer.linesToRender.clear()

        playerPosition.let {
            gameBoard.scanGame(BlockPos.ofFloored(playerPosition))
            ctx.source.sendFeedback(getScoreText())
        }

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

}

object ActionCompletionProvider : SuggestionProvider<FabricClientCommandSource> {
    override fun getSuggestions(
        context: CommandContext<FabricClientCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        arrayOf(
            "set",
            "clear",
            "scan",
        ).forEach { builder.suggest(it) }

        return builder.buildFuture()
    }

}
