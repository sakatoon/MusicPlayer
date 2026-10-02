package com.sakatoon.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakatoon.musicplayer.data.model.Song
import com.sakatoon.musicplayer.ui.viewmodel.MusicViewModel
import java.text.Normalizer

fun filterSongs(songs: List<Song>, query: String): List<Song> {
    val normalizedQuery = normalizeSearchText(query)
    if (normalizedQuery.isEmpty()) return songs
    return songs.filter { song ->
        listOf(song.title, song.artist, song.album).any { value ->
            normalizeSearchText(value).contains(normalizedQuery)
        }
    }
}

private fun normalizeSearchText(value: String): String = Normalizer
    .normalize(value.trim().lowercase(), Normalizer.Form.NFD)
    .replace("\\p{M}+".toRegex(), "")


@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onSongClick: (Song) -> Unit,
    onPlayerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    val visibleSongs = filterSongs(uiState.songs, searchQuery)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {


        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(modifier = Modifier.weight(1f))
                if (uiState.songs.isNotEmpty()) {
                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = if (isSearchVisible) "Ocultar búsqueda" else "Buscar canciones"
                        )
                    }
                }
            }
            if (uiState.songs.isNotEmpty() && (isSearchVisible || searchQuery.isNotEmpty())) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar canciones")
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda")
                            }
                        }
                    } else null,
                    placeholder = { Text("Buscar canciones, artistas o álbumes") },
                    label = { Text("Buscar") }
                )
            }
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
                if (visibleSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron canciones con esa búsqueda", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(bottom = if (uiState.currentSong != null) 140.dp else 0.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(visibleSongs, key = { it.id }) { song ->
                            SongItem(
                                song = song,
                                onClick = { onSongClick(song) },
                                isPlaying = uiState.currentSong?.id == song.id
                            )
                        }
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
            model = com.sakatoon.musicplayer.ui.components.resolveArtworkModel(
                song.albumArtUri,
                com.sakatoon.musicplayer.R.drawable.sakatoon_mp
            ),
            contentDescription = "Portada de ${song.title}",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .background(Color.DarkGray),
            error = androidx.compose.ui.res.painterResource(com.sakatoon.musicplayer.R.drawable.sakatoon_mp),
            fallback = androidx.compose.ui.res.painterResource(com.sakatoon.musicplayer.R.drawable.sakatoon_mp)
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
