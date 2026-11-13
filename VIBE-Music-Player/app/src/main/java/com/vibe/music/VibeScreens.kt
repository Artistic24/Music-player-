package com.vibe.music

import androidx.compose.foundation.*
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
 theme:String,player:androidx.media3.session.MediaController?,track:Track?,position:Long,duration:Long,playing:Boolean,shuffle:Boolean,repeat:Int,audioOnly:Boolean,lyrics:String,loading:Boolean,
 loadLyrics:()->Unit,seek:(Long)->Unit,previous:()->Unit,next:()->Unit,toggle:()->Unit,toggleShuffle:()->Unit,toggleRepeat:()->Unit,toggleAudio:()->Unit,close:()->Unit
){
 var lyricsPage by remember{mutableStateOf(false)}
 var drag by remember{mutableFloatStateOf(0f)}
 val lines=remember(lyrics){LyricsClient.parseLrc(lyrics)}
 val active=lines.indexOfLast{it.timeMs<=position}.coerceAtLeast(0)
 val palette=vibePalette(theme)
 Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(palette[0].copy(alpha=.9f),palette[1],palette[2]))).pointerInput(lyricsPage,lyrics){detectHorizontalDragGestures(onHorizontalDrag={_,amount->drag+=amount},onDragEnd={if(drag < -45){lyricsPage=true;if(lyrics.isBlank())loadLyrics()} else if(drag>45)lyricsPage=false;drag=0f})}){
  if(!lyricsPage){
   Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp),horizontalAlignment=Alignment.CenterHorizontally){
    Row(Modifier.fillMaxWidth().padding(top=8.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=close){Icon(Icons.Default.KeyboardArrowDown,"Back",tint=Color.White,modifier=Modifier.size(30.dp))};Column(Modifier.weight(1f)){Text(track?.title?:"Now playing",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(track?.artist.orEmpty(),color=Color.White.copy(alpha=.72f),fontSize=14.sp,maxLines=1,overflow=TextOverflow.Ellipsis)};IconButton(onClick={lyricsPage=true;if(lyrics.isBlank())loadLyrics()}){Icon(Icons.Default.Tune,"Player options",tint=Color.White)}}
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth().height(330.dp),contentAlignment=Alignment.Center){
     Box(Modifier.size(300.dp).clip(RoundedCornerShape(50)).background(Brush.radialGradient(listOf(Color(0xFFFFC5B7),palette[0],Color(0xFF21051D)))),contentAlignment=Alignment.Center){
      Box(Modifier.size(246.dp).clip(RoundedCornerShape(50)).background(Brush.radialGradient(listOf(Color(0xFF17121F),Color(0xFF3D164B),Color(0xFF100916)))),contentAlignment=Alignment.Center){
       Box(Modifier.size(168.dp).clip(RoundedCornerShape(50)).background(Brush.linearGradient(listOf(Color(0xFFFFE1D1),Color(0xFFFF4E9C),Color(0xFF6520A2)))),contentAlignment=Alignment.Center){Icon(Icons.Default.Album,null,tint=Color.White,modifier=Modifier.size(90.dp))}
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
fun VibeProfilePage(themeName:String,playerStyle:String,onTheme:(String)->Unit,onStyle:(String)->Unit,onFix:()->Unit,onInfo:()->Unit){
 val themes=listOf("Muso Style","Fiery Rhythms","Radiant Nights","Blue Ocean","Rainforest","Green Vibe")
 val styles=listOf("Vibrant Splash","Colorful Frenzy","Rose Vinyl","Petal Groove","Forest Voice","Winter Cottage")
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp)){
  Text("Me",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Bold)
  Text("Personalization",color=VibeMuted,fontSize=14.sp)
  Spacer(Modifier.height(16.dp))
  Row(horizontalArrangement=Arrangement.spacedBy(18.dp)){listOf("Player Style","Cool Mode","Theme","Lock screen").forEach{Text(it,color=if(it=="Theme")Color.White else VibeMuted,fontWeight=if(it=="Theme")FontWeight.Bold else FontWeight.Normal,fontSize=14.sp)}} 
  Text("Themes",color=Color.White,fontSize=21.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=18.dp,bottom=10.dp))
  themes.chunked(2).forEach{pair->Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){pair.forEach{name->val colors=vibePalette(name);Column(Modifier.weight(1f).clickable{onTheme(name)}.clip(RoundedCornerShape(17.dp)).background(Brush.linearGradient(colors)).border(if(themeName==name)2.dp else 1.dp,if(themeName==name)Color.White else Color.White.copy(alpha=.25f),RoundedCornerShape(17.dp)).padding(11.dp)){Box(Modifier.fillMaxWidth().height(82.dp).clip(RoundedCornerShape(11.dp)).background(Color.Black.copy(alpha=.45f)),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Album,null,tint=colors[0],modifier=Modifier.size(40.dp));Text("VIBE  •  Songs  •  Playlist",color=Color.White,fontSize=8.sp)}};Text(name,color=Color.White,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp));Text(if(themeName==name)"✓ Selected" else "Tap to apply",color=Color.White.copy(alpha=.75f),fontSize=10.sp)}};if(pair.size==1)Spacer(Modifier.weight(1f))}}
  Text("Player styles",color=Color.White,fontSize=21.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=22.dp,bottom=10.dp))
  styles.chunked(2).forEach{pair->Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){pair.forEachIndexed{index,name->val colors=listOf(listOf(Color(0xFFFFA75B),Color(0xFFFF168A),Color(0xFF39105B)),listOf(Color(0xFF42C8FF),Color(0xFFB51AFF),Color(0xFF241044)),listOf(Color(0xFFFFA4B6),Color(0xFFFF244F),Color(0xFF391020)),listOf(Color(0xFFFFD0D7),Color(0xFFEF376C),Color(0xFF3C102A)),listOf(Color(0xFF5EE58B),Color(0xFF126B45),Color(0xFF09251C)),listOf(Color(0xFFB9E8FF),Color(0xFF337CBF),Color(0xFF081D39)))[styles.indexOf(name)];Column(Modifier.weight(1f).clickable{onStyle(name)}.clip(RoundedCornerShape(17.dp)).background(Brush.linearGradient(colors)).padding(12.dp)){Box(Modifier.fillMaxWidth().height(110.dp),contentAlignment=Alignment.Center){Icon(Icons.Default.Album,null,tint=Color.White,modifier=Modifier.size(75.dp))};Text(name,color=Color.White,fontWeight=FontWeight.Bold);Text(if(playerStyle==name)"✓ Selected" else "Tap to select",color=Color.White.copy(alpha=.75f),fontSize=10.sp)}};if(pair.size==1)Spacer(Modifier.weight(1f))}}
  Spacer(Modifier.height(20.dp))
  listOf("Fix Song Info" to Icons.Default.Build,"MP3 Converter" to Icons.Default.MusicVideo,"Hide & Scan Songs" to Icons.Default.VisibilityOff,"Music Pet" to Icons.Default.Pets).forEach{(label,ic)->Row(Modifier.fillMaxWidth().padding(vertical=5.dp).clip(RoundedCornerShape(14.dp)).background(VibePanel).clickable{if(label=="Fix Song Info")onFix()else onInfo()}.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(ic,null,tint=VibePink);Spacer(Modifier.width(13.dp));Text(label,Modifier.weight(1f),color=Color.White,fontWeight=FontWeight.SemiBold);Icon(Icons.Default.ChevronRight,null,tint=VibeMuted)}}
 }
}

