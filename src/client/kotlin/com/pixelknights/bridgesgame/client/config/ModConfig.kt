package com.pixelknights.bridgesgame.client.config

import com.google.gson.GsonBuilder
import com.pixelknights.bridgesgame.client.MOD_ID
import com.pixelknights.bridgesgame.client.MOD_LOGGER
import kotlinx.io.IOException
import net.fabricmc.loader.api.FabricLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.pathString

@Suppress("MemberVisibilityCanBePrivate")
class ModConfig {

    val configVersion: Int = 1
    val boardConfig: BoardConfig = BoardConfig()
    val towerConfig: TowerConfig = TowerConfig()


    inner class BoardConfig(
        val width: Int = 19,
        val height: Int = 19,
        val blocksBetweenTowers: Int = 5,
        val towerDiameter: Int = 5,
        val maxGameHeight: Int = 30,
    ) {
        override fun toString(): String {
            return "BoardConfig(width=$width, height=$height, blocksBetweenTowers=$blocksBetweenTowers, towerDiameter=$towerDiameter, maxGameHeight=$maxGameHeight)"
        }
    }

    inner class TowerConfig (
        val blocksBetweenFloors: Int = 5,
        val orangeTowerCornerBlock: String = "minecraft:waxed_chiseled_copper",
        val markerBlock: String = "minecraft:beacon"
    ) {
        override fun toString(): String {
            return "TowerConfig(blocksBetweenFloors=$blocksBetweenFloors, orangeTowerCornerBlock='$orangeTowerCornerBlock', markerBlock='$markerBlock')"
        }
    }



    fun save() {
        if (!Files.exists(SAVE_DIR)) {
            Files.createDirectories(SAVE_DIR)
        }
        val saveFile = Paths.get(SAVE_DIR.pathString, CONFIG_FILE_NAME)
        val gson = GsonBuilder().setPrettyPrinting().create()

        try {
            MOD_LOGGER.info("Saving mod configuration")
            Files.writeString(saveFile, gson.toJson(this), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        } catch (e: IOException) {
            MOD_LOGGER.error("Failed to save config file", e)
        }
    }

    override fun toString(): String {
        return "ModConfig(configVersion='$configVersion', boardConfig=$boardConfig, towerConfig=$towerConfig)"
    }


    companion object {

        private const val CONFIG_FILE_NAME = "mod_config.json"
        private val SAVE_DIR = Paths.get(FabricLoader.getInstance().configDir.pathString, MOD_ID)

        @JvmStatic
        fun loadConfig(): ModConfig {
            MOD_LOGGER.info("Loading mod configuration")
            val saveFile = Paths.get(SAVE_DIR.pathString, CONFIG_FILE_NAME)
            if (!Files.exists(saveFile)) {
                MOD_LOGGER.info("No existing save file found, creating new one.")
                val config = ModConfig()
                config.save()
                return config
            }

            val gson = GsonBuilder().setPrettyPrinting().create()
            val fileContents = Files.readString(saveFile, StandardCharsets.UTF_8)
            return gson.fromJson(fileContents, ModConfig::class.java)

        }
    }
}