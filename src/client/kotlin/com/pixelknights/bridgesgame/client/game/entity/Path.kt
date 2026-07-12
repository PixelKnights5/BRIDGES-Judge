package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.render.Color
import com.pixelknights.bridgesgame.client.render.RenderedLine
import com.pixelknights.bridgesgame.client.util.plus
import com.pixelknights.bridgesgame.client.util.randomFloat
import net.minecraft.block.LadderBlock
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World


class Path (
    val pathOwner: GameColor?,
    private val scoring: TowerScoring,
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
                pathOwner -> scoring.getCapturePoints(pathOwner, tower.color)
                else -> 0
            }
        }

        return capturedFloorPoints + capturedTowerPoints
    }

    /**
     * Build a connection network and validate floors along the way.
     * The [startingFloor] must already be connected to the existing path.
     */
    fun buildPath(startingFloor: Floor, allTowers: List<Tower>) {
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
            var nextConnection: Connection?

            if (node.connections.size > 1) {
                // Choose the path that is most beneficial to the team and invalidate the others
                val allOptions = node.connections
                    .filter { it.errors.isEmpty() }
                    .associate { connection ->
                    val pathOption = Path(this.pathOwner, scoring).also { copy ->
                        copy.connections += (this.connections + connection)
                        copy.floors += (this.floors + startingFloor)
                    }
                    val endNode = connection.nodeB
                    if (endNode != null) {
                        pathOption.buildPath(endNode.floor, allTowers)
                        pathOption.buildPath(connection.nodeA.floor, allTowers)
                        return@associate connection to pathOption
                    } else {
                        return@associate connection to null
                    }
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
                buildPath(nextConnection.nodeA.floor, allTowers)
                buildPath(nextConnection.nodeB?.floor ?: continue, allTowers)
            }
        }
    }

    fun createRenderedLines(world: World, config: ModConfig): List<RenderedLine> {
        val color = Color.fromHex(pathOwner?.rgba ?: 0)

        return connections.flatMap { connection ->
            connection.nodeB ?: return@flatMap emptyList()
            when (connection) {
                is Ladder -> {
                    // Offset by the ladder's facing direction so the line isn't hidden inside the beacon beam
                    val ladderBlock = world.getBlockState(connection.nodeA.floor.worldCenter)
                    val facing = ladderBlock?.get(LadderBlock.FACING) ?: Direction.NORTH
                    connection.segments.map { seg ->
                        RenderedLine(seg.start + facing.vector, seg.end + facing.vector, color)
                    }
                }
                is Bridge -> {
                    // Segments now track the real (possibly diagonal, multi-block) glass
                    // footprint for intersection detection, not a single visual line. Render
                    // a clean node-to-node line at head height instead of the footprint.
                    listOf(RenderedLine(connection.nodeA.worldPosition, connection.nodeB!!.worldPosition, color))
                }
                is Circuit -> {
                    // Shared noise vector across all segments so corners connect visually
                    val noiseVec = Vec3d(
                        (-0.5..0.5).randomFloat().toDouble() / 2,
                        (-0.5..0.5).randomFloat().toDouble() / 2,
                        (-0.5..0.5).randomFloat().toDouble() / 2,
                    )
                    connection.segments.map { seg ->
                        RenderedLine(seg.start.up(2), seg.end.up(2), color, noiseVectorOverride = noiseVec)
                    }
                }
            }
        }
    }
}
