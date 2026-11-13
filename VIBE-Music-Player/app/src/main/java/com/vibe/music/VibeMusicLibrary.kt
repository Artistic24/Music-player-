package com.vibe.music

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

private val libraryTabs = listOf("For you", "Songs", "Playlist", "Video")
@Composable
internal fun VibeMusicLibrary(
    songs: List<Track>, videos: List<Track>, favourites: Set<Long>,
    playlists: Map<String, Set<Long>>, playingTrack: Track?, lyricsAvailable:Set<Long>,
    onPlay: (Track) -> Unit, onFavourite: (Track) -> Unit,
    onCreatePlaylist: () -> Unit, onOpenPlayer: () -> Unit, onProfile: () -> Unit
) {
    var tab by remember { mutableIntStateOf(1) }
    var sortNewest by remember { mutableStateOf(false) }; var searchOpen by remember { mutableStateOf(false) }; var query by remember { mutableStateOf("") }
    val baseList = when(tab) {
        0 -> songs.sortedByDescending { it.dateAdded }.take(12)
        1 -> if(sortNewest) songs.sortedByDescending { it.dateAdded } else songs
        2 -> emptyList()
        else -> videos
    }
    val list = baseList.filter { query.isBlank() || it.title.contains(query,true) || it.artist.contains(query,true) }
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart=22.dp,bottomEnd=22.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFF7900),Color(0xFFFF087F),Color(0xFF7A16C8),Color(0xFF080817))))
            .padding(horizontal=17.dp, vertical=14.dp)) {
            Column {
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text("VIBE", color=Color.White, fontSize=28.sp, fontWeight=FontWeight.Black, letterSpacing=1.5.sp)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick={searchOpen=!searchOpen}) { Icon(Icons.Default.Search, "Search", tint=Color.White, modifier=Modifier.size(25.dp)) }
                    Spacer(Modifier.width(14.dp))
                    IconButton(onClick=onProfile) { Icon(Icons.Default.AccountCircle, "Profile", tint=Color.White, modifier=Modifier.size(26.dp)) }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                    libraryTabs.forEachIndexed { i, name ->
                        Column(Modifier.clickable { tab=i }.padding(vertical=3.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                            Text(name, color=if(tab==i) Color.White else Color.White.copy(alpha=.62f), fontSize=16.sp, fontWeight=if(tab==i) FontWeight.Bold else FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.width(if(tab==i)30.dp else 0.dp).height(3.dp).clip(RoundedCornerShape(4.dp)).background(Color.White))
                        }
                    }
                }
            }
        }
        if(searchOpen) OutlinedTextField(value=query,onValueChange={query=it},modifier=Modifier.fillMaxWidth().padding(horizontal=14.dp),singleLine=true,placeholder={Text("Search songs or artists")},trailingIcon={IconButton(onClick={query="";searchOpen=false}){Icon(Icons.Default.Close,null)}},colors=OutlinedTextFieldDefaults.colors(focusedTextColor=Color.White,unfocusedTextColor=Color.White,focusedBorderColor=Color(0xFFFF2D83),unfocusedBorderColor=Color.Gray))
        Row(Modifier.fillMaxWidth().padding(horizontal=18.dp, vertical=12.dp), verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={ if(tab==2) onCreatePlaylist() else (if(tab==3) videos.firstOrNull() else songs.firstOrNull())?.let(onPlay) }) {
                Icon(if(tab==2) Icons.Default.AddCircle else Icons.Default.PlayCircleFilled, null, tint=Color.White, modifier=Modifier.size(29.dp))
            }
            if(tab==2) Text("New playlist", color=Color.White, fontSize=14.sp)
            else Text(if(tab==3) "${videos.size} Videos" else "${songs.size} Songs", color=Color(0xFFBBAED1), fontSize=17.sp, modifier=Modifier.weight(1f))
            if(tab!=2) IconButton(onClick={sortNewest=!sortNewest}) { Icon(Icons.Default.Sort, "Sort", tint=Color(0xFFBBAED1)) }
        }
        when(tab) {
            2 -> {
                if(playlists.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QueueMusic, null, tint=Color(0xFFFF2D83), modifier=Modifier.size(50.dp))
                        Text("Your playlists will appear here", color=Color.LightGray)
                        TextButton(onClick=onCreatePlaylist) { Text("Create playlist from all songs", color=Color(0xFFFF2D83)) }
                    }
                } else LazyColumn(Modifier.weight(1f)) {
                    playlists.forEach { (name, ids) ->
                        item { Row(Modifier.fillMaxWidth().clickable { songs.firstOrNull { it.id in ids }?.let(onPlay) }.padding(horizontal=18.dp, vertical=13.dp), verticalAlignment=Alignment.CenterVertically) {
                            CoverTile(name.hashCode().toLong(), Modifier.size(54.dp)); Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) { Text(name, color=Color.White, fontWeight=FontWeight.SemiBold); Text("${ids.size} songs", color=Color.Gray, fontSize=11.sp) }
                            Icon(Icons.Default.PlayArrow, null, tint=Color(0xFFFF2D83))
                        } }
                    }
                    item { TextButton(onClick=onCreatePlaylist, modifier=Modifier.padding(12.dp)) { Text("+ Save all songs as playlist", color=Color(0xFFFF2D83)) } }
                }
            }
            else -> {
                if(list.isEmpty()) Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment=Alignment.Center) { Text("No media found. Allow access and refresh.", color=Color.LightGray) }
                else LazyColumn(Modifier.weight(1f), contentPadding=PaddingValues(bottom=8.dp)) {
                    items(list, key={it.id}) { track ->
                        Row(Modifier.fillMaxWidth().clickable { onPlay(track) }.padding(horizontal=14.dp, vertical=5.dp), verticalAlignment=Alignment.CenterVertically) {
                            TrackCover(track, Modifier.size(58.dp))
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text(track.title, color=Color.White, fontSize=15.sp, maxLines=1, overflow=TextOverflow.Ellipsis, fontWeight=FontWeight.Medium)
                                Row(verticalAlignment=Alignment.CenterVertically) {
                                    if(track.id in lyricsAvailable) {
                                        Box(Modifier.clip(RoundedCornerShape(4.dp)).border(1.dp,Color(0xFFFF2D83),RoundedCornerShape(4.dp)).padding(horizontal=4.dp, vertical=1.dp)) { Text("Lyric", color=Color(0xFFFF2D83), fontSize=9.sp) }
                                        Spacer(Modifier.width(5.dp))
                                    }
                                    Text(track.artist, color=Color(0xFF99949F), fontSize=12.sp, maxLines=1, overflow=TextOverflow.Ellipsis)
                                }
                            }
                            IconButton(onClick={onFavourite(track)}) { Icon(Icons.Default.MoreVert, "More", tint=Color(0xFFAAA5B0)) }
                        }
                    }
                }
            }
        }
        playingTrack?.let { track ->
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp, vertical=5.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFF292929)).clickable(onClick=onOpenPlayer).padding(horizontal=10.dp, vertical=7.dp), verticalAlignment=Alignment.CenterVertically) {
                TrackCover(track, Modifier.size(38.dp))
                Spacer(Modifier.width(10.dp))
                Text(track.title, color=Color.White, maxLines=1, overflow=TextOverflow.Ellipsis, modifier=Modifier.weight(1f), fontSize=14.sp)
                IconButton(onClick=onOpenPlayer) { Icon(Icons.Default.PlayCircleOutline, null, tint=Color.White) }
                Icon(Icons.Default.QueueMusic, null, tint=Color.White, modifier=Modifier.padding(horizontal=7.dp))
            }
        }
    }
}
@Composable private fun CoverTile(seed: Long, modifier: Modifier=Modifier) {
    val palettes=listOf(listOf(Color(0xFFEF476F),Color(0xFF541388)),listOf(Color(0xFFFF9F1C),Color(0xFF7B2CBF)),listOf(Color(0xFF00B4D8),Color(0xFF03045E)),listOf(Color(0xFF2EC4B6),Color(0xFF011627)),listOf(Color(0xFFFB5607),Color(0xFF3A0CA3)))
    val colors=palettes[kotlin.math.abs(seed%palettes.size).toInt()]
    Box(modifier.clip(RoundedCornerShape(7.dp)).background(Brush.linearGradient(colors)), contentAlignment=Alignment.Center) {
        Icon(Icons.Default.Album, null, tint=Color.White.copy(alpha=.9f), modifier=Modifier.fillMaxSize(.63f))
    }
}


