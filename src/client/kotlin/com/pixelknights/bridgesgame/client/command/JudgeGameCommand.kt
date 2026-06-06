package com.pixelknights.bridgesgame.client.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.di.Scopes
import com.pixelknights.bridgesgame.client.game.ScanState
import com.pixelknights.bridgesgame.client.game.entity.GameBoard
import com.pixelknights.bridgesgame.client.game.entity.GameColor
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.apache.logging.log4j.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class JudgeGameCommand(
    private val gameBoard: GameBoard,
) : Command<FabricClientCommandSource>, KoinComponent {

    private val mc: MinecraftClient by inject()
    private val config: ModConfig by inject()
    private val logger: Logger by inject()
    private val scanState: ScanState by inject()
    private val scanScope: CoroutineScope by inject(named(Scopes.BridgesScanScope))

    override fun run(ctx: CommandContext<FabricClientCommandSource>): Int {
        val action = StringArgumentType.getString(ctx, "action")

        return when (action) {
            "scan" -> handleScanAction(ctx)
            "setCenterTower" -> handleSetCenterTowerAction(ctx)
            "clear" -> handleClearAction(ctx)
            "showPathLines" -> handlePathLineVisibility(ctx, true)
            "hidePathLines" -> handlePathLineVisibility(ctx, false)
            "showTowerText" -> handleTowerStateVisibility(ctx, true)
            "hideTowerText" -> handleTowerStateVisibility(ctx, false)
            else -> commandNotImplemented(ctx)
        }
    }

    private fun handleScanAction(ctx: CommandContext<FabricClientCommandSource>): Int {
        if (!scanState.isScanning.compareAndSet(false, true)) {
            ctx.source.sendError(Text.of("A scan is already in progress."))
            return -1
        }

        val centerPosition = config.playerSettings.centerCoordinate
        scanScope.launch {
            try {
                val result = gameBoard.scanGame(centerPosition)
                mc.execute {
                    if (mc.player == null) {
                        logger.warn("Player disconnected before scan result could be applied")
                        return@execute
                    }
                    gameBoard.applyScanResult(result)
                    result.errors.forEach { ctx.source.sendError(Text.of("WARNING: $it")) }
                    ctx.source.sendFeedback(getScoreText())
                    if (result.errors.isNotEmpty()) {
                        ctx.source.sendError(Text.of("${result.errors.size} warnings detected (see above scores)"))
                    }
                }
            } catch (t: Throwable) {
                logger.error("Scan failed", t)
                mc.execute { ctx.source.sendError(Text.of("Scan failed: ${t.message}")) }
            } finally {
                scanState.isScanning.set(false)
            }
        }

        return 0
    }

    fun handleClearAction(ctx: CommandContext<FabricClientCommandSource>): Int {
        if (scanState.isScanning.get()) {
            ctx.source.sendError(Text.of("Cannot clear while a scan is in progress."))
            return -1
        }
        gameBoard.resetGame()
        ctx.source.sendFeedback(Text.of("Game state cleared"))
        return 0
    }

    private fun handleTowerStateVisibility(ctx: CommandContext<FabricClientCommandSource>, isVisible: Boolean): Int {
        config.playerSettings.showTowerState = isVisible
        config.save()

        if (isVisible) {
            ctx.source.sendFeedback(Text.of("Showing tower text"))
        } else {
            ctx.source.sendFeedback(Text.of("Hiding tower text"))
        }

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
        val playerPosition = mc.player?.blockPos

        if (playerPosition == null) {
            ctx.source.sendError(Text.of("Player position is null"))
            return -1
        }

        config.playerSettings.centerCoordinate = playerPosition
        config.save()
        val coordinate = config.playerSettings.centerCoordinate
        ctx.source.sendFeedback(Text.of("Center tower coordinates set to: (${coordinate.x}, ${coordinate.y}, ${coordinate.z})"))

        return 0
    }

    private fun getScoreText(): Text {
        // https://minecraft.fandom.com/wiki/Formatting_codes
        val results = """

            §l§nBRIDGES Scores:§r
            §l§cRed:
                Towers: ${gameBoard.teams[GameColor.RED]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.RED]?.points ?: "N/A"}
                Moves: ${gameBoard.teams[GameColor.RED]?.moves ?: "N/A"} §r
            §l§9Blue:
                Towers: ${gameBoard.teams[GameColor.LIGHT_BLUE]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.LIGHT_BLUE]?.points ?: "N/A"}
                Moves: ${gameBoard.teams[GameColor.LIGHT_BLUE]?.moves ?: "N/A"} §r
            §l§eYellow:
                Towers: ${gameBoard.teams[GameColor.YELLOW]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.YELLOW]?.points ?: "N/A"}
                Moves: ${gameBoard.teams[GameColor.YELLOW]?.moves ?: "N/A"} §r
            §l§2Green:
                Towers: ${gameBoard.teams[GameColor.GREEN]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.GREEN]?.points ?: "N/A"}
                Moves: ${gameBoard.teams[GameColor.GREEN]?.moves ?: "N/A"} §r
            §l§dPurple:
                Towers: ${gameBoard.teams[GameColor.PURPLE]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.PURPLE]?.points ?: "N/A"}
                Moves: ${gameBoard.teams[GameColor.PURPLE]?.moves ?: "N/A"} §r
            §l§6Orange:
                Towers: ${gameBoard.teams[GameColor.ORANGE]?.capturedTowers ?: "N/A"}
                Points: ${gameBoard.teams[GameColor.ORANGE]?.points ?: "N/A"}
                Moves: ${gameBoard.teams[GameColor.ORANGE]?.moves ?: "N/A"} §r

        """.trimIndent()
        return Text.literal(results)
    }

    private fun commandNotImplemented(ctx: CommandContext<FabricClientCommandSource>): Int {
        ctx.source.sendError(Text.of("Not yet implemented"))
        return -999
    }

}
