package com.vibe.music

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

private val VibePink=Color(0xFFFF168A)
private val VibeText=Color(0xFFF8F3FA)
private val VibeMuted=Color(0xFFAAA1B2)
private val VibePanel=Color(0xFF21101F)

private fun vibePalette(theme:String):List<Color> = when(theme){
 "Fiery Rhythms"->listOf(Color(0xFFFF6A00),Color(0xFFFF1744),Color(0xFF16090C))
 "Radiant Nights"->listOf(Color(0xFFFF1493),Color(0xFF5B16C9),Color(0xFF10071B))
 "Blue Ocean"->listOf(Color(0xFF7CEBFF),Color(0xFF2376D8),Color(0xFF061726))
 "Rainforest"->listOf(Color(0xFF25D366),Color(0xFF075B45),Color(0xFF061712))
 "Green Vibe"->listOf(Color(0xFFB5FFD7),Color(0xFF18B978),Color(0xFF071B13))
 "Muso Style"->listOf(Color(0xFFFF8A00),Color(0xFFFF0080),Color(0xFF241078))
 else->listOf(Color(0xFFFF168A),Color(0xFF7900A8),Color(0xFF160014))
}

@Composable
fun VibePlayerScreen(
 theme:String,playerStyle:String,player:androidx.media3.session.MediaController?,track:Track?,position:Long,duration:Long,playing:Boolean,shuffle:Boolean,repeat:Int,audioOnly:Boolean,lyrics:String,loading:Boolean,
 loadLyrics:()->Unit,seek:(Long)->Unit,previous:()->Unit,next:()->Unit,toggle:()->Unit,toggleShuffle:()->Unit,toggleRepeat:()->Unit,toggleAudio:()->Unit,close:()->Unit
){
 var lyricsPage by remember{mutableStateOf(false)}
 var drag by remember{mutableFloatStateOf(0f)}
 val lines=remember(lyrics){LyricsClient.parseLrc(lyrics)}
 val active=lines.indexOfLast{it.timeMs<=position}.coerceAtLeast(0)
 val palette=vibePalette(theme)
 val context=LocalContext.current
 val artwork by produceState<Bitmap?>(null,track?.uri){
  value=withContext(Dispatchers.IO){
   runCatching {
    val uri=track?.uri ?: return@runCatching null
    val retriever=MediaMetadataRetriever()
    try {
     retriever.setDataSource(context,uri)
     retriever.embeddedPicture?.let{bytes->android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.size)}
    } finally { retriever.release() }
   }.getOrNull()
  }
 }
 Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(palette[0].copy(alpha=.9f),palette[1],palette[2]))).pointerInput(lyricsPage,lyrics){detectHorizontalDragGestures(onHorizontalDrag={_,amount->drag+=amount},onDragEnd={if(drag < -45){lyricsPage=true;if(lyrics.isBlank())loadLyrics()} else if(drag>45)lyricsPage=false;drag=0f})}){
  if(!lyricsPage){
   Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp),horizontalAlignment=Alignment.CenterHorizontally){
    Row(Modifier.fillMaxWidth().padding(top=8.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=close){Icon(Icons.Default.KeyboardArrowDown,"Back",tint=Color.White,modifier=Modifier.size(30.dp))};Column(Modifier.weight(1f)){Text(track?.title?:"Now playing",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(track?.artist.orEmpty(),color=Color.White.copy(alpha=.72f),fontSize=14.sp,maxLines=1,overflow=TextOverflow.Ellipsis)};IconButton(onClick={lyricsPage=true;if(lyrics.isBlank())loadLyrics()}){Icon(Icons.Default.Tune,"Player options",tint=Color.White)}}
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth().height(330.dp),contentAlignment=Alignment.Center){
     Box(Modifier.size(300.dp).clip(RoundedCornerShape(50)).background(Brush.radialGradient(listOf(Color(0xFFFFC5B7),palette[0],Color(0xFF21051D)))),contentAlignment=Alignment.Center){
      Box(Modifier.size(246.dp).clip(RoundedCornerShape(50)).background(Brush.radialGradient(listOf(Color(0xFF17121F),Color(0xFF3D164B),Color(0xFF100916)))),contentAlignment=Alignment.Center){
       Box(Modifier.size(168.dp).clip(if(playerStyle=="Round"||playerStyle=="Rose Vinyl")RoundedCornerShape(50) else RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Color(0xFFFFE1D1),Color(0xFFFF4E9C),Color(0xFF6520A2)))),contentAlignment=Alignment.Center){if(artwork!=null)Image(artwork!!.asImageBitmap(),contentDescription="Album artwork",modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)else Icon(Icons.Default.Album,null,tint=Color.White,modifier=Modifier.size(90.dp))}
      }
     }
    }
    Text(track?.title?:"Choose a song",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis)
    Text(track?.artist.orEmpty(),color=Color.White.copy(alpha=.72f),fontSize=15.sp)
    Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.SpaceEvenly){IconButton(onClick={}){Icon(Icons.Default.FavoriteBorder,"Favourite",tint=Color.White)};IconButton(onClick={}){Icon(Icons.Default.Add,"Add",tint=Color.White)};IconButton(onClick={}){Icon(Icons.Default.Timer,"Sleep timer",tint=Color.White)};IconButton(onClick={lyricsPage=true;if(lyrics.isBlank())loadLyrics()}){Icon(Icons.Default.Lyrics,"Lyrics",tint=Color.White)};IconButton(onClick={}){Icon(Icons.Default.MoreVert,"More",tint=Color.White)}}
    Slider(value=if(duration>0)(position.toFloat()/duration).coerceIn(0f,1f)else 0f,onValueChange={seek((it*duration).toLong())},colors=SliderDefaults.colors(thumbColor=Color.White,activeTrackColor=Color.White,inactiveTrackColor=Color.White.copy(alpha=.25f)))
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(timeLabel(position),color=Color.White,fontSize=11.sp);Text(timeLabel(duration),color=Color.White,fontSize=11.sp)}
    Row(Modifier.fillMaxWidth().padding(top=14.dp,bottom=28.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
     IconButton(onClick=toggleShuffle){Icon(Icons.Default.Shuffle,"Shuffle",tint=if(shuffle)Color.White else Color.White.copy(alpha=.6f),modifier=Modifier.size(26.dp))}
     IconButton(onClick=previous){Icon(Icons.Default.SkipPrevious,"Previous",tint=Color.White,modifier=Modifier.size(38.dp))}
     FilledIconButton(onClick=toggle,modifier=Modifier.size(86.dp),colors=IconButtonDefaults.filledIconButtonColors(containerColor=VibePink)){Icon(if(playing)Icons.Default.Pause else Icons.Default.PlayArrow,"Play pause",tint=Color.White,modifier=Modifier.size(52.dp))}
     IconButton(onClick=next){Icon(Icons.Default.SkipNext,"Next",tint=Color.White,modifier=Modifier.size(38.dp))}
     IconButton(onClick=toggleRepeat){Icon(Icons.Default.Repeat,"Repeat",tint=if(repeat==0)Color.White.copy(alpha=.6f)else Color.White,modifier=Modifier.size(26.dp))}
    }
    Text("SWIPE LEFT FOR LYRICS",color=Color.White.copy(alpha=.65f),fontSize=10.sp,letterSpacing=2.sp,modifier=Modifier.padding(bottom=16.dp))
    if(track?.isVideo==true)Row(verticalAlignment=Alignment.CenterVertically){Text("Audio only",color=Color.White);Switch(checked=audioOnly,onCheckedChange={toggleAudio()})}
   }
  }else{
   Column(Modifier.fillMaxSize().padding(horizontal=25.dp,vertical=18.dp)){
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick={lyricsPage=false}){Icon(Icons.Default.ArrowBack,"Back to player",tint=Color.White)};Text("Lyrics",Modifier.weight(1f),color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold);TextButton(onClick={if(lyrics.isBlank())loadLyrics()}){Text("Refresh",color=VibePink)}}
    Text(track?.title.orEmpty(),color=Color.White.copy(alpha=.7f),fontSize=13.sp)
    Spacer(Modifier.height(22.dp))
    if(loading)Text("Finding lyrics…",color=Color.White)
    else if(lyrics.isBlank()||lyrics.startsWith("No lyrics"))Text("Lyrics aren't available yet. Tap Refresh to search.",color=Color.White.copy(alpha=.8f))
    else if(lines.isNotEmpty())LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(vertical=30.dp),verticalArrangement=Arrangement.spacedBy(20.dp)){
     items(lines){line->val isActive=line==lines.getOrNull(active);Text(line.text,color=if(isActive)Color.White else Color.White.copy(alpha=.52f),fontSize=if(isActive)25.sp else 19.sp,fontWeight=if(isActive)FontWeight.Bold else FontWeight.Medium,modifier=Modifier.clickable{seek(line.timeMs)}.padding(vertical=2.dp))}
    }else Text(lyrics,color=Color.White,fontSize=19.sp,lineHeight=30.sp)
    Text("SWIPE RIGHT FOR PLAYER",color=Color.White.copy(alpha=.6f),fontSize=10.sp,letterSpacing=2.sp,modifier=Modifier.align(Alignment.CenterHorizontally).padding(vertical=16.dp))
   }
  }
 }
}



