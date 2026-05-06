package com.pixelknights.bridgesgame.client.render

import com.pixelknights.bridgesgame.client.MOD_ID
import com.pixelknights.bridgesgame.client.config.ModConfig
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.WorldRenderer
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.abs
import kotlin.math.sqrt

class LineRenderer : KoinComponent {
    val linesToRender = mutableSetOf<DebugLine>()

    val config: ModConfig by inject()

    fun renderLines(context: WorldRenderContext) {
        if (!config.playerSettings.showBridgePaths) return

        val matrices = context.matrices()
        val cameraPos = context.worldState().cameraRenderState.pos
        val vertexConsumers = context.consumers() ?: return
        val world = MinecraftClient.getInstance().world ?: return

        val matrix = matrices.peek().positionMatrix

        val consumer = vertexConsumers.getBuffer(RenderLayers.entitySolid(LINE_TEXTURE))
        for (line in linesToRender) {
            val box = computeBox(line, cameraPos) ?: continue
            val midPos = BlockPos(
                (line.start.x + line.end.x) / 2,
                (line.start.y + line.end.y) / 2,
                (line.start.z + line.end.z) / 2,
            )
            val light = WorldRenderer.getLightmapCoordinates(world, midPos)
            drawLineBox(consumer, matrix, box, line.color, light)
        }

//        val outlineConsumer = vertexConsumers.getBuffer(RenderLayers.lines())
//        for (line in linesToRender) {
//            val box = computeBox(line, cameraPos) ?: continue
//            drawLineOutline(outlineConsumer, matrix, box)
//        }
    }

    private fun computeBox(line: DebugLine, cameraPos: Vec3d): Box? {
        val noise = line.noiseVector
        val ax = (line.start.x - cameraPos.x + 0.5 + noise.x).toFloat()
        val ay = (line.start.y - cameraPos.y + 0.5 + noise.y).toFloat()
        val az = (line.start.z - cameraPos.z + 0.5 + noise.z).toFloat()
        val bx = (line.end.x - cameraPos.x + 0.5 + noise.x).toFloat()
        val by = (line.end.y - cameraPos.y + 0.5 + noise.y).toFloat()
        val bz = (line.end.z - cameraPos.z + 0.5 + noise.z).toFloat()

        val dx = bx - ax; val dy = by - ay; val dz = bz - az
        val length = sqrt(dx * dx + dy * dy + dz * dz)
        if (length < 1e-4f) return null

        val ffx = dx / length; val ffy = dy / length; val ffz = dz / length

        // right perpendicular: forward × worldUp, falling back to forward × X for vertical lines
        val rx: Float; val ry: Float; val rz: Float
        if (abs(ffy) < 0.99f) {
            val cx = -ffz; val cz = ffx
            val cl = sqrt(cx * cx + cz * cz)
            rx = cx / cl; ry = 0f; rz = cz / cl
        } else {
            val cy = ffz; val cz = -ffy
            val cl = sqrt(cy * cy + cz * cz)
            rx = 0f; ry = cy / cl; rz = cz / cl
        }

        // up = right × forward
        val ux = ry * ffz - rz * ffy
        val uy = rz * ffx - rx * ffz
        val uz = rx * ffy - ry * ffx

        val t = LINE_THICKNESS / 2f

        fun corner(bx: Float, by: Float, bz: Float, sr: Float, su: Float) = Triple(
            bx + sr * t * rx + su * t * ux,
            by + sr * t * ry + su * t * uy,
            bz + sr * t * rz + su * t * uz,
        )

        val (ablX, ablY, ablZ) = corner(ax, ay, az, -1f, -1f)
        val (abrX, abrY, abrZ) = corner(ax, ay, az,  1f, -1f)
        val (atrX, atrY, atrZ) = corner(ax, ay, az,  1f,  1f)
        val (atlX, atlY, atlZ) = corner(ax, ay, az, -1f,  1f)
        val (bblX, bblY, bblZ) = corner(bx, by, bz, -1f, -1f)
        val (bbrX, bbrY, bbrZ) = corner(bx, by, bz,  1f, -1f)
        val (btrX, btrY, btrZ) = corner(bx, by, bz,  1f,  1f)
        val (btlX, btlY, btlZ) = corner(bx, by, bz, -1f,  1f)

        return Box(
            ablX, ablY, ablZ, abrX, abrY, abrZ, atrX, atrY, atrZ, atlX, atlY, atlZ,
            bblX, bblY, bblZ, bbrX, bbrY, bbrZ, btrX, btrY, btrZ, btlX, btlY, btlZ,
            ffx, ffy, ffz, rx, ry, rz, ux, uy, uz,
            length / LINE_THICKNESS,
        )
    }

