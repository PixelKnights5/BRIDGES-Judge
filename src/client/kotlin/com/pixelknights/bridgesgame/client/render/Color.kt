package com.pixelknights.bridgesgame.client.render

data class Color(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1.0f) {

    companion object {
        val RED = Color(1.0f, 0.0f, 0.0f, 1.0f)
        val GREEN = Color(0.0f, 1.0f, 0.0f, 1.0f)
        val BLUE = Color(0.0f, 0.0f, 1.0f, 1.0f)
        val WHITE = Color(1.0f, 1.0f, 1.0f, 1.0f)
        val BLACK = Color(0.0f, 0.0f, 0.0f, 1.0f)
    }
}
