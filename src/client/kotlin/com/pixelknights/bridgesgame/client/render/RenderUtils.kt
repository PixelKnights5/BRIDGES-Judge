package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.config.ModConfig
import net.minecraft.client.MinecraftClient
import net.minecraft.world.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class RenderUtils : KoinComponent {

    private val client: MinecraftClient by inject()

    val shouldRender: Boolean
        get() = client.world?.registryKey?.equals(World.OVERWORLD) ?: false


}