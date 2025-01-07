package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.render.Color
import com.pixelknights.bridgesgame.client.render.DebugLine
import net.minecraft.block.LadderBlock
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import com.pixelknights.bridgesgame.client.util.plus


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
    fun buildPath(startingFloor: Floor, allTowers: List<Tower>) {
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

            buildPath(aboveFloor, allTowers)
        }

        // Take ladders going down
        val belowFloor = allTowers
            .asSequence()
            .filter { it == startingFloor.tower }
            .flatMap { it.floors }
            .firstOrNull { it.floorNumber == startingFloor.floorNumber - 1 }

        if (belowFloor?.hasLadder == true) {
            buildPath(belowFloor, allTowers)
        }

        // Take all usable bridges
        for (node in startingFloor.nodes) {
            if (node.connectedBridges.size > 1) {
//                println("Node at ${node.worldCoords} is connected to multiple bridges")
//                continue
            }

            // TODO: Choose the bridge that is most beneficial to the team
            val nextBridge = node.connectedBridges.firstOrNull()
            if ( pathOwner == null || nextBridge == null) {
                continue
            }

            if (nextBridge.canTeamUseBridge(pathOwner)) {
                bridges += nextBridge

                // One of these will be the same as startingFloor and return immediately.
                buildPath(nextBridge.startNode.floor, allTowers)
                buildPath(nextBridge.endNode?.floor ?: continue, allTowers)
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
            return@map DebugLine(it.startNode.worldCoords, it.endNode.worldCoords, color)
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