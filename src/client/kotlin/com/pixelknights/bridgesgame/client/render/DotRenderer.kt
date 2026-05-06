package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.config.ModConfig
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.WorldRenderer
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import org.joml.Matrix4f
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DotRenderer : KoinComponent {
    val dotsToRender: MutableSet<DebugDot> = mutableSetOf()

    val config: ModConfig by inject()

    fun renderDots(context: WorldRenderContext) {
        if (!config.playerSettings.showBridgePaths) {
            return
        }

        val matrices = context.matrices()
        val cameraPos = context.worldState().cameraRenderState.pos
        val vertexConsumers = context.consumers() ?: return
        val world = MinecraftClient.getInstance().world ?: return

        val model = BridgesModels.bakedDot() ?: return
        val parts = model.getParts(RANDOM)
        val consumer = vertexConsumers.getBuffer(RenderLayers.solid())
        val brightnesses = floatArrayOf(1f, 1f, 1f, 1f)

        for (dot in dotsToRender) {
            matrices.push()
            matrices.translate(
                dot.position.x - cameraPos.x + dot.noise.x,
                dot.position.y - cameraPos.y + dot.noise.y,
                dot.position.z - cameraPos.z + dot.noise.z,
            )

            val light = WorldRenderer.getLightmapCoordinates(world, dot.position)
            val lights = intArrayOf(light, light, light, light)
            val r = dot.color.red
            val g = dot.color.green
            val b = dot.color.blue
            val a = dot.color.alpha
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
//
//        val outlineConsumer = vertexConsumers.getBuffer(RenderLayers.lines())
//        for (dot in dotsToRender) {
//            matrices.push()
//            matrices.translate(
//                dot.position.x - cameraPos.x + 0.5 + dot.noise.x,
//                dot.position.y - cameraPos.y + 0.5 + dot.noise.y,
//                dot.position.z - cameraPos.z + 0.5 + dot.noise.z
//            )
//
//            drawOutline(outlineConsumer, matrices.peek().positionMatrix)
//
//            matrices.pop()
//        }
    }

    private fun drawCube(
        consumer: VertexConsumer,
        matrix: Matrix4f,
        color: Color,
        light: Int
    ) {
        val h = DOT_SIZE / 2f
        val r = color.red
        val g = color.green
        val b = color.blue
        val a = color.alpha
        val overlay = OverlayTexture.DEFAULT_UV

        // Front (-Z)
        drawFace(consumer, matrix, r, g, b, a, light, overlay, 0f, 0f, -1f,
            -h,  h, -h,  h,  h, -h,  h, -h, -h, -h, -h, -h)
        // Back (+Z)
        drawFace(consumer, matrix, r, g, b, a, light, overlay, 0f, 0f, 1f,
             h,  h,  h, -h,  h,  h, -h, -h,  h,  h, -h,  h)
        // Left (-X)
        drawFace(consumer, matrix, r, g, b, a, light, overlay, -1f, 0f, 0f,
            -h,  h,  h, -h,  h, -h, -h, -h, -h, -h, -h,  h)
        // Right (+X)
        drawFace(consumer, matrix, r, g, b, a, light, overlay, 1f, 0f, 0f,
             h,  h, -h,  h,  h,  h,  h, -h,  h,  h, -h, -h)
        // Top (+Y)
        drawFace(consumer, matrix, r, g, b, a, light, overlay, 0f, 1f, 0f,
            -h,  h,  h,  h,  h,  h,  h,  h, -h, -h,  h, -h)
        // Bottom (-Y)
        drawFace(consumer, matrix, r, g, b, a, light, overlay, 0f, -1f, 0f,
            -h, -h, -h,  h, -h, -h,  h, -h,  h, -h, -h,  h)
    }

    private fun drawFace(
        consumer: VertexConsumer,
        matrix: Matrix4f,
        r: Float, g: Float, b: Float, a: Float,
        light: Int, overlay: Int,
        nx: Float, ny: Float, nz: Float,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float,
        x4: Float, y4: Float, z4: Float
    ) {
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).texture(0f, 0f).overlay(overlay).light(light).normal(nx, ny, nz)
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).texture(1f, 0f).overlay(overlay).light(light).normal(nx, ny, nz)
        consumer.vertex(matrix, x3, y3, z3).color(r, g, b, a).texture(1f, 1f).overlay(overlay).light(light).normal(nx, ny, nz)
        consumer.vertex(matrix, x4, y4, z4).color(r, g, b, a).texture(0f, 1f).overlay(overlay).light(light).normal(nx, ny, nz)
    }

    private fun drawOutline(consumer: VertexConsumer, matrix: Matrix4f) {
        val h = DOT_SIZE / 2f
        // Bottom face
        drawEdge(consumer, matrix, -h, -h, -h,  h, -h, -h,  1f,  0f,  0f)
        drawEdge(consumer, matrix,  h, -h, -h,  h, -h,  h,  0f,  0f,  1f)
        drawEdge(consumer, matrix,  h, -h,  h, -h, -h,  h, -1f,  0f,  0f)
        drawEdge(consumer, matrix, -h, -h,  h, -h, -h, -h,  0f,  0f, -1f)
        // Top face
        drawEdge(consumer, matrix, -h,  h, -h,  h,  h, -h,  1f,  0f,  0f)
        drawEdge(consumer, matrix,  h,  h, -h,  h,  h,  h,  0f,  0f,  1f)
        drawEdge(consumer, matrix,  h,  h,  h, -h,  h,  h, -1f,  0f,  0f)
        drawEdge(consumer, matrix, -h,  h,  h, -h,  h, -h,  0f,  0f, -1f)
        // Vertical edges
        drawEdge(consumer, matrix, -h, -h, -h, -h,  h, -h,  0f,  1f,  0f)
        drawEdge(consumer, matrix,  h, -h, -h,  h,  h, -h,  0f,  1f,  0f)
        drawEdge(consumer, matrix,  h, -h,  h,  h,  h,  h,  0f,  1f,  0f)
        drawEdge(consumer, matrix, -h, -h,  h, -h,  h,  h,  0f,  1f,  0f)
    }

    private fun drawEdge(
        consumer: VertexConsumer,
        matrix: Matrix4f,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        nx: Float, ny: Float, nz: Float
    ) {
        consumer.vertex(matrix, x1, y1, z1).color(0, 0, 0, 255).normal(nx, ny, nz).lineWidth(OUTLINE_WIDTH)
        consumer.vertex(matrix, x2, y2, z2).color(0, 0, 0, 255).normal(nx, ny, nz).lineWidth(OUTLINE_WIDTH)
    }

    private companion object {
        const val DOT_SIZE = 0.1f
        const val OUTLINE_WIDTH = 5f
        val RANDOM: Random = Random.create(0L)
    }
}