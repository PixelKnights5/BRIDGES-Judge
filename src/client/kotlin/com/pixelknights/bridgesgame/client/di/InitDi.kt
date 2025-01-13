package com.pixelknights.bridgesgame.client.di

import com.pixelknights.bridgesgame.client.MOD_ID
import com.pixelknights.bridgesgame.client.command.CommandRegistry
import com.pixelknights.bridgesgame.client.command.JudgeGameCommand
import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.config.TowerLayoutConfig
import com.pixelknights.bridgesgame.client.game.entity.GameBoard
import com.pixelknights.bridgesgame.client.game.entity.scanner.BridgeScanner
import com.pixelknights.bridgesgame.client.game.entity.scanner.FloorScanner
import com.pixelknights.bridgesgame.client.game.entity.scanner.TowerScanner
import com.pixelknights.bridgesgame.client.render.DotRenderer
import com.pixelknights.bridgesgame.client.render.HoveringTextRenderer
import com.pixelknights.bridgesgame.client.render.LineRenderer
import net.minecraft.client.MinecraftClient
import org.apache.logging.log4j.LogManager
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

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
    singleOf(::CommandRegistry)
    singleOf(::TowerLayoutConfig)
    singleOf(::JudgeGameCommand)
    singleOf(::TowerScanner)
    singleOf(::FloorScanner)
    singleOf(::BridgeScanner)
    singleOf(::GameBoard)
    singleOf(::DotRenderer)
    singleOf(::LineRenderer)
    singleOf(::HoveringTextRenderer)
}

fun initDi(): KoinApplication {
    return startKoin {
        modules(appModule)
    }
}