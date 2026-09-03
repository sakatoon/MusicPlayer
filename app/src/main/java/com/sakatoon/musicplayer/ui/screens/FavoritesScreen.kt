
package com.sakatoon.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
fun FavoritesScreen(viewModel: MusicViewModel) {
    val favoriteSongs by viewModel.favoriteSongs.collectAsState(initial = emptyList<Song>())
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Favoritos",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (favoriteSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Todavía no tienes favoritos", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
             LazyColumn {
                items(favoriteSongs) { song ->

                    SongItem(
                        song = song,
                        onClick = { viewModel.playSong(song) }, // Or navigate to player
                        isPlaying = uiState.currentSong?.id == song.id,


                        trailingContent = {
                             IconButton(onClick = { viewModel.toggleFavorite(song) }) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Quitar de Favoritos",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
