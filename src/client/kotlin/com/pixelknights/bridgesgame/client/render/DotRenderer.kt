package com.pixelknights.bridgesgame.client.render

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f

class DotRenderer (
) {
    val dotsToRender: MutableSet<DebugDot> = mutableSetOf()

    // Store positions where dots should be rendered

//    init {
//        // Register the render callback
//        WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
//            renderDots(context)
//        }
//    }

    fun renderDots(context: WorldRenderContext) {
        val matrices = context.matrixStack() ?: throw IllegalStateException("MatrixStack is null")
        val camera = context.camera()
        val vertexConsumers = context.consumers() ?: return

        // Save the current matrix state
        matrices.push()

        // Get camera position for proper rendering position
        val cameraPos = camera.pos

        for (dot in dotsToRender) {
            // Translate to the block position
            matrices.translate(
                dot.position.x - cameraPos.x + 0.5 + dot.noise.x,
                dot.position.y - cameraPos.y + 0.5 + dot.noise.y,
                dot.position.z - cameraPos.z + 0.5 + dot.noise.z
            )

            // Draw the dot
            drawDot(
                matrices,
                vertexConsumers,
                dot.color
            )

            // Reset translation for next dot
            matrices.translate(
                -(dot.position.x - cameraPos.x + 0.5 + dot.noise.x),
                -(dot.position.y - cameraPos.y + 0.5 + dot.noise.y),
                -(dot.position.z - cameraPos.z + 0.5 + dot.noise.z)
            )
        }

        // Restore the matrix state
        matrices.pop()
    }

    private fun drawDot(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        color: Color
    ) {
        val size = 0.1f  // Size of the dot in blocks
        val consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent())
        val matrix = matrices.peek().positionMatrix
        val light = LightmapTextureManager.MAX_LIGHT_COORDINATE

        // Draw a quad facing all directions to make the dot visible from any angle
        drawQuad(consumer, matrix, color, size, light, Vec3d(1.0, 0.0, 0.0))  // X facing
        drawQuad(consumer, matrix, color, size, light, Vec3d(0.0, 1.0, 0.0))  // Y facing
        drawQuad(consumer, matrix, color, size, light, Vec3d(0.0, 0.0, 1.0))  // Z facing
    }

    private fun drawQuad(
        consumer: VertexConsumer,
        matrix: Matrix4f,
        color: Color,
        size: Float,
        light: Int,
        normal: Vec3d
    ) {
        val halfSize = size / 2f

        // Calculate vertices based on the normal direction
        val (x1, y1, z1) = when {
            normal.x != 0.0 -> Triple(-halfSize, -halfSize, 0f)
            normal.y != 0.0 -> Triple(-halfSize, 0f, -halfSize)
            else -> Triple(0f, -halfSize, -halfSize)
        }

        val (x2, y2, z2) = when {
            normal.x != 0.0 -> Triple(-halfSize, halfSize, 0f)
            normal.y != 0.0 -> Triple(-halfSize, 0f, halfSize)
            else -> Triple(0f, -halfSize, halfSize)
        }

        val (x3, y3, z3) = when {
            normal.x != 0.0 -> Triple(halfSize, halfSize, 0f)
            normal.y != 0.0 -> Triple(halfSize, 0f, halfSize)
            else -> Triple(0f, halfSize, halfSize)
        }

        val (x4, y4, z4) = when {
            normal.x != 0.0 -> Triple(halfSize, -halfSize, 0f)
            normal.y != 0.0 -> Triple(halfSize, 0f, -halfSize)
            else -> Triple(0f, halfSize, -halfSize)
        }

        // Draw the quad
        consumer.vertex(matrix, x1, y1, z1).color(color.red, color.green, color.blue, color.alpha)
            .texture(0f, 0f)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat())
        consumer.vertex(matrix, x2, y2, z2).color(color.red, color.green, color.blue, color.alpha)
            .texture(0f, 1f)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat())
        consumer.vertex(matrix, x3, y3, z3).color(color.red, color.green, color.blue, color.alpha)
            .texture(1f, 1f)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat())
        consumer.vertex(matrix, x4, y4, z4).color(color.red, color.green, color.blue, color.alpha)
            .texture(1f, 0f)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat())
    }
}