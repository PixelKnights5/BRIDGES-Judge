package com.pixelknights.bridgesgame.client.render

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d

class LineRenderer {
    val linesToRender = mutableSetOf<DebugLine>()


    fun renderLines(context: WorldRenderContext) {
        val matrices = context.matrixStack() ?: throw IllegalStateException("MatrixStack is null")
        val camera = context.camera()
        val vertexConsumers = context.consumers() ?: return

        matrices.push()
        val cameraPos = camera.pos

        linesToRender.forEach { line ->
            drawLine(
                matrices,
                vertexConsumers,
                line,
                cameraPos
            )
        }


        matrices.pop()
    }

    @Suppress("DuplicatedCode")
    private fun drawLine(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        line: DebugLine,
        cameraPos: Vec3d
    ) {
        val consumer = vertexConsumers.getBuffer(RenderLayer.LINES)
        val matrix = matrices.peek().positionMatrix

        val noiseVector = line.noiseVector
        // Set the star and end points of the line to be rendered with noise along the Y axis to distinguish overlapping lines
        consumer.vertex(
            matrix,
            (line.start.x - cameraPos.x + 0.5 + noiseVector.x).toFloat(),
            (line.start.y - cameraPos.y + 0.5 + noiseVector.y).toFloat(),
            (line.start.z - cameraPos.z + 0.5 + noiseVector.z).toFloat()
        )
            .color(line.color.red, line.color.green, line.color.blue, line.color.alpha)
            .normal(1f, 1f, 1f)

        consumer.vertex(
            matrix,
            (line.end.x - cameraPos.x + 0.5 + noiseVector.x).toFloat(),
            (line.end.y - cameraPos.y + 0.5 + noiseVector.y).toFloat(),
            (line.end.z - cameraPos.z + 0.5 + noiseVector.z).toFloat()
        )
            .color(line.color.red, line.color.green, line.color.blue, line.color.alpha)
            .normal(1f, 1f, 1f)
    }
}


