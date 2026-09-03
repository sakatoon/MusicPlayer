package com.sakatoon.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakatoon.musicplayer.data.model.Song
import com.sakatoon.musicplayer.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playlistName: String,
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayerClick: () -> Unit
) {
    val songs by viewModel.getSongsForPlaylist(playlistId).collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        TopAppBar(
            title = { Text(playlistName) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Esta playlist está vacía", color = Color.Gray)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(bottom = if (uiState.currentSong != null) 140.dp else 0.dp)
                ) {
                    items(songs) { song ->

                        SongItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            isPlaying = uiState.currentSong?.id == song.id,
                            trailingContent = {
                                IconButton(onClick = { viewModel.removeSongFromPlaylist(playlistId, song) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar de Playlist", tint = Color.Gray)
                                }
                            }
                        )
                    }
                }
                
                if (uiState.currentSong != null) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        com.sakatoon.musicplayer.ui.components.MiniPlayer(
                            song = uiState.currentSong!!,
                            isPlaying = uiState.isPlaying,
                            progress = uiState.progress,
                            onPreviousClick = { viewModel.skipPrevious() },
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onNextClick = { viewModel.skipNext() },
                            onClick = onPlayerClick
                        )
                    }
                }
            }
        }
    }
}
