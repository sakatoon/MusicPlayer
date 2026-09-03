package com.sakatoon.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
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


@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onSongClick: (Song) -> Unit,
    onPlayerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {


        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Biblioteca",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        if (uiState.folderUris.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ninguna carpeta seleccionada", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onSettingsClick) {
                        Text("Seleccionar Carpeta")
                    }
                }
            }
        } else if (uiState.songs.isEmpty()) {
             Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron canciones", color = Color.Gray)
            }

        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(bottom = if (uiState.currentSong != null) 140.dp else 0.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(uiState.songs) { song ->
                        SongItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            isPlaying = uiState.currentSong?.id == song.id
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


@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit,
    isPlaying: Boolean,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art
        coil.compose.AsyncImage(
            model = song.albumArtUri,
            contentDescription = "Album Art",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .background(Color.DarkGray),
            error = null
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${song.artist} • ${song.album}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        trailingContent?.invoke()
    }
}
