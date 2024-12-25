package com.pixelknights.bridgesgame.client.game.rules


import com.pixelknights.bridgesgame.client.util.plus
import net.minecraft.util.math.Vec3i
import kotlin.math.cos
import kotlin.math.sin

class BridgeTemplate(
    val blockCoords: List<Vec3i>
) {

    /**
     * Copy the existing template, but rotate it by a given number of degrees
     */
    fun rotateTemplate(degrees: Int): BridgeTemplate {
        val radians = Math.toRadians(degrees.toDouble())
        val cos = cos(radians)
        val sin = sin(radians)


        // 2D rotation matrix:
        // [x', y'] = | cos(theta)  -sin(theta) | * | x |
        //            | sin(theta)   cos(theta) |   | z |
        val newCoords = blockCoords.map { coordinate ->
            val newX = ((coordinate.x * cos) - (coordinate.z * sin)).toInt()
            val newZ = ((coordinate.x * sin) + (coordinate.z * cos)).toInt()
            Vec3i(newX, coordinate.y, newZ)
        }.toList()

        return BridgeTemplate(newCoords)
    }

    private fun flipX(): BridgeTemplate =
        BridgeTemplate(this.blockCoords.map {
            Vec3i(-it.x, it.y, it.z)
        })

    private fun flipZ(): BridgeTemplate =
        BridgeTemplate(this.blockCoords.map {
            Vec3i(it.x, it.y, -it.z)
        })

    /**
     * Shift the bridge over by [distance] blocks
     */
    fun translate(distance: Vec3i): BridgeTemplate {
        return BridgeTemplate(blockCoords.map { it + distance })
    }

    /**
     * Shift the bridge over by the provided number of blocks in each coordinatef
     */
    fun translate(x: Int, y: Int, z: Int) = translate(Vec3i(x, y, z))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BridgeTemplate) return false

        return !(blockCoords.size == other.blockCoords.size &&
                blockCoords.containsAll(other.blockCoords))
    }

    override fun hashCode(): Int {
        return blockCoords.hashCode()
    }


    companion object {

        /**
         * Get a list of all possible bridges that could come from a floor
         * from a corner node.
         * Coordinates are relative to the center of the floor
         */
        fun getCornerTemplates(): Set<BridgeTemplate> {
            // These coordinates are relative to the NODE, not the
            // center of the tower. It will be fixed later, but makes the
            // maths a bit simpler
            val coreBridges = mutableSetOf<BridgeTemplate>()

            val straightBridge = BridgeTemplate((1 .. 5).map {
                Vec3i(it, 0, 0)
            })
            coreBridges.add(straightBridge)
            coreBridges.add(straightBridge.rotateTemplate(90))

            val diagonalBridge = BridgeTemplate((0 until 5).map {
                Vec3i(it+1, 0, it+1)
            })
            coreBridges.add(diagonalBridge)

            val reverseDiagonalBridge = BridgeTemplate((0 until 5).map {
                Vec3i(it+1, 0, -it)
            })
            coreBridges.add(reverseDiagonalBridge)
            coreBridges.add(reverseDiagonalBridge.flipZ().flipX().translate(1, 0, 1))


            // There are only 2 2-step bridges that we need to consdier.
            // The other 2 possibilities will be picked up by another
            // tower's corner.
            var twoStepDiagonal = BridgeTemplate((0 until 10).map {
                Vec3i((it/2)+1, 0, it+1)
            })
            coreBridges.add(twoStepDiagonal)
            twoStepDiagonal = BridgeTemplate((0 until 10).map {
                Vec3i(it+1, 0, (it/2)+1)
            })
            coreBridges.add(twoStepDiagonal)

            // There are 4 3-step bridge options.
            var threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i((it/3)+1, 0, it+1)
            })
            coreBridges.add(threeStepDiagonal)

            threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i(it+1, 0, (it/3)+1)
            })
            coreBridges.add(threeStepDiagonal)

            threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i((-it/3)+1, 0, it)
            })
            coreBridges.add(threeStepDiagonal)

            threeStepDiagonal = BridgeTemplate((0 until 15).map {
                Vec3i(it, 0, (-it/3)+1)
            })
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
        fun getCardinalTemplates(): Set<BridgeTemplate> {
            // These coordinates are relative to the NODE, not the
            // center of the tower. It will be fixed later, but makes the
            // maths a bit simpler
            val coreBridges = mutableSetOf<BridgeTemplate>()
            val straightBridge = BridgeTemplate((1 .. 5).map {
                Vec3i(it, 0, 0)
            })
            coreBridges.add(straightBridge)

            val diagonalBridge = BridgeTemplate((0 until 8).map {
                Vec3i(it+1, 0, it)
            })
            // Get the diagonal in forward and reverse directions
            coreBridges.add(diagonalBridge)
            coreBridges.add(diagonalBridge.flipZ())

            val diagonalSameFaceBridge = BridgeTemplate((0 until 10).map {
                Vec3i((it/2)+1, 0, it)
            })
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

        @JvmStatic
        fun generateTemplate(length: Int, step: Int): BridgeTemplate {
            val coords = (1 .. length).map { x ->
                return@map if (step == 0) {
                    Vec3i(x, 0, 0)
                } else {
                    Vec3i(x, 0, x / step)
                }
            }.toList()

            return BridgeTemplate(coords)
        }
    }

}

