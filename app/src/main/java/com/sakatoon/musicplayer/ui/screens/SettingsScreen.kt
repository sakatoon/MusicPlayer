package com.sakatoon.musicplayer.ui.screens

import android.net.Uri
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import com.sakatoon.musicplayer.ui.viewmodel.MusicViewModel


@Composable

fun SettingsScreen(
    viewModel: MusicViewModel,
    onFolderSelected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Ventana emergente de escaneo
    if (uiState.isScanning) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { /* No permitir cerrar durante el escaneo */ }
        ) {
            androidx.compose.material3.Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Escaneando Biblioteca...",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Buscando música en tus carpetas y subcarpetas. Esto puede tardar un momento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                android.widget.Toast.makeText(context, "No se pudo conservar el acceso a la carpeta. Selecciónala de nuevo.", android.widget.Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }
            viewModel.addMusicFolder(it)
            onFolderSelected()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Configuración",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Biblioteca de Música",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Carpetas Seleccionadas:",
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
        ) {
            val folderList = uiState.folderUris.toList()
            items(folderList.size) { index ->
                val uriStr = folderList[index]
                val name = uiState.folderNames.getOrNull(index) ?: uriStr
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                    androidx.compose.material3.IconButton(
                        onClick = { viewModel.removeMusicFolder(uriStr) }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar carpeta",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 440.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FolderActionButtons(
                        addModifier = Modifier.fillMaxWidth(),
                        scanModifier = Modifier.fillMaxWidth(),
                        onAdd = { launcher.launch(null) },
                        onScan = { viewModel.scanAllFolders() }
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FolderActionButtons(
                        addModifier = Modifier.weight(1f),
                        scanModifier = Modifier.weight(1f),
                        onAdd = { launcher.launch(null) },
                        onScan = { viewModel.scanAllFolders() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DeveloperInformation()
    }
}

@Composable
private fun DeveloperInformation() {
    val context = LocalContext.current
    val developerEmail = "sakatoon@gmail.com"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(com.sakatoon.musicplayer.R.drawable.developer_logo),
                    contentDescription = "Logo de SakaToOn",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(180.dp).padding(12.dp)
                )
                Spacer(Modifier.height(14.dp))
                Text("Desarrollador de Software", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("♫ MusicPlayer Android App", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("CONTACTO Y SOPORTE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$developerEmail")
                            putExtra(Intent.EXTRA_SUBJECT, "Consulta - MusicPlayer App")
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Enviar correo a SakaToOn"))
                        } catch (_: android.content.ActivityNotFoundException) {
                            android.widget.Toast.makeText(context, "Instala una aplicación de correo para contactar a SakaToOn.", android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Enviar correo a SakaToOn",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Acerca de MusicPlayer", fontWeight = FontWeight.Bold)
                }
                Text(
                    "MusicPlayer es un reproductor local diseñado para organizar y disfrutar tu música, favoritos y listas de reproducción, con soporte para Android Auto.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                InformationRow("Versión de la app", "1.0")
                InformationRow("Tecnologías", "Kotlin + Jetpack Compose")
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FolderActionButtons(
    addModifier: Modifier,
    scanModifier: Modifier,
    onAdd: () -> Unit,
    onScan: () -> Unit
) {
    Button(onClick = onAdd, modifier = addModifier.heightIn(min = 52.dp), shape = RoundedCornerShape(12.dp)) {
        Text("Agregar carpeta", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, maxLines = 2)
    }
    androidx.compose.material3.OutlinedButton(onClick = onScan, modifier = scanModifier.heightIn(min = 52.dp), shape = RoundedCornerShape(12.dp)) {
        Text("Escanear toda la biblioteca", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun InformationRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
