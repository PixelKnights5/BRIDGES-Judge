package com.pixelknights.bridgesgame.client

import com.pixelknights.bridgesgame.client.command.registerJudgeGameCommand
import com.pixelknights.bridgesgame.client.di.initDi
import com.pixelknights.bridgesgame.client.render.DotRenderer
import com.pixelknights.bridgesgame.client.render.HoveringTextRenderer
import com.pixelknights.bridgesgame.client.render.LineRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class BridgesJudgeClient : ClientModInitializer {

    override fun onInitializeClient() {
        MOD_LOGGER.info("Initializing BridgesJudge Mod")
        val koin = initDi()
        registerJudgeGameCommand(koin)
        renderDebugObjects(koin.koin.get(), koin.koin.get(), koin.koin.get())
    }

}

fun renderDebugObjects(dotRenderer: DotRenderer, lineRenderer: LineRenderer, textRenderer: HoveringTextRenderer) {
    WorldRenderEvents.AFTER_ENTITIES.register { context ->
        dotRenderer.renderDots(context)
        lineRenderer.renderLines(context)

    }

    WorldRenderEvents.LAST.register { context ->
        textRenderer.renderAllText(context)
    }

//
//    HudRenderCallback.EVENT.register { matrixStack, tickDelta ->
//        textRenderer.renderAllText(matrixStack, tickDelta)
//    }
}

const val MOD_ID = "bridges-judge"
val MOD_LOGGER: Logger = LogManager.getLogger(MOD_ID)