package com.pixelknights.bridgesgame.client

import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.LogManager

class BridgesJudgeClient : ClientModInitializer {

    override fun onInitializeClient() {
        logger.info("Initializing BridgesJudge Mod")
    }

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        const val MOD_ID = "bridges-judge"
        val logger = LogManager.getLogger(MOD_ID)
    }
}