    private fun drawLineBox(
        consumer: VertexConsumer,
        matrix: Matrix4f,
        box: Box,
        color: Color,
        light: Int,
    ) {
        val r = color.red; val g = color.green; val b = color.blue; val a = color.alpha
        val overlay = OverlayTexture.DEFAULT_UV
        val uMax = box.uTiles

        // Long faces: u runs along the line (0..uMax), v wraps once across the thickness
        // Right (+right): bbr → btr → atr → abr (reversed for outward winding under U=R×F frame)
        drawFace(consumer, matrix, r, g, b, a, light, overlay, box.rx, box.ry, box.rz,
            box.bbrX, box.bbrY, box.bbrZ, uMax,  0f,
            box.btrX, box.btrY, box.btrZ, uMax,  1f,
            box.atrX, box.atrY, box.atrZ, 0f,    1f,
            box.abrX, box.abrY, box.abrZ, 0f,    0f)
        // Top (+up): atl → atr → btr → btl
        drawFace(consumer, matrix, r, g, b, a, light, overlay, box.ux, box.uy, box.uz,
            box.atlX, box.atlY, box.atlZ, 0f,    0f,
            box.atrX, box.atrY, box.atrZ, 0f,    1f,
            box.btrX, box.btrY, box.btrZ, uMax,  1f,
            box.btlX, box.btlY, box.btlZ, uMax,  0f)
        // Left (-right): btl → bbl → abl → atl (reversed for outward winding under U=R×F frame)
        drawFace(consumer, matrix, r, g, b, a, light, overlay, -box.rx, -box.ry, -box.rz,
            box.btlX, box.btlY, box.btlZ, uMax,  0f,
            box.bblX, box.bblY, box.bblZ, uMax,  1f,
            box.ablX, box.ablY, box.ablZ, 0f,    1f,
            box.atlX, box.atlY, box.atlZ, 0f,    0f)
        // Bottom (-up): abr → abl → bbl → bbr
        drawFace(consumer, matrix, r, g, b, a, light, overlay, -box.ux, -box.uy, -box.uz,
            box.abrX, box.abrY, box.abrZ, 0f,    0f,
            box.ablX, box.ablY, box.ablZ, 0f,    1f,
            box.bblX, box.bblY, box.bblZ, uMax,  1f,
            box.bbrX, box.bbrY, box.bbrZ, uMax,  0f)
        // Start cap (-forward): atl → abl → abr → atr
        drawFace(consumer, matrix, r, g, b, a, light, overlay, -box.ffx, -box.ffy, -box.ffz,
            box.atlX, box.atlY, box.atlZ, 0f, 0f,
            box.ablX, box.ablY, box.ablZ, 0f, 1f,
            box.abrX, box.abrY, box.abrZ, 1f, 1f,
            box.atrX, box.atrY, box.atrZ, 1f, 0f)
        // End cap (+forward): btr → bbr → bbl → btl
        drawFace(consumer, matrix, r, g, b, a, light, overlay, box.ffx, box.ffy, box.ffz,
            box.btrX, box.btrY, box.btrZ, 0f, 0f,
            box.bbrX, box.bbrY, box.bbrZ, 0f, 1f,
            box.bblX, box.bblY, box.bblZ, 1f, 1f,
            box.btlX, box.btlY, box.btlZ, 1f, 0f)
    }

