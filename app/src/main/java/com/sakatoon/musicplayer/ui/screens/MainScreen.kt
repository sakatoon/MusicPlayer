
package com.sakatoon.musicplayer.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sakatoon.musicplayer.ui.components.BottomNavigationBar
import com.sakatoon.musicplayer.ui.components.MusicNavigationRail
import com.sakatoon.musicplayer.ui.navigation.MusicNavHost
import com.sakatoon.musicplayer.ui.viewmodel.MusicViewModel

@Composable
fun MainScreen(
    windowSize: WindowWidthSizeClass,
    viewModel: MusicViewModel,
    startDestination: String = com.sakatoon.musicplayer.ui.navigation.Screen.Home.route
) {
    val navController = rememberNavController()

    if (windowSize == WindowWidthSizeClass.Compact) {
        // Phone Layout
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            MusicNavHost(
                navController = navController,
                viewModel = viewModel,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        // Tablet / Expanded Layout
        Row(modifier = Modifier.fillMaxSize()) {
            MusicNavigationRail(navController)
            
            MusicNavHost(
                navController = navController,
                viewModel = viewModel,
                startDestination = startDestination,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
