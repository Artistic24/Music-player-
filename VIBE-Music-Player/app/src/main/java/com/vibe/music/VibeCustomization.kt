package com.vibe.music

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

private val vibeThemes = listOf(
    "Neon Pulse" to listOf(Color(0xFF9B6BFF), Color(0xFF35D6E8), Color(0xFFFF5EA8)),
    "Solar Flare" to listOf(Color(0xFFFFA34D), Color(0xFFFF5EA8), Color(0xFF8B5CF6)),
    "Ocean Drive" to listOf(Color(0xFF25C6DA), Color(0xFF477BFF), Color(0xFF9B6BFF)),
    "Midnight" to listOf(Color(0xFF8B5CF6), Color(0xFF303B68), Color(0xFF35D6E8)),
    "Rose Gold" to listOf(Color(0xFFFF719A), Color(0xFFFFB36B), Color(0xFF8D4B91)),
    "Emerald" to listOf(Color(0xFF28D7A0), Color(0xFF168B8B), Color(0xFF7D8BFF))
)
private val vibeStyles = listOf("Classic", "Static", "Dynamic", "Spin Vinyl", "Round")

@Composable
internal fun VibeProfilePage(
    theme: String, style: String, onTheme: (String) -> Unit, onStyle: (String) -> Unit,
    onFix: () -> Unit, onSettings: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Color(0xFF542A91), Color(0xFF163E67), Color(0xFF9D286B)))).padding(19.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(62.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF130D25)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Album, null, tint = Color.White, modifier = Modifier.size(39.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Your VIBE", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                    Text("Your sound. Your style.", color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
                }
            }
        }
        item { Text("PLAYER STYLE", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                vibeStyles.forEach { item ->
                    FilterChip(selected = style == item, onClick = { onStyle(item) }, label = { Text(item) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF9B6BFF), selectedLabelColor = Color.White))
                }
            }
        }
        item { Text("THEME GALLERY", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp) }
        items(vibeThemes) { item ->
            val (name, colors) = item
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF211B36)).clickable { onTheme(name) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(88.dp).height(58.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(colors))) {
                    Icon(Icons.Default.GraphicEq, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(31.dp))
                }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(if (theme == name) "Currently selected" else "Tap to apply", color = Color(0xFFB8B0CE), fontSize = 11.sp)
                }
                if (theme == name) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF35D6E8))
            }
        }
        item {
            Text("YOUR MUSIC", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
            ProfileAction(Icons.Default.AutoFixHigh, "Fix song information", "Correct titles and artists; undo any change", onFix)
            ProfileAction(Icons.Default.Settings, "Settings", "Personalize your VIBE experience", onSettings)
        }
    }
}

@Composable
private fun ProfileAction(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, click: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF211B36)).clickable(onClick = click).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF35D6E8), modifier = Modifier.size(27.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Color(0xFFB8B0CE), fontSize = 10.sp) }
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
internal fun VibeFixSongInfoPage(
    tracks: List<Track>, overrides: Map<Long, Pair<String, String>>,
    onSave: (Track, String, String) -> Unit, onUndo: (Track) -> Unit
) {
    var editing by remember { mutableStateOf<Track?>(null) }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        Text("Fix song info", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
        Text("Changes are saved inside VIBE. Your original audio files are not modified.", color = Color(0xFFB8B0CE), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 18.dp))
        LazyColumn(contentPadding = PaddingValues(12.dp)) {
            items(tracks) { track ->
                val changed = overrides[track.id]
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xFF211B36)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, null, tint = Color(0xFFFF5EA8))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(changed?.first ?: track.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(changed?.second ?: track.artist, color = Color(0xFFB8B0CE), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    TextButton(onClick = { title = changed?.first ?: track.title; artist = changed?.second ?: track.artist; editing = track }) { Text("Edit") }
                    if (changed != null) IconButton(onClick = { onUndo(track) }) { Icon(Icons.Default.Undo, "Undo fix", tint = Color(0xFFFF5EA8)) }
                }
            }
        }
    }
    editing?.let { track ->
        AlertDialog(onDismissRequest = { editing = null }, title = { Text("Edit song information") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Song title") }, singleLine = true)
                    OutlinedTextField(value = artist, onValueChange = { artist = it }, label = { Text("Artist") }, singleLine = true)
                }
            },
            confirmButton = { TextButton(onClick = { if (title.isNotBlank() && artist.isNotBlank()) onSave(track, title.trim(), artist.trim()); editing = null }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
            containerColor = Color(0xFF211B36), titleContentColor = Color.White, textContentColor = Color.White)
    }
}
