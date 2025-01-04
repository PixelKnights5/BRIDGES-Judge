package com.pixelknights.bridgesgame.client.game.entity

class Path (
    val owningTeam: GameColor?,
) {
    val bridges: MutableSet<Bridge> = mutableSetOf()
    val floors: MutableSet<Floor> = mutableSetOf()

    fun containsBaseTower(): Boolean {
        return floors.any { it.tower.isBase }
    }


    fun calculatePathScore(): Int {
        if (owningTeam == null) {
            return 0
        }

        val capturedFloorPoints = floors.count { it.owner == owningTeam }

        val towers = floors.map { it.tower }.toSet()
        val capturedTowerPoints = towers.sumOf {
            when (it.isCaptureValidated) {
                true -> it.getCapturePoints(owningTeam)
                else -> 0
            }
        }

        return capturedFloorPoints + capturedTowerPoints
    }
}