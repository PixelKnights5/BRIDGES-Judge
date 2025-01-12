package com.pixelknights.bridgesgame.client.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import org.koin.core.component.KoinComponent

class BridgesCommand(

) : Command<FabricClientCommandSource>, KoinComponent {

    override fun run(context: CommandContext<FabricClientCommandSource?>?): Int {
        TODO("Not yet implemented")
    }

}