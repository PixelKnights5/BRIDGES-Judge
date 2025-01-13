package com.pixelknights.bridgesgame.client.game.rules


import com.pixelknights.bridgesgame.client.game.entity.GameColor
import com.pixelknights.bridgesgame.client.util.*
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i

class BridgeTemplate(
    val blockCoords: List<Vec3i>,
    /**
     * This value shows the offset from the current node to the node that the bridge goes to
     */
    val targetNodeOffset: Vec3i,
    val isCornerTemplate: Boolean,
) {

    /**
     * Copy the existing template, but rotate it by a given number of degrees about the y axis.
     * The input angle must be divisible by 90 degrees, otherwise the calculation will be incorrect.
     */
    fun rotateTemplate(degrees: Int): BridgeTemplate {
        val newCoords = blockCoords.map { coordinate ->
            coordinate.rotateBy(degrees)
        }.toList()

        return BridgeTemplate(newCoords, targetNodeOffset.rotateBy(degrees), isCornerTemplate)
    }

    private fun flipZ(): BridgeTemplate =
        BridgeTemplate(this.blockCoords.map {
            Vec3i(it.x, it.y, -it.z)
        }, Vec3i(targetNodeOffset.x, targetNodeOffset.y, -targetNodeOffset.z), isCornerTemplate)

    /**
     * Shift the bridge over by [distance] blocks
     */
    fun translate(distance: Vec3i): BridgeTemplate {
        return BridgeTemplate(blockCoords.map { it + distance }, targetNodeOffset + distance, isCornerTemplate)
    }


    fun findBridgePainter(mc: MinecraftClient, nodeCoords: BlockPos): GameColor? {
        val nodeTemplate = this.translate(nodeCoords)
        val blockColors = nodeTemplate.blockCoords.map {
            mc.world?.getBlockState(it)
        }.map {
            getTeamColorForBlock(it?.block)
        }.groupBy { it }.map { it.key to it.value.size }
        val primaryColor = blockColors.filter { it.first != null }.maxByOrNull { it.second }
        if (primaryColor == null) {
            return null
        }
        val numMissingBlocks = nodeTemplate.blockCoords.size - primaryColor.second


        return if (numMissingBlocks <= (nodeTemplate.blockCoords.size / 2) + 1) {
            primaryColor.first
        } else {
            null
        }
    }

    /**
     * Check who owns this bridge (if anyone). Returns `null` if this bridge does not exist.
     */
    fun findBridgeOwner(mc: MinecraftClient, nodeCoords: BlockPos): GameColor? {
        val nodeTemplate = this.translate(nodeCoords)
        val blockColors = nodeTemplate.blockCoords.map {
            mc.world?.getBlockState(it)
        }.map {
            getTeamColorForBlock(it?.block)
        }.groupBy { it }.map { it.key to it.value.size }

        val primaryColor = blockColors.maxBy { it.second }

        // If there are missing blocks, it's not a bridge.
        val numMissingBlocks = nodeTemplate.blockCoords.size - primaryColor.second
        if (numMissingBlocks > 1) {
            return null
        }


        // Technically there could be multiple colors if someone made a mistake, but it should not
        // happen in normal gameplay. If it does, the team that has the most blocks will be considered the
        // owner. TODO: Consider sending a warning event if this happens.
//        return GameColor.RED
////        return blockColors.values.flatten().filterNotNull().first()
        return primaryColor.first
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BridgeTemplate) return false

        // This intentionally does not check targetNodeOffset so that bridges in the opposite direction are
        // because bridges are not directed.
        return !(blockCoords.size == other.blockCoords.size &&
                blockCoords.containsAll(other.blockCoords))
    }

    override fun hashCode(): Int {
        return blockCoords.hashCode()
    }

    override fun toString(): String {
        return "BridgeTemplate(blockCoords=$blockCoords, targetNodeOffset=$targetNodeOffset)"
    }


    companion object {
        val ALL_BRIDGE_COMBINATIONS = setOf(getCornerTemplates(), getCardinalTemplates())

        /**
         * Get a list of all possible bridges that could come from a floor
         * from a corner node.
         * Coordinates are relative to the center of the floor
         */
        private fun getCornerTemplates(): Set<BridgeTemplate> {
            // These coordinates are relative to the NODE, not the
            // center of the tower. It will be fixed later, but makes the
            // maths a bit simpler
            val coreBridges = mutableSetOf<BridgeTemplate>()

            val straightBridge = BridgeTemplate((1 until 6).map {
                Vec3i(it, 0, 0)
            }, Vec3i(6, 0, 0), true)
            coreBridges.add(straightBridge)

            val diagonalBridge = BridgeTemplate((0 until 6).map {
                Vec3i(it+1, 0, it+1)
            }, Vec3i(6, 0, 6), true)
            coreBridges.add(diagonalBridge)
            coreBridges.add(diagonalBridge.flipZ())


            val reverseDiagonalBridge = BridgeTemplate((0 until 6).map {
                Vec3i(it+1, 0, -it)
            }, Vec3i(6, 0, -4), true)
            coreBridges.add(reverseDiagonalBridge)
            coreBridges.add(reverseDiagonalBridge.flipZ())


            // There are only 2 2-step bridges that we need to consider.
            // The other 2 possibilities will be picked up by another
            // tower's corner.
            var twoStepDiagonal = BridgeTemplate((0 until 10).map {
                Vec3i((it/2)+1, 0, it+1)
            }, Vec3i(6, 0, 10), true)
            coreBridges.add(twoStepDiagonal)
            twoStepDiagonal = BridgeTemplate((0 until 10).map {
                Vec3i(it+1, 0, (it/2)+1)
            }, Vec3i(10, 0, 6), true)
            coreBridges.add(twoStepDiagonal)

            // There are 4 3-step bridge options.
            var threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i((it/3)+1, 0, it+1)
            }, Vec3i(6, 0, 16), true)
            coreBridges.add(threeStepDiagonal)

            threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i(it+1, 0, (it/3)+1)
            }, Vec3i(16, 0, 6), true)
            coreBridges.add(threeStepDiagonal)

            threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i((-it/3)+1, 0, it)
            }, Vec3i(-6, 0, 16), true)
            coreBridges.add(threeStepDiagonal)

            threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i(it, 0, (-it/3)+1)
            }, Vec3i(16, 0, -6), true)
            coreBridges.add(threeStepDiagonal)

            val translationVector = Vec3i(2, 0, 2)
            val floorSpaceBridges = coreBridges.map { bridgeTemplate ->
                bridgeTemplate.translate(translationVector)
            }

            // floorSpaceBridges only contains bridges coming out of the East node.
            // Get the same bridges for the other 3 cardinal directions
            val allTemplates = intArrayOf(0, 90, 180, 270).map { angle ->
                val negativeTranslationVector = (translationVector * -1).rotateBy(angle)
                floorSpaceBridges.map {
                    it
                        .rotateTemplate(angle)
                        .translate(negativeTranslationVector)
                }
            }.flatten().toMutableSet()

            return allTemplates
        }

        /**
         * Get a list of all possible bridges that could come from a
         * floor in a cardinal direction (i.e. Not a corner node).
         * Coordinates are relative to the center of the floor
         */
        private fun getCardinalTemplates(): Set<BridgeTemplate> {
            // These coordinates are relative to the NODE, not the
            // center of the tower. It will be fixed later, but makes the
            // maths a bit simpler
            val coreBridges = mutableSetOf<BridgeTemplate>()
            val straightBridge = BridgeTemplate((1 .. 5).map {
                Vec3i(it, 0, 0)
            }, Vec3i(6, 0, 0), false)
            coreBridges.add(straightBridge)

            val diagonalBridge = BridgeTemplate((0 until 8).map {
                Vec3i(it+1, 0, it)
            }, Vec3i(8, 0, 8), false)
            // Get the diagonal in forward and reverse directions
            coreBridges.add(diagonalBridge)
            coreBridges.add(diagonalBridge.flipZ())

            val diagonalSameFaceBridge = BridgeTemplate((0 until 10).map {
                Vec3i((it/2)+1, 0, it)
            }, Vec3i(6, 0, 10), false)
            coreBridges.add(diagonalSameFaceBridge)
            coreBridges.add(diagonalSameFaceBridge.flipZ())

            // Get the positions of each possible bridge block relative to the
            // center of the floor
            val translationVector = Vec3i(2, 0, 0)
            val floorSpaceBridges = coreBridges.map {
                it.translate(translationVector)
            }

            // floorSpaceBridges only contains bridges coming out of the East node.
            // Get the same bridges for the other 3 cardinal directions
//            return floorSpaceBridges..toSet()
            val allTemplates = intArrayOf(0, 90, 180, 270).map { angle ->
                val negativeTranslationVector = (translationVector * -1).rotateBy(angle)
                floorSpaceBridges.map {
                    it
                        .rotateTemplate(angle)
                        .translate(negativeTranslationVector)
                }
            }.flatten().toMutableSet()


            return allTemplates
        }

    }

}


