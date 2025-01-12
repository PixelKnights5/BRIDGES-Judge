package com.pixelknights.bridgesgame.client.render

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


    fun renderAllText(context: WorldRenderContext) {
        val fontHeight = mc.textRenderer.fontHeight * SCALE

        textToRender
            .forEach { textBlock ->
                val position = textBlock.worldPos.toVector3f()
                textBlock.lines.forEachIndexed { index, line ->
                    val offset = Vector3f(0f, (textBlock.lines.size - index) * fontHeight, 0f)
                    renderText(context, line, position - offset)
                }
            }
//            .forEach { renderText(context, it.lines.first(), it.worldPos) }
    }

//    private fun renderLine(line: TextLine, pos: Vec3i, context: WorldRenderContext) {
//        val camera = mc.gameRenderer.camera
//        if (!camera.isReady || mc.entityRenderDispatcher.gameOptions == null) {
//            // Not ready yet
//            return
//        }
//        val vertexConsumers = context.consumers() ?: return
//
//        val renderer = mc.textRenderer
//        val matrices = context.matrixStack() ?: throw IllegalStateException("MatrixStack is null")
//        matrices.push()
//        val offset = pos.toVec3d() - camera.pos
//
//        matrices.translate(offset.x, offset.y, offset.z)
//        matrices.translate(0.5, 1.0, 0.5)
////        matrices.multiplyPositionMatrix(Matrix4f().rotation(camera.rotation.invert()))
//        matrices.scale(-0.04f, -0.04f, 0.04f)
//
//        RenderSystem.applyModelViewMatrix()
//
//        val xPos: Float = -mc.textRenderer.getWidth(line.text) / 2f
//        renderer.draw(line.text, xPos, 0f, -16777216, false, matrices.peek().positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE)
//
//
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.enableDepthTest();
//        matrices.pop()
//        RenderSystem.applyModelViewMatrix();
//    }

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

//
//    private fun renderText(text: HoveringText, context: WorldRenderContext) {
//        val camera = context.camera()
//        val matrices = context.matrixStack() ?: throw IllegalStateException("MatrixStack is null")
//        val vertexConsumers = context.consumers() ?: return
//        var baseHeight = text.worldPos.toVec3d()
////        print/**/("Rendering ${text.lines}")
//
//        val playerPos = mc.player!!.pos
//        val angleX = atan2(baseHeight.x, playerPos.x).toFloat()
//        val angleZ = atan2(baseHeight.z, playerPos.z).toFloat()
//        val rot = Quaternionf().rotationXYZ(angleX, 0.0f, angleZ);
//
//        text.lines.forEach { line ->
//            matrices.push()
//
////            matrices.multiply(rot)
//            matrices.translate(baseHeight.x, baseHeight.y, baseHeight.z)
//            matrices.translate(0.5, 1.0, 0.5)
//            matrices.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z)
////            matrices.multiply(camera.rotation)
//            matrices.multiply(rot)
//
//            matrices.scale(-0.04f, -0.04f, 0.04f)
//
//            val xPos: Float = -mc.textRenderer.getWidth(line.text) / 2f
//
////            RenderSystem.applyModelViewMatrix();
////            RenderSystem.disableCull();
////            RenderSystem.enableBlend();
////            RenderSystem.defaultBlendFunc();
////            RenderSystem.disableDepthTest();
//
//            val positionMatrix = matrices.peek().positionMatrix
//            mc.textRenderer.draw(line.text, xPos, 0f, -16777216, false, positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE)
//
//
//            matrices.pop()
////            RenderSystem.applyModelViewMatrix();
////            RenderSystem.enableCull();
////            RenderSystem.disableBlend();
////            RenderSystem.enableDepthTest();
//        }
//    }
//    matrices.translate(baseHeight.x, baseHeight.y, baseHeight.z)
//    matrices.multiply(mc.entityRenderDispatcher.rotation)
//    matrices.scale(0.025f, -0.025f, 0.025f)
//    val positionMatrix = matrices.peek().positionMatrix
//    val opacity = (0.25f * 255f).toInt() shl 24
//    val g = (-mc.textRenderer.getWidth(line.text) / 2).toFloat()
//    //	public int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light) {
//    mc.textRenderer.draw(line.text, g, 0f, -16777216, false, positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LIGHT)

//
//    public static void renderText(String text, BlockPos pos) {
//        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
//        Vec3d cameraPos = camera.getPos();
//
//        matrixStack.push();
//        float angleX = camera.getPitch() * (float) (Math.PI / 180.0);
//        float angleY = (camera.getYaw() + 180) * (float) (Math.PI / 180.0);
//        Quaternionf rot = new Quaternionf().rotationXYZ(angleX, angleY, 0.0F);
//        matrixStack.multiply(rot);
//        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
//        matrixStack.translate(pos.getX()+0.5, pos.getY()+1.5, pos.getZ()+0.5);
//        matrixStack.multiply(camera.getRotation());
//        matrixStack.scale(-0.04F, -0.04f, 0.04f);
//
//        RenderSystem.setShader(GameRenderer::getRenderTypeTextProgram);
//        RenderSystem.applyModelViewMatrix();
//        RenderSystem.disableCull();
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.disableDepthTest();
//
//        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
//        VertexConsumerProvider.Immediate consumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
//        textRenderer.draw(text, -textRenderer.getWidth(text) / 2f, 0f, new Color(255, 255, 255).getRGB(), false, positionMatrix, consumers, TextRenderer.TextLayerType.NORMAL, new Color(0, 0, 0, 70).getRGB(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
//
//        matrixStack.pop();
//        RenderSystem.applyModelViewMatrix();
//        RenderSystem.enableCull();
//        RenderSystem.disableBlend();
//        RenderSystem.enableDepthTest();
//    }

    companion object {
        private const val MAX_DISTANCE = 4096.0
        private const val TEXT_HEIGHT = 1.0
        private const val LIGHT = 15728880
        private val TEXT_OFFSET = Vec3d(0.5, 1.0, 0.5)
        private const val SCALE = 0.04f
    }
}