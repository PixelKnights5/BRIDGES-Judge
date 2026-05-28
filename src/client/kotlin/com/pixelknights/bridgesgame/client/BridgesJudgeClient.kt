package com.pixelknights.bridgesgame.client

import com.pixelknights.bridgesgame.client.command.CommandRegistry
import com.pixelknights.bridgesgame.client.di.initDi
import com.pixelknights.bridgesgame.client.game.ScanState
import com.pixelknights.bridgesgame.client.render.BridgesModels
import com.pixelknights.bridgesgame.client.render.DotRenderer
import com.pixelknights.bridgesgame.client.render.HoveringTextRenderer
import com.pixelknights.bridgesgame.client.render.LineRenderer
import com.pixelknights.bridgesgame.client.render.WarningIconRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class BridgesJudgeClient : ClientModInitializer {

    override fun onInitializeClient() {
        MOD_LOGGER.info("Initializing BridgesJudge Mod")
        BridgesModels.register()
        val koin = initDi()

        koin.koin.get<CommandRegistry>().registerCommands()
        renderDebugObjects(koin.koin.get(), koin.koin.get(), koin.koin.get(), koin.koin.get())
        registerScanSpinner(koin.koin.get())
    }

}

fun renderDebugObjects(dotRenderer: DotRenderer, lineRenderer: LineRenderer, textRenderer: HoveringTextRenderer, warningIconRenderer: WarningIconRenderer) {
    WorldRenderEvents.AFTER_ENTITIES.register { context ->
        dotRenderer.renderDots(context)
        lineRenderer.renderLines(context)
        warningIconRenderer.renderWarnings(context)
    }

    WorldRenderEvents.END_MAIN.register { context ->
        textRenderer.renderAllText(context)
    }
}

fun registerScanSpinner(scanState: ScanState) {
    ClientTickEvents.END_CLIENT_TICK.register { mc ->
        if (scanState.isScanning.get()) {
            scanState.spinnerTick++
            val frame = SPINNER_FRAMES[(scanState.spinnerTick / SPINNER_TICKS_PER_FRAME) % SPINNER_FRAMES.size]
            mc.inGameHud.setOverlayMessage(Text.of("${frame.reversed()} Scanning Bridges $frame"), false)
            scanState.hasActiveOverlay = true
        } else if (scanState.hasActiveOverlay) {
            mc.inGameHud.setOverlayMessage(Text.literal(""), false)
            scanState.hasActiveOverlay = false
            scanState.spinnerTick = 0
        }
    }
}

//private val SPINNER_FRAMES = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
//private val SPINNER_FRAMES = listOf("█   ", " █  ", "  █ ", "   █", "  █ ", " █  ")
private val SPINNER_FRAMES = listOf("▌   ", " ▌  ", "  ▌ ", "   ▌", "  ▌ ", " ▌  ")
//private val SPINNER_FRAMES = listOf("▌ ","▀","▐","▄")
private const val SPINNER_TICKS_PER_FRAME = 2

const val MOD_ID = "bridges-judge"
val MOD_LOGGER: Logger = LogManager.getLogger(MOD_ID)
