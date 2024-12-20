package com.pixelknights.bridgesgame.client.game.entity

enum class GameColor(val colorCode: String, val isTeam: Boolean) {
    ORANGE("o", true),
    YELLOW("y", true),
    GREEN("g", true),
    CYAN("b", true),
    MAGENTA("m", true),
    RED("r", true),
    WHITE("w", false),
    GREY("x", false);

    companion object {
        @JvmStatic
        fun fromChar(code: String): GameColor? {
            entries.forEach { entry ->
                if (entry.colorCode == code.lowercase()) {
                    return entry
                }
            }

            return null
        }
    }
}