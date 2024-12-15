package com.pixelknights.bridgesgame.client.command

import com.pixelknights.bridgesgame.client.util.getBlockBelowPlayer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos


fun registerJudgeGameCommand() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
        dispatcher.register(
            ClientCommandManager.literal("judge_game")
                .executes { ctx ->


                    ctx.source.sendFeedback(Text.literal("Standing on: ${getBlockBelowPlayer()}"))
                    1
                }
        )
    }
}
