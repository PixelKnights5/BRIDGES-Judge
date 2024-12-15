package com.pixelknights.bridgesgame.client

import com.pixelknights.bridgesgame.client.command.registerJudgeGameCommand
import com.pixelknights.bridgesgame.client.config.ModConfig
import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class BridgesJudgeClient : ClientModInitializer {

    override fun onInitializeClient() {
        MOD_LOGGER.info("Initializing BridgesJudge Mod")
        registerJudgeGameCommand()
        val config = ModConfig.INSTANCE
        MOD_LOGGER.info("Config = $config")
    }

}

const val MOD_ID = "bridges-judge"
val MOD_LOGGER: Logger = LogManager.getLogger(MOD_ID)