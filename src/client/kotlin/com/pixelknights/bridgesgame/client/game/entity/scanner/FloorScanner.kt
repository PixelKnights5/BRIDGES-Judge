package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.game.entity.*
import com.pixelknights.bridgesgame.client.util.getTeamColorForBlock
import com.pixelknights.bridgesgame.client.util.getTeamColorForDye
import com.pixelknights.bridgesgame.client.util.plus
import com.pixelknights.bridgesgame.client.util.times
import net.minecraft.block.entity.BannerBlockEntity
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

        val claimingTeam = getCapturingTeam(worldFloorPosition)
        val paintingTeam = getCapturingTeam(worldFloorPosition.up())
        val blockingTeam = getBlockingTeam(worldCenterPosition)

        val floor = Floor(
            floorNumber = floorNum,
            tower = tower,
            worldCenter = worldCenterPosition,
            captureColor = paintingTeam ?: claimingTeam,
            paintColor = paintingTeam,
            blockingTeamColor = blockingTeam,
            isBase = tower.isBase && floorNum == 0
        )
        floor.nodes = NodeSide.entries.map { side -> getNode(floor, side) }.toList()
        return floor
    }

    private fun getNode(floor: Floor, side: NodeSide): Node {
        val worldCoords = floor.worldCenter + (side.vector * Node.DISTANCE_FROM_CENTER)
        // CENTER is the tower interior — not a valid bridge endpoint
        val brokenByTeam = if (side == NodeSide.CENTER) null else getBrokenTeam(worldCoords)
        // A break banner re-opens the node only if the breaking team owns the floor.
        // An illegal break (wrong team, or no team) leaves the node closed so that
        // bridges into it are still flagged as connecting to a closed node.
        val isValidBreak = brokenByTeam != null && brokenByTeam == floor.owner
        val isOpen = when {
            side == NodeSide.CENTER -> false
            isValidBreak -> true
            else -> mc.world?.getBlockState(worldCoords)?.isAir ?: false
        }

        return Node(
            side = side,
            isOpen = isOpen,
            floor = floor,
            worldPosition = worldCoords,
            brokenByTeam = brokenByTeam,
        )
    }

    /**
     * Break banners are team-colored banners (e.g. a red banner) placed at the node's world position,
     * marking a previously closed node as re-opened. The team is read from the banner block's own
     * base color; any patterns on the banner are decorative and ignored.
     */
    private fun getBrokenTeam(nodePos: BlockPos): GameColor? {
        val bannerEntity = mc.world?.getBlockEntity(nodePos) as? BannerBlockEntity ?: return null
        return getTeamColorForDye(bannerEntity.colorForState)
    }

    /**
     * Scans all node positions on this floor to detect if a team has blocked it.
     * Blocked nodes contain team glass placed directly at the node world position.
     * Blocked nodes are non-air, so isOpen=false falls out automatically — the existing
     * BRIDGE_TO_CLOSED_NODE path handles bridges into them without additional logic.
     */
    private fun getBlockingTeam(worldCenter: BlockPos): GameColor? {
        val teamColors = NodeSide.entries
            .filter { it != NodeSide.CENTER }
            .mapNotNull { side ->
                val nodePos = worldCenter + (side.vector * Node.DISTANCE_FROM_CENTER)
                getTeamColorForBlock(mc.world?.getBlockState(nodePos)?.block)
            }.toSet()

        if (teamColors.size > 1) {
            logger.warn("The floor centered at ($worldCenter) has glass from multiple teams in node positions.")
            return null
        }

        return teamColors.firstOrNull()
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