@Composable
fun VibeFixSongInfoPage(songs:List<Track>,overrides:Map<Long,Pair<String,String>>,onSave:(Track,String,String)->Unit,onUndo:(Track)->Unit){
 var query by remember{mutableStateOf("")}
 var selected by remember{mutableStateOf<Track?>(null)}
 var title by remember{mutableStateOf("")}
 var artist by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().padding(horizontal=18.dp)){
  Text("Fix song info",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.Bold)
  Text("Correct song title and artist in your VIBE library",color=VibeMuted,fontSize=12.sp,modifier=Modifier.padding(bottom=12.dp))
  OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Find a song…")},singleLine=true)
  if(selected==null)LazyColumn(Modifier.weight(1f)){items(songs.filter{it.title.contains(query,true)||it.artist.contains(query,true)}.take(100)){track->Row(Modifier.fillMaxWidth().clickable{selected=track;title=track.title;artist=track.artist}.padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.MusicNote,null,tint=VibePink);Column(Modifier.weight(1f).padding(start=10.dp)){Text(track.title,color=Color.White);Text(track.artist,color=VibeMuted,fontSize=11.sp)};if(track.id in overrides)Text("Edited",color=VibePink,fontSize=10.sp)}}}
  else{Text("Selected: ${selected?.title}",color=VibeMuted,modifier=Modifier.padding(vertical=12.dp));OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text("Song title")});Spacer(Modifier.height(8.dp));OutlinedTextField(artist,{artist=it},Modifier.fillMaxWidth(),label={Text("Artist")});Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={selected?.let{onSave(it,title.trim(),artist.trim())};selected=null},enabled=title.isNotBlank(),colors=ButtonDefaults.buttonColors(containerColor=VibePink)){Text("Save correction")};TextButton(onClick={selected?.let(onUndo);selected=null}){Text("Undo fix",color=VibePink)};TextButton(onClick={selected=null}){Text("Cancel",color=VibeMuted)}}}
 }
}
