package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.game.entity.Bridge
import com.pixelknights.bridgesgame.client.game.entity.Node
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.Logger

class BridgeScanner (
    private val logger: Logger,
    private val config: ModConfig,
    private val mc: MinecraftClient
) {



    fun getBridgesForNode(node: Node): List<Bridge> {
        TODO()
    }


}

