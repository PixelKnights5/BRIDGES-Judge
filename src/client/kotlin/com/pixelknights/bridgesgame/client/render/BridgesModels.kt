package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.MOD_ID
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BlockStateModel
import net.minecraft.util.Identifier

object BridgesModels {
    val DOT: ExtraModelKey<BlockStateModel> = ExtraModelKey.create { "$MOD_ID:dot" }

    fun register() {
        ModelLoadingPlugin.register { context ->
            context.addModel(DOT, SimpleUnbakedExtraModel.blockStateModel(
                Identifier.of(MOD_ID, "misc/dot")
            ))
        }
    }

    fun bakedDot(): BlockStateModel? =
        (MinecraftClient.getInstance().bakedModelManager as FabricBakedModelManager).getModel(DOT)
}