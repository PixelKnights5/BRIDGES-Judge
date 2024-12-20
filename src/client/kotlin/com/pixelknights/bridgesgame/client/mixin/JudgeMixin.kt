package com.pixelknights.bridgesgame.client.mixin

import com.pixelknights.bridgesgame.client.MOD_LOGGER
import com.pixelknights.bridgesgame.client.config.TowerLayoutConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.TitleScreen
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen::class)
class JudgeMixin {

    @Inject(method = ["init()V"], at = [At("RETURN")])
    private fun registerMixin(info: CallbackInfo) {
        MOD_LOGGER.info("Mixin: $info")
        val foo = TowerLayoutConfig()
        MOD_LOGGER.info("JudgeMixin registered")
    }

}