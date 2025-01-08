package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.game.entity.*
import com.pixelknights.bridgesgame.client.util.getTeamColorForBlock
import com.pixelknights.bridgesgame.client.util.plus
import com.pixelknights.bridgesgame.client.util.times
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
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
    fun getFloor(tower: Tower, centerCoordinate: BlockPos, floorNum: Int): Floor {
        val blocksBetweenFloors = config.towerConfig.blocksBetweenFloors
        val worldFloorPosition = tower.worldCoordinates(centerCoordinate, config)
            .up(floorNum * blocksBetweenFloors)

        val worldCenterPosition = worldFloorPosition.up(blocksBetweenFloors / 2)

        val hasLadder = (mc.world?.getBlockState(worldCenterPosition)?.block == Blocks.LADDER)

        val claimingTeam = getCapturingTeam(worldFloorPosition)
        val paintingTeam = getCapturingTeam(worldFloorPosition.up())

        val floor = Floor(
            floorNumber = floorNum,
            hasLadder = hasLadder,
            tower = tower,
            worldCenter = worldCenterPosition,
            captureColor = claimingTeam,
            paintColor = paintingTeam
        )
        floor.nodes = NodeSide.entries.map { side -> getNode(floor, side) }.toList()
        return floor
    }

    private fun getNode(floor: Floor, side: NodeSide): Node {
        val worldCoords = floor.worldCenter + (side.vector * Node.DISTANCE_FROM_CENTER)
        val isOpen = mc.world?.getBlockState(worldCoords)?.isAir

        return Node(
            side = side,
            isOpen = isOpen ?: false,
            floor = floor,
            worldCoords = worldCoords,
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