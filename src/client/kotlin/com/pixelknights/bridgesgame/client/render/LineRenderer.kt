package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.config.ModConfig
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.model.BlockModelPart
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import org.joml.Matrix3f
import org.joml.Quaternionf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.abs
import kotlin.math.sqrt

class LineRenderer : KoinComponent {
    val linesToRender = mutableSetOf<RenderedLine>()

    val config: ModConfig by inject()
    private val renderUtils: RenderUtils by inject()


    fun renderLines(context: WorldRenderContext) {
        if ((!config.playerSettings.showBridgePaths) || (!renderUtils.shouldRender)) {
            return
        }

        val matrices = context.matrices()
        val cameraPos = context.worldState().cameraRenderState.pos
        val vertexConsumers = context.consumers() ?: return
        val world = MinecraftClient.getInstance().world ?: return

        val startModel = BridgesModels.bakedLineStart() ?: return
        val middleModel = BridgesModels.bakedLineMiddle() ?: return
        val endModel = BridgesModels.bakedLineEnd() ?: return

        val startParts = startModel.getParts(RANDOM)
        val middleParts = middleModel.getParts(RANDOM)
        val endParts = endModel.getParts(RANDOM)

        val consumer = vertexConsumers.getBuffer(RenderLayers.solid())
        val brightnesses = floatArrayOf(1f, 1f, 1f, 1f)

        for (line in linesToRender) {
            val noise = line.noiseVector
            val ax = (line.start.x - cameraPos.x + 0.5 + noise.x).toFloat()
            val ay = (line.start.y - cameraPos.y + 0.5 + noise.y).toFloat()
            val az = (line.start.z - cameraPos.z + 0.5 + noise.z).toFloat()
            val bx = (line.end.x - cameraPos.x + 0.5 + noise.x).toFloat()
            val by = (line.end.y - cameraPos.y + 0.5 + noise.y).toFloat()
            val bz = (line.end.z - cameraPos.z + 0.5 + noise.z).toFloat()

            val dx = bx - ax; val dy = by - ay; val dz = bz - az
            val length = sqrt(dx * dx + dy * dy + dz * dz)
            if (length < 1e-4f) {
                continue
            }
            val ffx = dx / length; val ffy = dy / length; val ffz = dz / length

            val rx: Float; val ry: Float; val rz: Float
            if (abs(ffy) < 0.99f) {
                val cxR = -ffz; val czR = ffx
                val cl = sqrt(cxR * cxR + czR * czR)
                rx = cxR / cl; ry = 0f; rz = czR / cl
            } else {
                val cyR = ffz; val czR = -ffy
                val cl = sqrt(cyR * cyR + czR * czR)
                rx = 0f; ry = cyR / cl; rz = czR / cl
            }
            val ux = ry * ffz - rz * ffy
            val uy = rz * ffx - rx * ffz
            val uz = rx * ffy - ry * ffx

            val rotMat = Matrix3f(
                ffx, ffy, ffz,
                ux,  uy,  uz,
                rx,  ry,  rz,
            )
            val quat = Quaternionf().setFromNormalized(rotMat)

            val midPos = BlockPos(
                (line.start.x + line.end.x) / 2,
                (line.start.y + line.end.y) / 2,
                (line.start.z + line.end.z) / 2,
            )
            val light = WorldRenderer.getLightmapCoordinates(world, midPos)
            val lights = intArrayOf(light, light, light, light)
            val r = line.color.red; val g = line.color.green
            val b = line.color.blue; val a = line.color.alpha

            if (length < CAP_LENGTH * 2f) {
                // Too short for two caps + middle — render a single stretched cap.
                val cx = (ax + bx) * 0.5f
                val cy = (ay + by) * 0.5f
                val cz = (az + bz) * 0.5f
                renderPiece(matrices, consumer, startParts, cx, cy, cz, quat,
                    length / CAP_LENGTH, r, g, b, a, brightnesses, lights)
                continue
            }

            // Start cap, fixed length, anchored at line start.
            val startX = ax + ffx * (CAP_LENGTH * 0.5f)
            val startY = ay + ffy * (CAP_LENGTH * 0.5f)
            val startZ = az + ffz * (CAP_LENGTH * 0.5f)
            renderPiece(matrices, consumer, startParts, startX, startY, startZ, quat,
                1f, r, g, b, a, brightnesses, lights)

            // Stretched middle: single piece spanning the gap between caps. Cheap because
            // the white texture stretches without visible artifacts; if a patterned middle
            // is ever authored, switch back to a per-tile loop here.
            val middleSpan = length - 2f * CAP_LENGTH
            if (middleSpan > 0f) {
                val along = CAP_LENGTH + middleSpan * 0.5f
                val cx = ax + ffx * along
                val cy = ay + ffy * along
                val cz = az + ffz * along
                renderPiece(matrices, consumer, middleParts, cx, cy, cz, quat,
                    middleSpan / TILE_LENGTH, r, g, b, a, brightnesses, lights)
            }

            // End cap, fixed length, anchored at line end.
            val endX = ax + ffx * (length - CAP_LENGTH * 0.5f)
            val endY = ay + ffy * (length - CAP_LENGTH * 0.5f)
            val endZ = az + ffz * (length - CAP_LENGTH * 0.5f)
            renderPiece(matrices, consumer, endParts, endX, endY, endZ, quat,
                1f, r, g, b, a, brightnesses, lights)
        }
    }

    private fun renderPiece(
        matrices: MatrixStack,
        consumer: VertexConsumer,
        parts: List<BlockModelPart>,
        cx: Float, cy: Float, cz: Float,
        quat: Quaternionf,
        scaleX: Float,
        r: Float, g: Float, b: Float, a: Float,
        brightnesses: FloatArray,
        lights: IntArray,
    ) {
        matrices.push()
        matrices.translate(cx.toDouble(), cy.toDouble(), cz.toDouble())
        matrices.multiply(quat)
        matrices.scale(scaleX, 1f, 1f)
        matrices.translate(-0.5f, -0.5f, -0.5f)
        val entry = matrices.peek()

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

    private companion object {
        // Default model JSONs are LINE_THICKNESS-cubes; CAP_LENGTH and TILE_LENGTH must
        // match the X-extent of the corresponding model in world units (1 unit = 1 block).
        const val CAP_LENGTH = 0.07f
        const val TILE_LENGTH = 0.07f
        val RANDOM: Random = Random.create(0L)
    }
}