package com.pixelknights.bridgesgame.client.game.entity

enum class GameColor(
    val colorCode: String,
    val blockColor: String?,
    val isTeam: Boolean,
    val rgba: Long,
) {


    ORANGE("o", "orange", true, 0xFFAA00FF),
    YELLOW("y", "yellow", true, 0xFFFF00FF),
    GREEN("g", "lime", true, 0x00AA00FF),
    CYAN("b", "cyan", true, 0x00FFFFFF),
    MAGENTA("m", "magenta", true, 0xFF00FFFF),
    RED("r", "red", true, 0xFF0000FF),
    WHITE("w", null, false, 0xFFFFFFFF),
    GREY("x", null, false, 0x808080FF);


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