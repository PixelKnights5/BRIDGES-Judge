package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.game.entity.Floor
import com.pixelknights.bridgesgame.client.game.entity.GameColor
import com.pixelknights.bridgesgame.client.util.getTeamColorForBlock
import com.pixelknights.bridgesgame.client.game.entity.Tower
import com.pixelknights.bridgesgame.client.util.plus

import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import org.apache.logging.log4j.Logger
import org.koin.core.component.KoinComponent

class FloorScanner(
    private val logger: Logger,
    private val config: ModConfig,
    private val mc: MinecraftClient
) : KoinComponent {

    /**
     * This method scans the world to determine information about the specified floor.
     */
    fun getFloor(tower: Tower, centerCoordinate: BlockPos, floor: Int): Floor {
        val blocksBetweenFloors = config.towerConfig.blocksBetweenFloors
        val worldFloorPosition = tower.worldCoordinates(centerCoordinate, config) + Vec3i(
            0,
            floor * blocksBetweenFloors,
            0
        )

        val worldCenterPosition = worldFloorPosition + Vec3i(
            0,
            (blocksBetweenFloors / 2),
            0
        )

        val hasLadder = (mc.world?.getBlockState(worldCenterPosition)?.block == Blocks.LADDER)

        val claimingTeam = getCapturingTeam(worldFloorPosition)
        val paintingTeam = getCapturingTeam(worldFloorPosition.up())

        return Floor(
            floorNumber = floor,
            hasLadder = hasLadder,
            worldCenter = worldCenterPosition,
            captureColor = claimingTeam,
            paintColor = paintingTeam
        )
    }

    /**
     * This method will check 1 block away in each cardinal direction from [worldFloorPosition]
     * to determine if a team has claimed it. It does NOT validate if the claiming team can legally claim the floor.
     * The method is valid for both paints and claims, since the logic is the same.
     */
    private fun getCapturingTeam(worldFloorPosition: BlockPos): GameColor? {
        // Check the claim state of each of the 4 floor claim blocks, and remove duplicates
        val teamClaims = listOf(
            worldFloorPosition.north(),
            worldFloorPosition.east(),
            worldFloorPosition.south(),
            worldFloorPosition.west(),
        )
            .mapNotNull { coords ->
                getTeamColorForBlock(mc.world?.getBlockState(coords)?.block)
            }.toSet()

        if (teamClaims.size > 1) {
            // TODO: Send a validation error event
            logger.warn("The floor at ($worldFloorPosition) has multiple claiming teams. Determining the floor unclaimed.")
            return null
        }

        return teamClaims.firstOrNull()

    }
}