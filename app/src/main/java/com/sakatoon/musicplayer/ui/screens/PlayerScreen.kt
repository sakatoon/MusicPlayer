package com.sakatoon.musicplayer.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakatoon.musicplayer.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    viewModel: MusicViewModel
) {


    val uiState by viewModel.uiState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState(initial = emptyList())
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current


    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    val artworkSize = minOf(maxWidth - 48.dp, 360.dp)
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ... (Album Art and Text - keep existing)

        // Album Art
        coil.compose.AsyncImage(
            model = com.sakatoon.musicplayer.ui.components.resolveArtworkModel(
                uiState.currentSong?.albumArtUri,
                com.sakatoon.musicplayer.R.drawable.sakatoon_mp
            ),
            contentDescription = "Portada de la canción",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .size(artworkSize)
                .background(Color.DarkGray),
            placeholder = androidx.compose.ui.res.painterResource(com.sakatoon.musicplayer.R.drawable.sakatoon_mp),
            error = androidx.compose.ui.res.painterResource(com.sakatoon.musicplayer.R.drawable.sakatoon_mp),
            fallback = androidx.compose.ui.res.painterResource(com.sakatoon.musicplayer.R.drawable.sakatoon_mp)
        )

        Spacer(modifier = Modifier.height(32.dp))


        // Song Info
        Text(
            text = uiState.currentSong?.title ?: "No se está reproduciendo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2
        )
        Text(
            text = uiState.currentSong?.artist ?: "Artista Desconocido",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Progress Bar
        Slider(
            value = uiState.progress,
            onValueChange = { viewModel.seekTo(it) },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.DarkGray
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(uiState.currentPosition), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatTime(uiState.duration), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(32.dp))


        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.skipPrevious() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = MaterialTheme.colorScheme.onBackground)
            }

            FloatingActionButton(
                onClick = { viewModel.togglePlayPause() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir/Pausar",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(onClick = { viewModel.skipNext() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Extra Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
             FilledTonalIconButton(
                onClick = {
                    val enabling = !uiState.isShuffleEnabled
                    viewModel.toggleShuffle()
                    android.widget.Toast.makeText(
                        context,
                        if (enabling) "Reproducción aleatoria activada" else "Reproducción aleatoria desactivada",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.size(60.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (uiState.isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (uiState.isShuffleEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
             ) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = if (uiState.isShuffleEnabled) {
                        "Desactivar reproducción aleatoria"
                    } else {
                        "Activar reproducción aleatoria"
                    },
                    modifier = Modifier.size(30.dp)
                )
            }

             val isFavorite = uiState.currentSong?.let { favoriteIds.contains(it.id) } ?: false
             
             FilledTonalIconButton(
                onClick = { uiState.currentSong?.let { viewModel.toggleFavorite(it) } },
                modifier = Modifier.size(60.dp)
             ) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            // Add to Playlist Button
            FilledTonalIconButton(onClick = {
                // Show dialog
                showAddToPlaylistDialog = true 
            }, modifier = Modifier.size(60.dp)) {
                Icon(Icons.Default.PlaylistAdd, contentDescription = "Agregar a Playlist", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
            }
        }
    }
    }
    
    if (showAddToPlaylistDialog) {
        com.sakatoon.musicplayer.ui.components.AddToPlaylistDialog(
            playlists = playlists,
            onPlaylistSelected = { playlist ->
                uiState.currentSong?.let { song ->
                    viewModel.addSongToPlaylist(playlist.playlistId, song)
                    android.widget.Toast.makeText(context, "Agregada a ${playlist.name}", android.widget.Toast.LENGTH_SHORT).show()
                }
                showAddToPlaylistDialog = false
            },
            onDismiss = { showAddToPlaylistDialog = false }
        )
    }
}

fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}
