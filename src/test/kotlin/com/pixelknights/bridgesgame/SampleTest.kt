package com.pixelknights.bridgesgame

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(FabricExtension::class)
class SampleTest {

    @Test
    fun testDiamondItemStack() {
        val diamondStack = ItemStack(Items.DIAMOND, 64)

        Assertions.assertTrue(diamondStack.item == Items.DIAMOND)
        Assertions.assertEquals(64, diamondStack.count)
    }

}