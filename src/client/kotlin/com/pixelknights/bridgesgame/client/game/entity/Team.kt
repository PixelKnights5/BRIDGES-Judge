package com.pixelknights.bridgesgame.client.game.entity

data class Team (
    var capturedTowers: Int = 0,
    var points: Int = 0,
){
    lateinit var baseColor: GameColor
}