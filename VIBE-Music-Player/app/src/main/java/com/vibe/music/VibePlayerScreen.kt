package com.vibe.music

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

@Composable
internal fun VibePlayerScreen(
 themeName:String,player:androidx.media3.session.MediaController?,track:Track?,position:Long,duration:Long,playing:Boolean,
 shuffle:Boolean,repeat:Int,audioOnly:Boolean,lyrics:String,loadingLyrics:Boolean,loadLyrics:()->Unit,
 seek:(Long)->Unit,previous:()->Unit,next:()->Unit,toggle:()->Unit,toggleShuffle:()->Unit,
 toggleRepeat:()->Unit,toggleAudio:()->Unit,close:()->Unit
){
 var lyricsPage by remember{mutableStateOf(false)}
 var drag by remember{mutableFloatStateOf(0f)}
 val colors=when(themeName){
  "Muso Style"->listOf(Color(0xFFFF7900),Color(0xFFEC008C),Color(0xFF10051B))
  "Fiery Rhythms"->listOf(Color(0xFFFF3D00),Color(0xFF7A001F),Color(0xFF100308))
  "Radiant Nights"->listOf(Color(0xFFFF27B8),Color(0xFF5420C8),Color(0xFF06112D))
  "Blue Ocean"->listOf(Color(0xFF8DEBFF),Color(0xFF168DD1),Color(0xFF061321))
  "Rainforest"->listOf(Color(0xFF00D9A6),Color(0xFF087A70),Color(0xFF050D13))
  "Green Vibe"->listOf(Color(0xFFB9FFD0),Color(0xFF35C878),Color(0xFF07180F))
  else->when(track?.title?.hashCode()?.let{kotlin.math.abs(it%6)}){
  0->listOf(Color(0xFFFF7900),Color(0xFFEC008C),Color(0xFF10051B))
  1->listOf(Color(0xFFFF3D00),Color(0xFF7A001F),Color(0xFF100308))
  2->listOf(Color(0xFFFF27B8),Color(0xFF5420C8),Color(0xFF06112D))
  3->listOf(Color(0xFF8DEBFF),Color(0xFF168DD1),Color(0xFF061321))
  4->listOf(Color(0xFF00D9A6),Color(0xFF087A70),Color(0xFF050D13))
  else->listOf(Color(0xFFB9FFD0),Color(0xFF35C878),Color(0xFF07180F))
 }
 }
 Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)).pointerInput(lyricsPage){
  detectHorizontalDragGestures(onDragStart={drag=0f},onHorizontalDrag={change,amount->drag+=amount;change.consume()},onDragEnd={if(drag < -55f)lyricsPage=true else if(drag>55f)lyricsPage=false})
 }){
  Column(Modifier.fillMaxSize().padding(horizontal=20.dp),horizontalAlignment=Alignment.CenterHorizontally){
   Row(Modifier.fillMaxWidth().padding(top=7.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
    IconButton(onClick=close){Icon(Icons.Default.KeyboardArrowDown,null,tint=Color.White)}
    Text(if(lyricsPage)"LYRICS" else "NOW PLAYING",color=Color.White,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
    IconButton(onClick={lyricsPage=!lyricsPage}){Icon(if(lyricsPage)Icons.Default.Album else Icons.Default.Lyrics,null,tint=Color.White)}
   }
   if(lyricsPage){
    Spacer(Modifier.height(16.dp))
    Text(track?.title?:"No track selected",color=Color.White,fontSize=21.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis)
    Text(track?.artist.orEmpty(),color=Color.White.copy(alpha=.75f))
    Spacer(Modifier.height(22.dp))
    if(loadingLyrics)CircularProgressIndicator(color=Color.White)
    else if(lyrics.isBlank()||lyrics.startsWith("No lyrics")){
     Text("Lyrics aren't loaded yet.",color=Color.White.copy(alpha=.8f))
     Button(onClick=loadLyrics,colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFF2384))){Text("Find lyrics")}
    }else{
     val timed=remember(lyrics){LyricsClient.parseLrc(lyrics)}
     if(timed.isNotEmpty()){
      val active=timed.indexOfLast{it.timeMs<=position}.coerceAtLeast(0)
      Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){
       timed.forEachIndexed{index,line->Text(line.text.ifBlank{"♪"},color=if(index==active)Color.White else Color.White.copy(alpha=.45f),fontSize=if(index==active)22.sp else 15.sp,fontWeight=if(index==active)FontWeight.Bold else FontWeight.Medium,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth().padding(vertical=8.dp))}
      }
     }else Text(lyrics,color=Color.White,fontSize=17.sp,lineHeight=27.sp,textAlign=TextAlign.Center,modifier=Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp))
    }
    Spacer(Modifier.weight(1f));Text("SWIPE RIGHT FOR PLAYER",color=Color.White.copy(alpha=.65f),fontSize=10.sp,letterSpacing=1.5.sp);Spacer(Modifier.height(18.dp))
   }else{
    Spacer(Modifier.height(15.dp))
    Box(Modifier.fillMaxWidth().weight(1f),contentAlignment=Alignment.Center){
     Box(Modifier.fillMaxWidth(.92f).aspectRatio(1f).clip(RoundedCornerShape(999.dp)).background(Brush.sweepGradient(listOf(Color(0xFF07050D),Color(0xFF432044),Color(0xFF080610),Color(0xFF4A1744),Color(0xFF07050D)))),contentAlignment=Alignment.Center){
      Box(Modifier.fillMaxWidth(.78f).aspectRatio(1f).clip(RoundedCornerShape(999.dp)).background(Brush.radialGradient(listOf(Color(0xFFFFB4C8),Color(0xFFFF2384),Color(0xFF7C0C68)))),contentAlignment=Alignment.Center){
       Box(Modifier.fillMaxWidth(.76f).aspectRatio(1f).clip(RoundedCornerShape(999.dp)).background(Brush.linearGradient(listOf(colors[0],colors[1],colors[2]))),contentAlignment=Alignment.Center){Icon(Icons.Default.Album,null,tint=Color.White,modifier=Modifier.size(88.dp))}
      }
     }
    }
    Text(track?.title?:"Nothing playing",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis)
    Text(track?.artist.orEmpty(),color=Color.White.copy(alpha=.75f),fontSize=14.sp)
    Spacer(Modifier.height(12.dp))
    Slider(value=if(duration>0)(position.toFloat()/duration).coerceIn(0f,1f)else 0f,onValueChange={seek((it*duration).toLong())},colors=SliderDefaults.colors(thumbColor=Color.White,activeTrackColor=Color(0xFFFF2384),inactiveTrackColor=Color.White.copy(alpha=.25f)))
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(vibeTimeLabel(position),color=Color.White,fontSize=11.sp);Text(vibeTimeLabel(duration),color=Color.White,fontSize=11.sp)}
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){
     IconButton(onClick=toggleShuffle){Icon(Icons.Default.Shuffle,null,tint=if(shuffle)Color(0xFFFF8BC2)else Color.White)}
     IconButton(onClick=previous){Icon(Icons.Default.SkipPrevious,null,tint=Color.White,modifier=Modifier.size(37.dp))}
     FilledIconButton(onClick=toggle,colors=IconButtonDefaults.filledIconButtonColors(containerColor=Color(0xFFFF2384)),modifier=Modifier.size(76.dp)){Icon(if(playing)Icons.Default.Pause else Icons.Default.PlayArrow,null,tint=Color.White,modifier=Modifier.size(43.dp))}
     IconButton(onClick=next){Icon(Icons.Default.SkipNext,null,tint=Color.White,modifier=Modifier.size(37.dp))}
     IconButton(onClick=toggleRepeat){Icon(Icons.Default.Repeat,null,tint=if(repeat==androidx.media3.common.Player.REPEAT_MODE_OFF)Color.White else Color(0xFFFF8BC2))}
    }
    if(track?.isVideo==true)Row(verticalAlignment=Alignment.CenterVertically){Text("Audio-only",color=Color.White,fontSize=12.sp);Switch(checked=audioOnly,onCheckedChange={toggleAudio()})}
    Spacer(Modifier.height(10.dp));Text("SWIPE LEFT FOR LYRICS",color=Color.White.copy(alpha=.65f),fontSize=10.sp,letterSpacing=1.5.sp);Spacer(Modifier.height(14.dp))
   }
  }
 }
}
private fun vibeTimeLabel(ms:Long):String{val s=ms.coerceAtLeast(0)/1000;return "%d:%02d".format(s/60,s%60)}
