package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.config.ModConfig
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.WorldRenderer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import org.joml.Quaternionf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.PI

data class GameWarning(
    val position: BlockPos,
    val color: Color,
    val message: String,
    val id: Int = 0,
    var isVisible: Boolean = true,
)

class WarningIconRenderer : KoinComponent {
    val warnings: MutableSet<GameWarning> = mutableSetOf()

    private val config: ModConfig by inject()
    private val renderUtils: RenderUtils by inject()
    private val mc: MinecraftClient by inject()

    fun renderWarnings(context: WorldRenderContext) {
        if ((!config.playerSettings.showBridgePaths) || (!renderUtils.shouldRender)) {
            return
        }

        val matrices = context.matrices()
        val cameraPos = context.worldState().cameraRenderState.pos
        val vertexConsumers = context.consumers()
        val world = mc.world ?: return

        val model = BridgesModels.bakedWarningIcon() ?: return
        val parts = model.getParts(RANDOM)
        val consumer = vertexConsumers.getBuffer(RenderLayers.solid())
        val brightnesses = floatArrayOf(1f, 1f, 1f, 1f)

        val angleRad = ((System.currentTimeMillis() % ROTATION_PERIOD_MS).toFloat() / ROTATION_PERIOD_MS) * TWO_PI

        for (warning in warnings) {
            if (!warning.isVisible) {
                continue
            }
            val pos = warning.position
            val light = WorldRenderer.getLightmapCoordinates(world, pos)
            val lights = intArrayOf(light, light, light, light)

            matrices.push()
            matrices.translate(
                pos.x - cameraPos.x + 0.5,
                pos.y - cameraPos.y + HEIGHT_OFFSET,
                pos.z - cameraPos.z + 0.5,
            )
            matrices.multiply(Quaternionf().rotationY(angleRad))
            matrices.scale(ICON_SCALE, ICON_SCALE, ICON_SCALE)
            matrices.translate(-0.5, 0.0, -0.5)

            val entry = matrices.peek()
            val r = warning.color.red
            val g = warning.color.green
            val b = warning.color.blue
            val a = warning.color.alpha
            for (part in parts) {
                for (direction in Direction.entries) {
                    for (quad in part.getQuads(direction)) {
                        consumer.quad(entry, quad, brightnesses, r, g, b, a, lights, OverlayTexture.DEFAULT_UV)
                    }
                }
                for (quad in part.getQuads(null)) {
                    consumer.quad(entry, quad, brightnesses, r, g, b, a, lights, OverlayTexture.DEFAULT_UV)
                }
            }

            matrices.pop()
        }
    }

    private companion object {
        const val ROTATION_PERIOD_MS = 3000L
        const val ICON_SCALE = 2f
        const val HEIGHT_OFFSET = 0f
        const val TWO_PI = PI.toFloat() * 2f
        val RANDOM: Random = Random.create(0L)
    }
}
