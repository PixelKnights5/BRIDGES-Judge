package com.pixelknights.bridgesgame.client.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import org.koin.core.component.KoinComponent

class ConfigGameCommand()
    : Command<FabricClientCommandSource>, KoinComponent {
    override fun run(context: CommandContext<FabricClientCommandSource?>?): Int {
        println("Hello World")
        return 0
    }


}