@Composable
internal fun DancingPet(playing:Boolean) {
    val dance = rememberInfiniteTransition(label="vibe-pet-dance")
    val bounce by dance.animateFloat(
        initialValue=0f, targetValue=1f,
        animationSpec=infiniteRepeatable(tween(if(playing)190 else 900, easing=FastOutSlowInEasing), RepeatMode.Reverse),
        label="pet-bounce"
    )
    val sway by dance.animateFloat(
        initialValue=-7f, targetValue=7f,
        animationSpec=infiniteRepeatable(tween(if(playing)260 else 1200, easing=FastOutSlowInEasing), RepeatMode.Reverse),
        label="pet-sway"
    )
    Box(
        Modifier.size(88.dp).graphicsLayer {
            translationY = if(playing) -bounce*10f else -2f
            rotationZ = if(playing) sway else 0f
            scaleX = 1f + if(playing) bounce*.035f else 0f
            scaleY = 1f + if(playing) bounce*.035f else 0f
        },
        contentAlignment=Alignment.Center
    ) {
        Box(Modifier.size(78.dp).clip(RoundedCornerShape(26.dp)).background(Brush.radialGradient(listOf(Color(0x66FF168A),Color(0x00100612)))),contentAlignment=Alignment.Center) {
            Text("🐶",fontSize=57.sp)
            Icon(Icons.Default.Headphones,null,tint=Color(0xFFFF55B5),modifier=Modifier.align(Alignment.TopCenter).padding(top=1.dp).size(23.dp))
        }
        if(playing) Text("♪",color=Color(0xFFFF78C4),fontSize=20.sp,fontWeight=FontWeight.Black,modifier=Modifier.align(Alignment.TopEnd))
    }
}
