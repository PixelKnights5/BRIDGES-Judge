package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.render.Color
import com.pixelknights.bridgesgame.client.render.DebugLine


class Path (
    val pathOwner: GameColor?,
) {
    val bridges: MutableSet<Bridge> = mutableSetOf()
    val floors: MutableSet<Floor> = mutableSetOf()

    fun containsBaseTower(): Boolean {
        return floors.any { it.tower.isBase }
    }


    fun calculatePathScore(): Int {
        if (pathOwner == null) {
            return 0
        }

        val capturedFloorPoints = floors.count { it.owner == pathOwner }

        val towers = floors.map { it.tower }.toSet()
        val capturedTowerPoints = towers.sumOf {
            when (it.isCaptureValidated) {
                true -> it.getCapturePoints(pathOwner)
                else -> 0
            }
        }

        return capturedFloorPoints + capturedTowerPoints
    }

    fun buildPath(startingFloor: Floor, allTowers: List<Tower>) {
        if (startingFloor in floors) {
            return
        }
        val containsBaseTower = containsBaseTower()

        if (startingFloor.owner == pathOwner) {
            startingFloor.isCaptureValidated = containsBaseTower
        }
        if (startingFloor.paintColor == pathOwner) {
            startingFloor.isPaintValidated = containsBaseTower()
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
                println("Node at ${node.worldCoords} is connected to multiple bridges")
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

    fun createDebugLines(): List<DebugLine> {
        val color = Color.fromHex(pathOwner?.rgba ?: 0)

        return bridges.map {
            if (it.endNode == null) {
                return@map null
            }
            return@map DebugLine(it.startNode.worldCoords, it.endNode.worldCoords, color)
        }.filterNotNull().toList()
    }
}