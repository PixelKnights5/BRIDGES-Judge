package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.game.entity.Ladder
import com.pixelknights.bridgesgame.client.game.entity.NodeSide
import com.pixelknights.bridgesgame.client.game.entity.Tower
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient

class LadderScanner(private val mc: MinecraftClient) {

    fun getLaddersForTower(tower: Tower): List<Ladder> {
        val ladders = mutableListOf<Ladder>()

        for (i in 0 until tower.floors.size - 1) {
            val lowerFloor = tower.floors[i]
            val upperFloor = tower.floors[i + 1]

            val hasLadder = mc.world?.getBlockState(lowerFloor.worldCenter)?.block == Blocks.LADDER
            if (!hasLadder) continue

            val lowerCenterNode = lowerFloor.nodes.first { it.side == NodeSide.CENTER }
            val upperCenterNode = upperFloor.nodes.first { it.side == NodeSide.CENTER }

            ladders += Ladder(
                nodeA = lowerCenterNode,
                nodeB = upperCenterNode,
                blocks = listOf(lowerFloor.worldCenter),
            )
        }

        return ladders
    }
}
