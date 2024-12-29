package com.pixelknights.bridgesgame.client.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.pixelknights.bridgesgame.client.game.entity.GameBoard
import com.pixelknights.bridgesgame.client.render.DotRenderer
import com.pixelknights.bridgesgame.client.render.LineRenderer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent

class JudgeGameCommand (
    private val mc: MinecraftClient,
    private val gameBoard: GameBoard,
    private val dotRenderer: DotRenderer,
    private val lineRenderer: LineRenderer,
) : Command<FabricClientCommandSource>, KoinComponent {


    override fun run(ctx: CommandContext<FabricClientCommandSource>): Int {
//        val playerPosition = mc.player?.pos
        val playerPosition = Vec3d(534.0, 150.0, 314.0)
        dotRenderer.dotsToRender.clear()
        lineRenderer.linesToRender.clear()

        playerPosition.let {
            val position = BlockPos.ofFloored(playerPosition.x, playerPosition.y, playerPosition.z)
            mc.world?.getBlockState(position)?.let {
                ctx.source.sendFeedback(Text.literal("Standing on: $it"))
            }.also {
                if (it == null) {
                    ctx.source.sendFeedback(Text.literal("Couldn't find block below player"))
                }
            }

            gameBoard.scanGame(BlockPos.ofFloored(playerPosition))
        }


        return 0
    }
}

fun registerJudgeGameCommand(koinApp : KoinApplication) {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
        dispatcher.register(
            ClientCommandManager.literal("judge_game")
                .executes(koinApp.koin.get(JudgeGameCommand::class))
        )
    }
}
