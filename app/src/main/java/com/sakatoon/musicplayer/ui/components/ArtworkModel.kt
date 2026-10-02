package com.sakatoon.musicplayer.ui.components

/** Chooses the local drawable before Coil receives a missing or blank artwork URI. */
fun resolveArtworkModel(artworkUri: String?, fallbackResource: Int): Any =
    artworkUri?.takeIf { it.isNotBlank() } ?: fallbackResource
