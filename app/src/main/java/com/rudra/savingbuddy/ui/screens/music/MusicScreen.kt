package com.rudra.savingbuddy.ui.screens.music

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    viewModel: MusicViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Music Player", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (state.isShuffled) PrimaryGreen else TextSecondary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { MoodSelector(currentMood = state.currentMood, onMoodSelect = { viewModel.setMood(it) }) }

            item {
                NowPlayingCard(
                    currentTrack = state.currentTrack,
                    isPlaying = state.isPlaying,
                    currentPosition = state.currentPosition,
                    volume = state.volume,
                    repeatMode = state.repeatMode,
                    isFavorite = state.currentTrack?.let { state.favorites.contains(it.id) } ?: false,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onNext = { viewModel.nextTrack() },
                    onPrevious = { viewModel.previousTrack() },
                    onSeek = { viewModel.seekTo(it) },
                    onVolumeChange = { viewModel.setVolume(it) },
                    onRepeatClick = { viewModel.cycleRepeatMode() },
                    onFavoriteClick = { state.currentTrack?.let { viewModel.toggleFavorite(it.id) } },
                    onShuffleClick = { viewModel.toggleShuffle() },
                    isShuffled = state.isShuffled
                )
            }

            if (state.recentlyPlayed.isNotEmpty()) {
                item {
                    RecentlyPlayedSection(
                        tracks = state.recentlyPlayed.take(5),
                        currentTrackId = state.currentTrack?.id,
                        onTrackClick = { viewModel.playTrack(it) }
                    )
                }
            }

            item {
                PlaylistSection(
                    title = when (state.currentMood) {
                        MusicGenre.FOCUS -> "Focus Beats"
                        MusicGenre.RELAX -> "Relaxation"
                        MusicGenre.ENERGY -> "Energy Boost"
                        MusicGenre.LO_FI -> "Lo-Fi Beats"
                        MusicGenre.NATURE -> "Nature Sounds"
                        MusicGenre.CLASSICAL -> "Classical"
                        MusicGenre.JAZZ -> "Jazz Vibes"
                        MusicGenre.AMBIENT -> "Ambient"
                    },
                    tracks = viewModel.getFilteredPlaylist(),
                    currentTrackId = state.currentTrack?.id,
                    favorites = state.favorites,
                    onTrackClick = { viewModel.playTrack(it) },
                    onFavoriteClick = { viewModel.toggleFavorite(it) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MoodSelector(currentMood: MusicGenre, onMoodSelect: (MusicGenre) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Psychology, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Your Mood", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MusicGenre.entries) { mood ->
                    val isSelected = mood == currentMood
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) PrimaryGreen.copy(alpha = 0.2f) else Color.Transparent,
                        label = "moodBg"
                    )
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) PrimaryGreen else BorderLight.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onMoodSelect(mood) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(mood.icon, fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            mood.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) PrimaryGreen else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NowPlayingCard(
    currentTrack: MusicTrack?,
    isPlaying: Boolean,
    currentPosition: Int,
    volume: Float,
    repeatMode: RepeatMode,
    isFavorite: Boolean,
    isShuffled: Boolean,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onRepeatClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing)
                )
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            currentTrack?.coverColor?.copy(alpha = 0.3f) ?: PrimaryGreen.copy(alpha = 0.3f),
                            BackgroundCard
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    currentTrack?.coverColor ?: PrimaryGreen,
                                    (currentTrack?.coverColor ?: PrimaryGreen).copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        Icon(
                            Icons.Default.MusicNote,
                            null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(60.dp)
                                .rotate(rotation.value)
                        )
                    } else {
                        Icon(
                            Icons.Default.PlayArrow,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentTrack?.title ?: "No Track Selected",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = currentTrack?.artist ?: "Select a track to play",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatDuration(currentPosition), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(formatDuration(currentTrack?.duration ?: 0), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                Slider(
                    value = if (currentTrack != null) currentPosition.toFloat() / currentTrack.duration.toFloat().coerceAtLeast(1f) else 0f,
                    onValueChange = { onSeek((it * (currentTrack?.duration ?: 1)).toInt()) },
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryGreen,
                        activeTrackColor = PrimaryGreen,
                        inactiveTrackColor = BorderLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onShuffleClick) {
                        Icon(Icons.Default.Shuffle, "Shuffle", tint = if (isShuffled) PrimaryGreen else TextSecondary)
                    }
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Default.SkipPrevious, "Previous", tint = TextPrimary, modifier = Modifier.size(32.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                            .clickable { onTogglePlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Default.SkipNext, "Next", tint = TextPrimary, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = onRepeatClick) {
                        val tint = when (repeatMode) {
                            RepeatMode.ALL -> PrimaryGreen
                            RepeatMode.ONE -> PrimaryGreen
                            RepeatMode.NONE -> TextSecondary
                        }
                        Icon(
                            if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            "Repeat",
                            tint = tint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.VolumeDown, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Slider(
                            value = volume,
                            onValueChange = onVolumeChange,
                            modifier = Modifier.width(120.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = TextSecondary,
                                activeTrackColor = TextSecondary,
                                inactiveTrackColor = BorderLight
                            )
                        )
                        Icon(Icons.Outlined.VolumeUp, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            "Favorite",
                            tint = if (isFavorite) ExpenseRed else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentlyPlayedSection(
    tracks: List<MusicTrack>,
    currentTrackId: Int?,
    onTrackClick: (MusicTrack) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.History, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recently Played", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tracks) { track ->
                    val isActive = track.id == currentTrackId
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(track.coverColor.copy(alpha = 0.2f))
                            .border(
                                1.dp,
                                if (isActive) PrimaryGreen else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onTrackClick(track) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(track.coverColor.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isActive) Icons.Default.MusicNote else Icons.Outlined.MusicNote,
                                null,
                                tint = if (isActive) PrimaryGreen else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(track.title.take(12), style = MaterialTheme.typography.labelSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistSection(
    title: String,
    tracks: List<MusicTrack>,
    currentTrackId: Int?,
    favorites: Set<Int>,
    onTrackClick: (MusicTrack) -> Unit,
    onFavoriteClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.QueueMusic, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Text("${tracks.size} tracks", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            tracks.forEach { track ->
                val isActive = track.id == currentTrackId
                val bgColor by animateColorAsState(
                    targetValue = if (isActive) PrimaryGreen.copy(alpha = 0.1f) else Color.Transparent,
                    label = "trackBg"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onTrackClick(track) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(track.coverColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isActive) Icons.Default.MusicNote else Icons.Outlined.MusicNote,
                            null,
                            tint = if (isActive) PrimaryGreen else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            track.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            "${track.artist} • ${formatDuration(track.duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { onFavoriteClick(track.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (favorites.contains(track.id)) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            "Favorite",
                            tint = if (favorites.contains(track.id)) ExpenseRed else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
