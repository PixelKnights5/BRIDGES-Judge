package com.pixelknights.bridgesgame.client.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.pixelknights.bridgesgame.client.game.entity.scanner.TowerScanner
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.Logger
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent

class JudgeGameCommand (
    private val logger: Logger,
    private val mc: MinecraftClient,
    private val towerScanner: TowerScanner
) : Command<FabricClientCommandSource>, KoinComponent {


    override fun run(ctx: CommandContext<FabricClientCommandSource>): Int {
        val playerPosition = mc.player?.pos

        playerPosition?.let {
            val position = BlockPos.ofFloored(playerPosition.x, playerPosition.y, playerPosition.z)
            mc.world?.getBlockState(position)?.let {
                ctx.source.sendFeedback(Text.literal("Standing on: $it"))
            }.also {
                if (it == null) {
                    ctx.source.sendFeedback(Text.literal("Couldn't find block below player"))
                }
            }

            towerScanner.getTowers(position)
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