@Composable
private fun TrackCover(track:Track,modifier:Modifier=Modifier) {
    val context=LocalContext.current
    val artwork by produceState<Bitmap?>(null,track.uri) {
        value=withContext(Dispatchers.IO) {
            runCatching {
                val retriever=MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context,track.uri)
                    retriever.embeddedPicture?.let { bytes -> android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.size) }
                } finally { retriever.release() }
            }.getOrNull()
        }
    }
    val palettes=listOf(
        listOf(Color(0xFFFF2D83),Color(0xFF541388)),
        listOf(Color(0xFFFF9F1C),Color(0xFF7B2CBF)),
        listOf(Color(0xFF00B4D8),Color(0xFF03045E)),
        listOf(Color(0xFF2EC4B6),Color(0xFF011627)),
        listOf(Color(0xFFFB5607),Color(0xFF3A0CA3))
    )
    val colors=palettes[kotlin.math.abs(track.id%palettes.size).toInt()]
    Box(modifier.clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(colors)),contentAlignment=Alignment.Center) {
        if(artwork!=null) Image(artwork!!.asImageBitmap(),contentDescription="Album cover for ${track.title}",modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        else Icon(if(track.isVideo)Icons.Default.PlayCircle else Icons.Default.MusicNote,null,tint=Color.White.copy(alpha=.95f),modifier=Modifier.fillMaxSize(.62f))
    }
}
