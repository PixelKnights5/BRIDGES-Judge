package com.pixelknights.bridgesgame.client.config

import com.pixelknights.bridgesgame.client.MOD_ID
import com.pixelknights.bridgesgame.client.MOD_LOGGER
import com.pixelknights.bridgesgame.client.game.entity.GameColor
import kotlinx.io.IOException
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class TowerLayoutConfig(
    val logger: Logger
) : KoinComponent {

    private val heightMap: MutableList<MutableList<Int?>> = mutableListOf()
    private val colorMap: MutableList<MutableList<GameColor?>> = mutableListOf()
    private val baseMap: MutableMap<Array<Int>, GameColor> = mutableMapOf()

    private val mc: MinecraftClient by inject()


    init {
        MOD_LOGGER.info("Initializing TowerLayoutConfig")
        loadHeightmap()
        loadColorMap()
        MOD_LOGGER.info("Finished initializing TowerLayoutConfig")

    }

    fun getHeight(row: Int, col: Int): Int? {
        return heightMap[row][col]
    }

    fun getColor(row: Int, col: Int): GameColor? {
        return colorMap[row][col]
    }

    fun isTeamBase(row: Int, col: Int): Boolean {
        // Sadly, looking up the the value by map key does not work with array keys, so we must explicitly search based on content.
        return baseMap
            .filter {
                it.key contentEquals arrayOf(row, col)
            }
            .any()

    }

    private fun loadColorMap() {
        val id = Identifier.of(MOD_ID, COLORMAP_PATH)
        val resource = mc.resourceManager.getResource(id)

        if (resource.isEmpty) {
            throw IOException("Color map could not be loaded")
        }

        for ((row, line) in resource.get().reader.lines().iterator().withIndex()) {
            val tmp = line.split(' ')
                .mapIndexed { col, code ->
                    val color = GameColor.fromChar(code)
                    if (color != null && color.colorCode.uppercase() == code) {
                        baseMap[arrayOf(row, col)] = color
                    }
                    return@mapIndexed color
                }
                .toMutableList()

            colorMap.add(tmp)
        }
    }

    private fun loadHeightmap() {
        val id = Identifier.of(MOD_ID, HEIGHTMAP_PATH)
        val resource = mc.resourceManager.getResource(id)

        if (resource.isEmpty) {
            throw IOException("Height map could not be loaded")
        }

        for (line in resource.get().reader.lines().iterator()) {
            val tmp = line.split(' ')
                .map { height ->
                    if (height == ".") {
                        return@map null
                    } else {
                        return@map Integer.parseInt(height)
                    }
                }
                .toMutableList()
            heightMap.add(tmp)
        }
    }

    companion object {
        private const val COLORMAP_PATH = "config/tower_color_layout.txt"
        private const val HEIGHTMAP_PATH = "config/tower_height_layout.txt"
    }
}