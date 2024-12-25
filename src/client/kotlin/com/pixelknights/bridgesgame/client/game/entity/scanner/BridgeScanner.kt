package com.pixelknights.bridgesgame.client.game.entity.scanner

import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.game.entity.Bridge
import com.pixelknights.bridgesgame.client.game.entity.Node
import com.pixelknights.bridgesgame.client.game.rules.BridgeTemplate
import com.pixelknights.bridgesgame.client.util.plus
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.Logger

class BridgeScanner (
    private val logger: Logger,
    private val config: ModConfig,
    private val mc: MinecraftClient,
) {

    fun getBridgesForNode(node: Node, allNodes: List<Node>): Set<Bridge> {
        return BridgeTemplate.ALL_BRIDGE_COMBINATIONS.flatten().map { template ->
            val owner = template.findBridgeOwner(mc, config, node.worldCoords)
            val painter = if (owner != null) {
                template.findBridgeOwner(mc, config, node.worldCoords.up())
            } else {
                null
            }

            val endNode = allNodes.first { it.worldCoords == (node.worldCoords + template.targetNodeOffset) }

            return@map Bridge(
                blocks = template.blockCoords.map(::BlockPos),
                startNode = node,
                endNode = endNode,
                owner = owner,
                painter = painter
            )
        }.toSet()
    }


}

