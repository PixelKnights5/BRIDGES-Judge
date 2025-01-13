package com.pixelknights.bridgesgame.client.config

import com.google.gson.GsonBuilder
import com.pixelknights.bridgesgame.client.MOD_ID
import com.pixelknights.bridgesgame.client.MOD_LOGGER
import kotlinx.io.IOException
import kotlinx.serialization.Transient
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.math.BlockPos
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.pathString

data class ModConfig(
    val boardConfig: BoardConfig = BoardConfig(),
    val towerConfig: TowerConfig = TowerConfig(),
    val playerSettings: PlayerSettings = PlayerSettings(),
    val configVersion: Int = 1,
) {

    @Transient
    val spaceBetweenCenters: Int
        get() = boardConfig.blocksBetweenTowers + boardConfig.towerDiameter


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


    companion object {

        private const val CONFIG_FILE_NAME = "mod_config.json"
        private val SAVE_DIR = Paths.get(FabricLoader.getInstance().configDir.pathString, MOD_ID)

        @JvmStatic
        fun loadConfig(saveFile: Path = Paths.get(SAVE_DIR.pathString, CONFIG_FILE_NAME)): ModConfig {
            MOD_LOGGER.info("Loading mod configuration")
//            val saveFile = Paths.get(SAVE_DIR.pathString, CONFIG_FILE_NAME)
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

data class BoardConfig(
    val width: Int = 19,
    val height: Int = 19,
    val blocksBetweenTowers: Int = 5,
    val towerDiameter: Int = 5,
    val maxGameHeight: Int = 30,
    val maxMisplacedBlockTolerance: Int = 2,
)

data class TowerConfig (
    val blocksBetweenFloors: Int = 4,
    val orangeTowerCornerBlock: String = "minecraft:waxed_chiseled_copper",
    val markerBlock: String = "minecraft:beacon"
)

data class PlayerSettings (
    var centerCoordinate: BlockPos = BlockPos.ORIGIN,
)