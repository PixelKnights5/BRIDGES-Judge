package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.game.entity.Floor
import com.pixelknights.bridgesgame.client.game.entity.Tower
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import org.koin.core.component.KoinComponent

class FloorScanner(
    private val config: ModConfig,
    private val mc: MinecraftClient
) : KoinComponent {

    fun getFloor(tower: Tower, centerCoordinate: BlockPos, floor: Int): Floor {
        // Position of the floor in the world
        val worldFloorPosition = tower.worldCoordinates(centerCoordinate, config)
        // Position of the floor's center block in the world
        val worldCenterPosition = worldFloorPosition.mutableCopy().add(
            0,
            config.towerConfig.blocksBetweenFloors / 2,
            0
        )

        val hasLadder = (mc.world?.getBlockState(worldCenterPosition) == Blocks.LADDER)

        return Floor(
            floorNumber = floor,
            hasLadder = hasLadder,
            worldCenter = worldCenterPosition,
        )
    }
}