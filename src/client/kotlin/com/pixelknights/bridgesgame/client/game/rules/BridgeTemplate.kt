package com.pixelknights.bridgesgame.client.game.rules


import com.pixelknights.bridgesgame.client.config.ModConfig
import com.pixelknights.bridgesgame.client.game.entity.GameColor
import com.pixelknights.bridgesgame.client.util.getBlockState
import com.pixelknights.bridgesgame.client.util.getTeamColorForBlock
import com.pixelknights.bridgesgame.client.util.plus
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import kotlin.math.cos
import kotlin.math.sin

class BridgeTemplate(
    val blockCoords: List<Vec3i>,
    /**
     * This value shows the offset from the current node to the node that the bridge goes to
     */
    val targetNodeOffset: Vec3i
) {

    /**
     * Copy the existing template, but rotate it by a given number of degrees about the y axis
     */
    fun rotateTemplate(degrees: Int): BridgeTemplate {
        val radians = Math.toRadians(degrees.toDouble())
        val cos = cos(radians)
        val sin = sin(radians)


        // 2D rotation matrix:
        // [x', z'] = | cos(theta)  -sin(theta) | * | x |
        //            | sin(theta)   cos(theta) |   | z |
        val newCoords = blockCoords.map { coordinate ->
            val newX = ((coordinate.x * cos) - (coordinate.z * sin)).toInt()
            val newZ = ((coordinate.x * sin) + (coordinate.z * cos)).toInt()
            Vec3i(newX, coordinate.y, newZ)
        }.toList()

        val newNodeX =  ((targetNodeOffset.x * cos) - (targetNodeOffset.z * sin)).toInt()
        val newNodeZ =  ((targetNodeOffset.x * sin) + (targetNodeOffset.z * cos)).toInt()

        return BridgeTemplate(newCoords, Vec3i(newNodeX, targetNodeOffset.y, newNodeZ))
    }

    private fun flipX(): BridgeTemplate =
        BridgeTemplate(this.blockCoords.map {
            Vec3i(-it.x, it.y, it.z)
        }, Vec3i(-targetNodeOffset.x, targetNodeOffset.y, targetNodeOffset.z))

    private fun flipZ(): BridgeTemplate =
        BridgeTemplate(this.blockCoords.map {
            Vec3i(it.x, it.y, -it.z)
        }, Vec3i(targetNodeOffset.x, targetNodeOffset.y, -targetNodeOffset.z))

    /**
     * Shift the bridge over by [distance] blocks
     */
    fun translate(distance: Vec3i): BridgeTemplate {
        return BridgeTemplate(blockCoords.map { it + distance }, targetNodeOffset + distance)
    }

    /**
     * Shift the bridge over by the provided number of blocks in each coordinate
     */
    fun translate(x: Int, y: Int, z: Int) = translate(Vec3i(x, y, z))

    /**
     * Check who owns this bridge (if anyone). Returns `null` if this bridge does not exist.
     * This can also be used to detect paints by shifting [nodeCoords] +1 in the Y axis
     */
    fun findBridgeOwner(mc: MinecraftClient, config: ModConfig, nodeCoords: BlockPos): GameColor? {
        val nodeTemplate = this.translate(nodeCoords)
        val blockColors = nodeTemplate.blockCoords.map {
            mc.world?.getBlockState(it)
        }.map {
            getTeamColorForBlock(it?.block)
        }.groupBy { it }

        // If there are too many missing blocks, it's not a bridge.
        val numMissingBlocks = blockColors[null]?.size ?: 0
        if (numMissingBlocks > config.boardConfig.maxMisplacedBlockTolerance) {
            return null
        }

        // Technically there could be multiple colors if someone made a mistake, but it should not
        // happen in normal gameplay. If it does, the team that has the most blocks will be considered the
        // owner. TODO: Consider sending a warning event if this happens.
        return blockColors.maxBy { it.value.size }.key
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

            val straightBridge = BridgeTemplate((1 .. 5).map {
                Vec3i(it, 0, 0)
            }, Vec3i(6, 0, 0))
            coreBridges.add(straightBridge)
            coreBridges.add(straightBridge.rotateTemplate(90))

            val diagonalBridge = BridgeTemplate((0 until 5).map {
                Vec3i(it+1, 0, it+1)
            }, Vec3i(6, 0, 6))
            coreBridges.add(diagonalBridge)

            val reverseDiagonalBridge = BridgeTemplate((0 until 5).map {
                Vec3i(it+1, 0, -it)
            }, Vec3i(6, 0, -4))
            coreBridges.add(reverseDiagonalBridge)
            coreBridges.add(reverseDiagonalBridge.flipZ().flipX().translate(1, 0, 1))


            // There are only 2 2-step bridges that we need to consdier.
            // The other 2 possibilities will be picked up by another
            // tower's corner.
            var twoStepDiagonal = BridgeTemplate((0 until 10).map {
                Vec3i((it/2)+1, 0, it+1)
            }, Vec3i(6, 0, 10))
            coreBridges.add(twoStepDiagonal)
            twoStepDiagonal = BridgeTemplate((0 until 10).map {
                Vec3i(it+1, 0, (it/2)+1)
            }, Vec3i(10, 0, 6))
            coreBridges.add(twoStepDiagonal)

            // There are 4 3-step bridge options.
            var threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i((it/3)+1, 0, it+1)
            }, Vec3i(6, 0, 16))
            coreBridges.add(threeStepDiagonal)

            threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i(it+1, 0, (it/3)+1)
            }, Vec3i(16, 0, 6))
            coreBridges.add(threeStepDiagonal)

            threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i((-it/3)+1, 0, it)
            }, Vec3i(-6, 0, 16) )
            coreBridges.add(threeStepDiagonal)

            threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i(it, 0, (-it/3)+1)
            }, Vec3i(16, 0, -6))
            coreBridges.add(threeStepDiagonal)

            val floorSpaceBridges = coreBridges.map { bridgeTemplate ->
                bridgeTemplate.translate(2, 0, 2)
            }

            // floorSpaceBridges only contains bridges coming out of the East node.
            // Get the same bridges for the other 3 cardinal directions
            return intArrayOf(0, 90, 180, 270).map { angle ->
                floorSpaceBridges.map {
                    it.rotateTemplate(angle)
                }
            }.flatten().toSet()
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
            }, Vec3i(6, 0, 0))
            coreBridges.add(straightBridge)

            val diagonalBridge = BridgeTemplate((0 until 8).map {
                Vec3i(it+1, 0, it)
            }, Vec3i(6, 0, 10))
            // Get the diagonal in forward and reverse directions
            coreBridges.add(diagonalBridge)
            coreBridges.add(diagonalBridge.flipZ())

            val diagonalSameFaceBridge = BridgeTemplate((0 until 10).map {
                Vec3i((it/2)+1, 0, it)
            }, Vec3i(6, 0, 10))
            coreBridges.add(diagonalSameFaceBridge)
            coreBridges.add(diagonalBridge.flipZ())

            // Get the positions of each possible bridge block relative to the
            // center of the floor
            val floorSpaceBridges = coreBridges.map {
                it.translate(Vec3i(2, 0, 0))
            }

            // floorSpaceBridges only contains bridges coming out of the East node.
            // Get the same bridges for the other 3 cardinal directions
            return intArrayOf(0, 90, 180, 270).map { angle ->
                floorSpaceBridges.map {
                    it.rotateTemplate(angle)
                }
            }.flatten().toSet()
        }

    }

}

