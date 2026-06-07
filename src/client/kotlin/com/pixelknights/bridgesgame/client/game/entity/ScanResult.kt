package com.pixelknights.bridgesgame.client.game.entity

import com.pixelknights.bridgesgame.client.render.HoveringText
import com.pixelknights.bridgesgame.client.render.RenderedDot
import com.pixelknights.bridgesgame.client.render.RenderedLine
import com.pixelknights.bridgesgame.client.render.GameWarning

data class ScanResult(
    val towers: MutableList<MutableList<Tower>>,
    val bridges: MutableSet<Bridge>,
    val ladders: MutableSet<Ladder>,
    val circuits: MutableSet<Circuit>,
    val paths: MutableList<Path>,
    val teams: MutableMap<GameColor, Team>,
    val linesToRender: List<RenderedLine>,
    val dotsToRender: List<RenderedDot>,
    val textsToRender: List<HoveringText>,
    val warnings: List<GameWarning>,
)
