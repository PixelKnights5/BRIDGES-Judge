package com.pixelknights.bridgesgame.client.di

import com.pixelknights.bridgesgame.client.MOD_ID
import com.pixelknights.bridgesgame.client.command.JudgeGameCommand
import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.config.TowerLayoutConfig
import com.pixelknights.bridgesgame.client.game.entity.scanner.FloorScanner
import com.pixelknights.bridgesgame.client.game.entity.scanner.TowerScanner
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf

val appModule = module {
    single {
        LogManager.getLogger(MOD_ID)
    }
    single {
        ModConfig.loadConfig()
    }
    single {
        MinecraftClient.getInstance()
    }
    singleOf(::TowerLayoutConfig)
    singleOf(::JudgeGameCommand)
    singleOf(::TowerScanner)
    singleOf(::FloorScanner)
}

fun initDi(): KoinApplication {
    return startKoin {
        modules(appModule)
    }
}