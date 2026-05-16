package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.render.Color
import com.pixelknights.bridgesgame.client.render.DebugLine
import com.pixelknights.bridgesgame.client.util.plus
import net.minecraft.block.LadderBlock
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.concurrent.BlockingQueue


class Path (
    val pathOwner: GameColor?,
) {
    val connections: MutableSet<Connection> = mutableSetOf()
    val floors: MutableSet<Floor> = mutableSetOf()

    fun containsBaseFloor(startingFloor: Floor): Boolean {
        return startingFloor.tower.color == pathOwner && startingFloor.isBase  || floors.any { it.tower.color == pathOwner && it.isBase }
    }


    fun calculateScore(): Int {
        if (pathOwner == null) {
            return 0
        }

        val capturedFloorPoints = floors.count { it.owner == pathOwner }

        val towers = floors.asSequence()
            .map { it.tower }
            .toSet()
        val capturedTowerPoints = towers.sumOf { tower ->
            when (tower.capturingTeam) {
                pathOwner -> tower.getCapturePoints(pathOwner)
                else -> 0
            }
        }

        return capturedFloorPoints + capturedTowerPoints
    }

    /**
     * Build a connection network and validate floors along the way.
     * The [startingFloor] must already be connected to the existing path.
     */
    fun buildPath(startingFloor: Floor, allTowers: List<Tower>, errorChannel: BlockingQueue<String>) {
        if (startingFloor in floors) {
            return
        }
        val containsBaseFloor = containsBaseFloor(startingFloor)

        if (startingFloor.captureColor == pathOwner) {
            startingFloor.isCaptureValidated = containsBaseFloor
        }
        if (startingFloor.paintColor == pathOwner) {
            startingFloor.isPaintValidated = containsBaseFloor
        }

        floors += startingFloor

        for (node in startingFloor.nodes) {
            var nextConnection: Connection? = null

            if (node.connections.size > 1) {
                // Choose the path that is most beneficial to the team and invalidate the others
                val allOptions = node.connections
                    .filter { it.errors.isEmpty() }
                    .associate { connection ->
                    val pathOption = Path(this.pathOwner).also { copy ->
                        copy.connections += (this.connections + connection)
                        copy.floors += (this.floors + startingFloor)
                    }
                    val endNode = connection.nodeB
                    if (endNode != null) {
                        pathOption.buildPath(endNode.floor, allTowers, errorChannel)
                        pathOption.buildPath(connection.nodeA.floor, allTowers, errorChannel)
                        return@associate connection to pathOption
                    } else {
                        return@associate connection to null
                    }
                }
                if (allOptions.size > 1) {
                    errorChannel += "Node ${node.coords} ${node.worldCoords} has multiple connections"
                }

                val bestOption = allOptions.maxBy { option -> option.value?.calculateScore() ?: Int.MIN_VALUE }
                nextConnection = bestOption.key

                // TODO: Send error event for each illegal unused connection.
            } else {
                nextConnection = node.connections.firstOrNull()
            }

            if (pathOwner == null || nextConnection == null) {
                continue
            }

            if (nextConnection.canTeamUse(pathOwner)) {
                connections += nextConnection

                // One of these will be the same as startingFloor and return immediately.
                buildPath(nextConnection.nodeA.floor, allTowers, errorChannel)
                buildPath(nextConnection.nodeB?.floor ?: continue, allTowers, errorChannel)
            }
        }
    }

    fun createDebugLines(world: World, config: ModConfig): List<DebugLine> {
        val color = Color.fromHex(pathOwner?.rgba ?: 0)

        return connections.mapNotNull { connection ->
            val endNode = connection.nodeB ?: return@mapNotNull null
            when (connection) {
                is Ladder -> {
                    // Offset by the ladder's facing direction so the line isn't hidden inside the beacon beam
                    val ladderBlock = world.getBlockState(connection.nodeA.floor.worldCenter)
                    val facing = ladderBlock?.get(LadderBlock.FACING) ?: Direction.NORTH
                    val startPos = connection.nodeA.floor.worldCenter + facing.vector
                    val endPos = endNode.floor.worldCenter + facing.vector
                    DebugLine(startPos, endPos, color)
                }
                is Bridge -> DebugLine(connection.nodeA.worldPosition, endNode.worldPosition, color)
                is Circuit -> null
            }
        }
    }
}
