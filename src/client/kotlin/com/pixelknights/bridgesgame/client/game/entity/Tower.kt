package com.pixelknights.bridgesgame.client.game.entity

class Tower(
    val row: Int,
    val column: Int,
    val numFloors: Int,
    val color: GameColor,
) {
    var capturingTeam: Team? = null
}