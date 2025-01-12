package com.pixelknights.bridgesgame.client.render

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1.0f) {

    val argb: Int
        get() {
            val red = (this.red * 255f).toInt()
            val green = (this.green * 255f).toInt()
            val blue = (this.blue * 255f).toInt()
            val alpha = (this.alpha * 255f).toInt()
            return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
        }

    companion object {
        val RED = Color(1.0f, 0.0f, 0.0f, 1.0f)
        val GREEN = Color(0.0f, 1.0f, 0.0f, 1.0f)
        val BLUE = Color(0.0f, 0.0f, 1.0f, 1.0f)
        val WHITE = Color(1.0f, 1.0f, 1.0f, 1.0f)
        val BLACK = Color(0.0f, 0.0f, 0.0f, 1.0f)


        @JvmStatic
        fun fromHex(hex: Long): Color {
            val red = (hex shr 24 and 0xFF) / 255.0f
            val green = (hex shr 16 and 0xFF) / 255.0f
            val blue = (hex shr 8 and 0xFF) / 255.0f
            val alpha = (hex and 0xFF) / 255.0f
            return Color(red, green, blue, alpha)
        }

    }
}
