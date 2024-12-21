package com.pixelknights.bridgesgame.client.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
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
    private val client: MinecraftClient
) : Command<FabricClientCommandSource>, KoinComponent {


    override fun run(ctx: CommandContext<FabricClientCommandSource>): Int {
        ctx.source.sendFeedback(Text.literal("Standing on: ${getBlockBelowPlayer()}"))
        return 0
    }

    private fun getBlockBelowPlayer(): BlockState? {
        val playerPosition = client.player?.pos
        if (playerPosition != null) {
            val block = client.world?.getBlockState(BlockPos.ofFloored(playerPosition.x, playerPosition.y - 1, playerPosition.z))
            return block
        } else {
            logger.info("Could not get block below player - Player was null")
        }

        return null
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