    @Suppress("LongParameterList")
    private fun drawFace(
        consumer: VertexConsumer,
        matrix: Matrix4f,
        r: Float, g: Float, b: Float, a: Float,
        light: Int, overlay: Int,
        nx: Float, ny: Float, nz: Float,
        x1: Float, y1: Float, z1: Float, u1: Float, v1: Float,
        x2: Float, y2: Float, z2: Float, u2: Float, v2: Float,
        x3: Float, y3: Float, z3: Float, u3: Float, v3: Float,
        x4: Float, y4: Float, z4: Float, u4: Float, v4: Float,
    ) {
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).texture(u1, v1).overlay(overlay).light(light).normal(nx, ny, nz)
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a).texture(u2, v2).overlay(overlay).light(light).normal(nx, ny, nz)
        consumer.vertex(matrix, x3, y3, z3).color(r, g, b, a).texture(u3, v3).overlay(overlay).light(light).normal(nx, ny, nz)
        consumer.vertex(matrix, x4, y4, z4).color(r, g, b, a).texture(u4, v4).overlay(overlay).light(light).normal(nx, ny, nz)
    }

    private fun drawLineOutline(consumer: VertexConsumer, matrix: Matrix4f, box: Box) {
        // Cap A
        drawEdge(consumer, matrix, box.ablX, box.ablY, box.ablZ, box.abrX, box.abrY, box.abrZ,  box.rx,  box.ry,  box.rz)
        drawEdge(consumer, matrix, box.abrX, box.abrY, box.abrZ, box.atrX, box.atrY, box.atrZ,  box.ux,  box.uy,  box.uz)
        drawEdge(consumer, matrix, box.atrX, box.atrY, box.atrZ, box.atlX, box.atlY, box.atlZ, -box.rx, -box.ry, -box.rz)
        drawEdge(consumer, matrix, box.atlX, box.atlY, box.atlZ, box.ablX, box.ablY, box.ablZ, -box.ux, -box.uy, -box.uz)
        // Cap B
        drawEdge(consumer, matrix, box.bblX, box.bblY, box.bblZ, box.bbrX, box.bbrY, box.bbrZ,  box.rx,  box.ry,  box.rz)
        drawEdge(consumer, matrix, box.bbrX, box.bbrY, box.bbrZ, box.btrX, box.btrY, box.btrZ,  box.ux,  box.uy,  box.uz)
        drawEdge(consumer, matrix, box.btrX, box.btrY, box.btrZ, box.btlX, box.btlY, box.btlZ, -box.rx, -box.ry, -box.rz)
        drawEdge(consumer, matrix, box.btlX, box.btlY, box.btlZ, box.bblX, box.bblY, box.bblZ, -box.ux, -box.uy, -box.uz)
        // Connecting (along line)
        drawEdge(consumer, matrix, box.ablX, box.ablY, box.ablZ, box.bblX, box.bblY, box.bblZ, box.ffx, box.ffy, box.ffz)
        drawEdge(consumer, matrix, box.abrX, box.abrY, box.abrZ, box.bbrX, box.bbrY, box.bbrZ, box.ffx, box.ffy, box.ffz)
        drawEdge(consumer, matrix, box.atrX, box.atrY, box.atrZ, box.btrX, box.btrY, box.btrZ, box.ffx, box.ffy, box.ffz)
        drawEdge(consumer, matrix, box.atlX, box.atlY, box.atlZ, box.btlX, box.btlY, box.btlZ, box.ffx, box.ffy, box.ffz)
    }

    @Suppress("LongParameterList")
    private fun drawEdge(
        consumer: VertexConsumer,
        matrix: Matrix4f,
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        nx: Float, ny: Float, nz: Float,
    ) {
        consumer.vertex(matrix, x1, y1, z1).color(0, 0, 0, 255).normal(nx, ny, nz).lineWidth(OUTLINE_WIDTH)
        consumer.vertex(matrix, x2, y2, z2).color(0, 0, 0, 255).normal(nx, ny, nz).lineWidth(OUTLINE_WIDTH)
    }

    @Suppress("LongParameterList")
    private data class Box(
        val ablX: Float, val ablY: Float, val ablZ: Float,
        val abrX: Float, val abrY: Float, val abrZ: Float,
        val atrX: Float, val atrY: Float, val atrZ: Float,
        val atlX: Float, val atlY: Float, val atlZ: Float,
        val bblX: Float, val bblY: Float, val bblZ: Float,
        val bbrX: Float, val bbrY: Float, val bbrZ: Float,
        val btrX: Float, val btrY: Float, val btrZ: Float,
        val btlX: Float, val btlY: Float, val btlZ: Float,
        val ffx: Float, val ffy: Float, val ffz: Float,
        val rx: Float, val ry: Float, val rz: Float,
        val ux: Float, val uy: Float, val uz: Float,
        val uTiles: Float,
    )

    private companion object {
        const val LINE_THICKNESS = 0.07f
        // Scaled proportionally to LINE_THICKNESS so the outline matches the box thickness
        // (DotRenderer uses 5px for DOT_SIZE 0.1).
        const val OUTLINE_WIDTH = 1f
        val LINE_TEXTURE: Identifier = Identifier.of(MOD_ID, "textures/misc/line.png")
    }
}