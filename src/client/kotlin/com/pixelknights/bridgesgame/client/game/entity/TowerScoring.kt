package com.pixelknights.bridgesgame.client.game.entity

import kotlin.math.pow

class TowerScoring(basePositions: Map<GameColor, Pair<Int, Int>>) {

    private val bonusByTeam: Map<GameColor, Map<GameColor, Int>> = buildBonusTable(basePositions)

    fun getCapturePoints(team: GameColor, towerColor: GameColor): Int {
        return when (towerColor) {
            GameColor.WHITE -> CENTER_BONUS
            GameColor.GREY -> NEUTRAL_BONUS
            else -> bonusByTeam[team]?.get(towerColor) ?: 0
        }
    }

    private fun buildBonusTable(basePositions: Map<GameColor, Pair<Int, Int>>): Map<GameColor, Map<GameColor, Int>> {
        return GameColor.entries
            .filter { it.isTeam }
            .associate { team ->
                val homePos = basePositions[team]

                // Rank the other 5 teams by distance from this team's base; assign bonuses by rank.
                val otherTeamsSortedByDistance = GameColor.entries
                    .filter { it.isTeam && it != team }
                    .sortedBy { other -> squaredDistanceFromHome(homePos, basePositions[other]) }

                val bonuses = mutableMapOf<GameColor, Int>(team to OWN_BONUS)
                DISTANCE_BONUS.forEachIndexed { index, bonus ->
                    bonuses[otherTeamsSortedByDistance[index]] = bonus
                }

                team to bonuses.toMap()
            }
    }

    // Squared distance is used as a sort key only — avoids sqrt() since only rank matters.
    private fun squaredDistanceFromHome(homePosition: Pair<Int, Int>?, otherPosition: Pair<Int, Int>?): Int {
        if (homePosition == null || otherPosition == null) {
            return 0
        }
        return (homePosition.first - otherPosition.first).toDouble().pow(2).toInt() +
                (homePosition.second - otherPosition.second).toDouble().pow(2).toInt()
    }

    companion object {
        private const val OWN_BONUS = 0
        private const val NEUTRAL_BONUS = 1
        private const val CENTER_BONUS = 2
        // Bonuses assigned by ascending distance rank (nearest to farthest among the 5 other teams)
        private val DISTANCE_BONUS = listOf(1, 1, 2, 2, 3)
    }
}