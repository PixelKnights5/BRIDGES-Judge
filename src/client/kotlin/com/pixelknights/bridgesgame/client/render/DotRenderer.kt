package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.config.ModConfig
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumer
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
        val consumer = vertexConsumers.getBuffer(RenderLayers.debugFilledBox())
        for (dot in dotsToRender) {
            matrices.push()
            matrices.translate(
                dot.position.x - cameraPos.x + 0.5 + dot.noise.x,
                dot.position.y - cameraPos.y + 0.5 + dot.noise.y,
                dot.position.z - cameraPos.z + 0.5 + dot.noise.z
            )

            drawCube(consumer, matrices.peek().positionMatrix, dot.color)

            matrices.pop()
        }

        val outlineConsumer = vertexConsumers.getBuffer(RenderLayers.lines())
        for (dot in dotsToRender) {
            matrices.push()
            matrices.translate(
                dot.position.x - cameraPos.x + 0.5 + dot.noise.x,
                dot.position.y - cameraPos.y + 0.5 + dot.noise.y,
                dot.position.z - cameraPos.z + 0.5 + dot.noise.z
            )

            drawOutline(outlineConsumer, matrices.peek().positionMatrix)

            matrices.pop()
        }
    }

    private fun drawCube(
        consumer: VertexConsumer,
        matrix: Matrix4f,
        color: Color
    ) {
        val h = DOT_SIZE / 2f
        val r = color.red
        val g = color.green
        val b = color.blue
        val a = color.alpha

        // Front (-Z)
        consumer.vertex(matrix, -h, -h, -h).color(r, g, b, a)
        consumer.vertex(matrix,  h, -h, -h).color(r, g, b, a)
        consumer.vertex(matrix,  h,  h, -h).color(r, g, b, a)
        consumer.vertex(matrix, -h,  h, -h).color(r, g, b, a)
        // Back (+Z)
        consumer.vertex(matrix,  h, -h,  h).color(r, g, b, a)
        consumer.vertex(matrix, -h, -h,  h).color(r, g, b, a)
        consumer.vertex(matrix, -h,  h,  h).color(r, g, b, a)
        consumer.vertex(matrix,  h,  h,  h).color(r, g, b, a)
        // Left (-X)
        consumer.vertex(matrix, -h, -h,  h).color(r, g, b, a)
        consumer.vertex(matrix, -h, -h, -h).color(r, g, b, a)
        consumer.vertex(matrix, -h,  h, -h).color(r, g, b, a)
        consumer.vertex(matrix, -h,  h,  h).color(r, g, b, a)
        // Right (+X)
        consumer.vertex(matrix,  h, -h, -h).color(r, g, b, a)
        consumer.vertex(matrix,  h, -h,  h).color(r, g, b, a)
        consumer.vertex(matrix,  h,  h,  h).color(r, g, b, a)
        consumer.vertex(matrix,  h,  h, -h).color(r, g, b, a)
        // Top (+Y)
        consumer.vertex(matrix, -h,  h, -h).color(r, g, b, a)
        consumer.vertex(matrix,  h,  h, -h).color(r, g, b, a)
        consumer.vertex(matrix,  h,  h,  h).color(r, g, b, a)
        consumer.vertex(matrix, -h,  h,  h).color(r, g, b, a)
        // Bottom (-Y)
        consumer.vertex(matrix, -h, -h,  h).color(r, g, b, a)
        consumer.vertex(matrix,  h, -h,  h).color(r, g, b, a)
        consumer.vertex(matrix,  h, -h, -h).color(r, g, b, a)
        consumer.vertex(matrix, -h, -h, -h).color(r, g, b, a)
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
    }
}