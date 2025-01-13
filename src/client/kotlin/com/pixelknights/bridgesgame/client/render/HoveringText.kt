package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.util.minus
import com.pixelknights.bridgesgame.client.util.toVector3f
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.joml.Math.atan2
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

data class TextLine(
    val text: String,
    val color: Color,
)

data class HoveringText(
    val worldPos: Vec3i,
    val lines: MutableList<TextLine> = mutableListOf(),
) {
    fun addLine(text: String, color: Color): HoveringText {
        lines.add(TextLine(text, color))
        return this
    }
}

class HoveringTextRenderer(
    private val mc: MinecraftClient,
) : KoinComponent {
    val textToRender = mutableListOf<HoveringText>()
    val config: ModConfig by inject()


    fun renderAllText(context: WorldRenderContext) {
        if (!config.playerSettings.showTowerState) {
            return
        }
        val fontHeight = mc.textRenderer.fontHeight * SCALE

        textToRender
            .forEach { textBlock ->
                val position = textBlock.worldPos.toVector3f()
                textBlock.lines.forEachIndexed { index, line ->
                    val offset = Vector3f(0f, (textBlock.lines.size - index) * fontHeight, 0f)
                    renderText(context, line, position - offset)
                }
            }
    }


    fun renderText(context: WorldRenderContext, text: TextLine, pos: Vector3f) {
        val matrix = Matrix4f()
        val camera = context.camera()
        val cameraPos = camera.pos.toVector3f()
        val vertexConsumers = context.consumers() ?: return
        val textRenderer = mc.textRenderer


        // Place the text in the correct position
        matrix.translate(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
        matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)
        matrix.translate(0.5f, 1.0f, 0.5f)

        // Rotate the text to point toward the player
        val angleX = atan2(pos.x - cameraPos.x, pos.z - cameraPos.z).toFloat()
        val rotation = Quaternionf().rotationY(angleX)
        matrix.rotate(rotation)

        // Set text size
        matrix.scale(-SCALE, -SCALE, SCALE)

        val xPos = -textRenderer.getWidth(text.text) / 2f
        textRenderer.draw(text.text, xPos, 0f, text.color.argb, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0x818589, LightmapTextureManager.MAX_LIGHT_COORDINATE)
    }


    companion object {
        private const val MAX_DISTANCE = 4096.0
        private const val TEXT_HEIGHT = 1.0
        private const val LIGHT = 15728880
        private val TEXT_OFFSET = Vec3d(0.5, 1.0, 0.5)
        private const val SCALE = 0.04f
    }
}