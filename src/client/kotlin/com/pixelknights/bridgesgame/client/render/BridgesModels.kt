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
    val LINE_START: ExtraModelKey<BlockStateModel> = ExtraModelKey.create { "$MOD_ID:line_start" }
    val LINE_MIDDLE: ExtraModelKey<BlockStateModel> = ExtraModelKey.create { "$MOD_ID:line_middle" }
    val LINE_END: ExtraModelKey<BlockStateModel> = ExtraModelKey.create { "$MOD_ID:line_end" }

    fun register() {
        ModelLoadingPlugin.register { context ->
            context.addModel(DOT, SimpleUnbakedExtraModel.blockStateModel(
                Identifier.of(MOD_ID, "misc/dot")
            ))
            context.addModel(LINE_START, SimpleUnbakedExtraModel.blockStateModel(
                Identifier.of(MOD_ID, "misc/line_start")
            ))
            context.addModel(LINE_MIDDLE, SimpleUnbakedExtraModel.blockStateModel(
                Identifier.of(MOD_ID, "misc/line_middle")
            ))
            context.addModel(LINE_END, SimpleUnbakedExtraModel.blockStateModel(
                Identifier.of(MOD_ID, "misc/line_end")
            ))
        }
    }

    private val manager: FabricBakedModelManager
        get() = MinecraftClient.getInstance().bakedModelManager as FabricBakedModelManager

    fun bakedDot(): BlockStateModel? = manager.getModel(DOT)
    fun bakedLineStart(): BlockStateModel? = manager.getModel(LINE_START)
    fun bakedLineMiddle(): BlockStateModel? = manager.getModel(LINE_MIDDLE)
    fun bakedLineEnd(): BlockStateModel? = manager.getModel(LINE_END)
}