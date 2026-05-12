package com.rudra.savingbuddy.ui.screens.music

import androidx.compose.ui.graphics.Color

data class MusicTrack(
    val id: Int,
    val title: String,
    val artist: String,
    val duration: Int,
    val coverColor: Color,
    val genre: MusicGenre,
    val isLocal: Boolean = false,
    val uri: String = ""
)

enum class MusicGenre(val displayName: String, val icon: String) {
    FOCUS("Focus", "🎯"),
    RELAX("Relax", "🧘"),
    ENERGY("Energy", "⚡"),
    LO_FI("Lo-Fi", "🎧"),
    NATURE("Nature", "🌿"),
    CLASSICAL("Classical", "🎵"),
    JAZZ("Jazz", "🎷"),
    AMBIENT("Ambient", "🌊")
}

enum class RepeatMode {
    NONE, ALL, ONE
}

data class MusicPlayerState(
    val currentTrack: MusicTrack? = null,
    val playlist: List<MusicTrack> = emptyList(),
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val volume: Float = 0.7f,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val isShuffled: Boolean = false,
    val miniPlayerVisible: Boolean = false,
    val currentMood: MusicGenre = MusicGenre.FOCUS,
    val favorites: Set<Int> = emptySet(),
    val recentlyPlayed: List<MusicTrack> = emptyList()
)
