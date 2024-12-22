package com.pixelknights.bridgesgame

import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * This extension provides common functionality to bootstrap the Minecraft environment as
 * described in [the Fabric testing docs](https://docs.fabricmc.net/develop/automatic-testing#setting-up-registries)
 */
class FabricExtension : BeforeAllCallback {

    override fun beforeAll(ctx: ExtensionContext?) {
        SharedConstants.createGameVersion()
        Bootstrap.initialize()
    }

}