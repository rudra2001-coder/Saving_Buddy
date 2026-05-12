package com.rudra.savingbuddy.ui.screens.music

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(MusicPlayerState())
    val state: StateFlow<MusicPlayerState> = _state.asStateFlow()

    fun playTrack(track: MusicTrack) {
        _state.update {
            val updatedRecent = listOf(track) + it.recentlyPlayed.filter { t -> t.id != track.id }.take(9)
            it.copy(
                currentTrack = track,
                isPlaying = true,
                currentPosition = 0,
                duration = track.duration,
                miniPlayerVisible = true,
                recentlyPlayed = updatedRecent
            )
        }
    }

    fun togglePlayPause() {
        _state.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun seekTo(position: Int) {
        _state.update { it.copy(currentPosition = position.coerceIn(0, it.duration)) }
    }

    fun nextTrack() {
        val current = _state.value
        val playlist = if (current.isShuffled) current.playlist.shuffled() else current.playlist
        val currentIndex = playlist.indexOfFirst { it.id == current.currentTrack?.id }
        if (currentIndex >= 0 && currentIndex < playlist.size - 1) {
            playTrack(playlist[currentIndex + 1])
        } else if (current.repeatMode == RepeatMode.ALL) {
            playTrack(playlist.first())
        }
    }

    fun previousTrack() {
        val current = _state.value
        val currentIndex = current.playlist.indexOfFirst { it.id == current.currentTrack?.id }
        if (currentIndex > 0) {
            playTrack(current.playlist[currentIndex - 1])
        }
    }

    fun setVolume(volume: Float) {
        _state.update { it.copy(volume = volume.coerceIn(0f, 1f)) }
    }

    fun toggleShuffle() {
        _state.update { it.copy(isShuffled = !it.isShuffled) }
    }

    fun cycleRepeatMode() {
        _state.update {
            it.copy(repeatMode = when (it.repeatMode) {
                RepeatMode.NONE -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.NONE
            })
        }
    }

    fun toggleFavorite(trackId: Int) {
        _state.update {
            val updated = it.favorites.toMutableSet()
            if (updated.contains(trackId)) updated.remove(trackId) else updated.add(trackId)
            it.copy(favorites = updated)
        }
    }

    fun setMood(mood: MusicGenre) {
        _state.update { it.copy(currentMood = mood) }
    }

    fun getFilteredPlaylist(): List<MusicTrack> {
        val current = _state.value
        return if (current.currentMood == MusicGenre.FOCUS && current.playlist.all { it.genre == MusicGenre.FOCUS }) {
            current.playlist
        } else {
            current.playlist.filter { it.genre == current.currentMood }.ifEmpty { current.playlist }
        }
    }

    fun hideMiniPlayer() {
        _state.update {
            it.copy(miniPlayerVisible = false, isPlaying = false)
        }
    }

    fun updatePosition(position: Int) {
        _state.update { it.copy(currentPosition = position) }
    }
}
