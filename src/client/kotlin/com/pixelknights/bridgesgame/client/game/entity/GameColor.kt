package com.pixelknights.bridgesgame.client.game.entity

enum class GameColor(
    val colorCode: String,
    val blockColor: String?,
    val isTeam: Boolean
) {


    ORANGE("o", "orange", true),
    YELLOW("y", "yellow", true),
    GREEN("g", "lime", true),
    CYAN("b", "cyan", true),
    MAGENTA("m", "magenta", true),
    RED("r", "red", true),
    WHITE("w", null, false),
    GREY("x", null, false);


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