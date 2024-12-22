package com.pixelknights.bridgesgame.client.util

fun CharSequence.containsAny(vararg others: CharSequence): Boolean = others.any(this::contains)
