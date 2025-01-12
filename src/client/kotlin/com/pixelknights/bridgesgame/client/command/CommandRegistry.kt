package com.pixelknights.bridgesgame.client.command

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import org.koin.core.component.KoinComponent

class CommandRegistry(
    private val judgeGameCommand: JudgeGameCommand,
    private val configGameCommand: ConfigGameCommand
) : KoinComponent {

    fun registerCommands() {
        registerJudgeGameCommand()
    }


    private fun registerJudgeGameCommand() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("bridges").then (
                    argument("action", StringArgumentType.string())
                        .suggests(ActionCompletionProvider)
                        .executes(judgeGameCommand)
                )

                    .executes(judgeGameCommand)
            )
        }
    }
}
