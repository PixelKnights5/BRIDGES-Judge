package com.pixelknights.bridgesgame.client.game

import java.util.concurrent.atomic.AtomicBoolean

class ScanState {
    val isScanning: AtomicBoolean = AtomicBoolean(false)
    var spinnerTick: Int = 0
    var hasActiveOverlay: Boolean = false
}
