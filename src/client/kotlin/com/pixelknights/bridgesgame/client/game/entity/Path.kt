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
    val bridges: MutableSet<Bridge> = mutableSetOf()
    val floors: MutableSet<Floor> = mutableSetOf()

    fun containsBaseTower(): Boolean {
        return floors.any { it.tower.isBase }
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
     * Build a bridge network and validate floors along the way.
     * The [startingFloor] must already be connected to the existing path
     */
    fun buildPath(startingFloor: Floor, allTowers: List<Tower>, errorChannel: BlockingQueue<String>) {
        if (startingFloor in floors) {
            return
        }
        val containsBaseTower = containsBaseTower()

        if (startingFloor.owner == pathOwner) {
            startingFloor.isCaptureValidated = containsBaseTower
        }
        if (startingFloor.paintColor == pathOwner) {
            startingFloor.isPaintValidated = containsBaseTower
        }

        floors += startingFloor

        // Take ladders going up
        if (startingFloor.hasLadder) {
            val aboveFloor = allTowers
                .asSequence()
                .filter { it == startingFloor.tower }
                .flatMap { it.floors }
                .first { it.floorNumber == startingFloor.floorNumber + 1 }

            buildPath(aboveFloor, allTowers, errorChannel)
        }

        // Take ladders going down
        val belowFloor = allTowers
            .asSequence()
            .filter { it == startingFloor.tower }
            .flatMap { it.floors }
            .firstOrNull { it.floorNumber == startingFloor.floorNumber - 1 }

        if (belowFloor?.hasLadder == true) {
            buildPath(belowFloor, allTowers, errorChannel)
        }

        // Take all usable bridges
        for (node in startingFloor.nodes) {
            var nextBridge: Bridge? = null

            if (node.connectedBridges.size > 1) {
                // Choose the path that is most beneficial to the team and invalidate the others
                val allOptions = node.connectedBridges
                    .filter { it.errors.isEmpty() }
                    .associate { bridge ->
                    val pathOption = Path(this.pathOwner).also { copy ->
                        copy.bridges += (this.bridges + bridge)
                        copy.floors += (this.floors + startingFloor)
                    }
                    if (bridge.endNode != null) {
                        pathOption.buildPath(bridge.endNode.floor, allTowers, errorChannel)
                        pathOption.buildPath(bridge.startNode.floor, allTowers, errorChannel)
                        return@associate bridge to pathOption
                    } else {
                        return@associate bridge to null
                    }
                }
                if (allOptions.size > 1) {
                    errorChannel += "Node ${node.coords} ${node.worldCoords} has multiple bridges"
                }

                val bestOption = allOptions.maxBy { option -> option.value?.calculateScore() ?: Int.MIN_VALUE }
                nextBridge = bestOption.key
                
                // TODO: Send error event for each illegal unused bridge. 
            } else {
                nextBridge = node.connectedBridges.firstOrNull()
            }

            if ( pathOwner == null || nextBridge == null) {
                continue
            }

            if (nextBridge.canTeamUseBridge(pathOwner)) {
                bridges += nextBridge

                // One of these will be the same as startingFloor and return immediately.
                buildPath(nextBridge.startNode.floor, allTowers, errorChannel)
                buildPath(nextBridge.endNode?.floor ?: continue, allTowers, errorChannel)
            }

//            val nextFloor = if (nextBridge.startNode == node) {
//                nextBridge.endNode?.floor
//            } else {
//                nextBridge.startNode.floor
//            }
//            if (nextFloor == null) {
//                // TODO: Send error event - Node not connected to a floor
//                println("Node at ${node.worldCoords} is not connected to a floor")
//            } else {
//                buildPath(nextFloor)
//            }
        }
    }

    fun createDebugLines(world: World, config: ModConfig): List<DebugLine> {
        val color = Color.fromHex(pathOwner?.rgba ?: 0)

        val bridgeLines = bridges.map {
            if (it.endNode == null) {
                return@map null
            }
            return@map DebugLine(it.startNode.worldPosition, it.endNode.worldPosition, color)
        }.filterNotNull().toList()

        val ladderLines = floors
            .filter { it.hasLadder }
            .map { floor ->
                // offset the coords by 1 block in the direction the ladder is facing so the line doesn't get hidden
                // inside of the beacon beam
                val ladderBlock = world.getBlockState(floor.worldCenter)
                val facing = ladderBlock?.get(LadderBlock.FACING) ?: Direction.NORTH
                val startPos = floor.worldCenter + facing.vector
                val endPath = startPos.up(config.towerConfig.blocksBetweenFloors)

                DebugLine(startPos, endPath, color)
            }
            .toList()

        return bridgeLines + ladderLines
    }
}