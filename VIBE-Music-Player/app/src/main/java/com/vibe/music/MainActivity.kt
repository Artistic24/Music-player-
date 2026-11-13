package com.vibe.music

import android.Manifest
import android.content.ComponentName
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.*
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val Night=Color(0xFF100D20); private val Card=Color(0xFF211B36); private val Purple=Color(0xFF9B6BFF); private val Aqua=Color(0xFF35D6E8); private val Rose=Color(0xFFFF5EA8); private val Amber=Color(0xFFFFA34D)
class MainActivity:ComponentActivity(){
    private var mediaController by mutableStateOf<MediaController?>(null); private var permissionVersion by mutableIntStateOf(0); private var controllerFuture:ListenableFuture<MediaController>?=null
    private val permissionRequest=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permissionVersion++}
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState); askMediaPermission(); val token=SessionToken(this,ComponentName(this,PlaybackService::class.java)); controllerFuture=MediaController.Builder(this,token).buildAsync(); controllerFuture?.addListener({try{mediaController=controllerFuture?.get()}catch(_:Exception){}},ContextCompat.getMainExecutor(this)); setContent { MaterialTheme(colorScheme=darkColorScheme(background=Night,surface=Card,primary=Purple,secondary=Aqua,tertiary=Rose)){ VibeApp(mediaController,permissionVersion) } } }
    private fun askMediaPermission(){val p=if(Build.VERSION.SDK_INT>=33) arrayOf(Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.READ_MEDIA_VIDEO) else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE); if(p.any{ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED})permissionRequest.launch(p)}
    override fun onDestroy(){controllerFuture?.let{MediaController.releaseFuture(it)};super.onDestroy()}
}
private enum class Page(val label:String){HOME("For You"),MUSIC("Music"),VIDEOS("Videos"),CONVERT("Convert"),PROFILE("Me"),FIX("Fix Info"),HIDE("Hide & Scan")}

@Composable private fun VibeApp(player:MediaController?,permissionVersion:Int){
    val context= LocalContext.current; val scope= rememberCoroutineScope(); val store= remember{UserLibraryStore(context)}
    var page by remember{mutableStateOf(Page.HOME)}; var themeName by remember{mutableStateOf(store.setting("theme","Neon Pulse"))}; var playerStyle by remember{mutableStateOf(store.setting("player_style","Classic"))}; var metadataOverrides by remember{mutableStateOf(store.metadataOverrides())}; var playlists by remember{mutableStateOf(store.playlists())}; var songs by remember{mutableStateOf(emptyList<Track>())}; var videos by remember{mutableStateOf(emptyList<Track>())}; var selected by remember{mutableStateOf<Track?>(null)}
    var counts by remember{mutableStateOf(store.playCounts())}; var favourites by remember{mutableStateOf(store.favourites())}; var hiddenIds by remember{mutableStateOf(store.hiddenTracks())}; var petEnabled by remember{mutableStateOf(store.setting("music_pet","true")=="true")}; var lyrics by remember{mutableStateOf("")}; var loadingLyrics by remember{mutableStateOf(false)}; var playing by remember{mutableStateOf(false)}; var status by remember{mutableStateOf("Preparing your library…")}; var showNow by remember{mutableStateOf(false)}; var audioOnly by remember{mutableStateOf(false)}; var position by remember{mutableLongStateOf(0L)}; var duration by remember{mutableLongStateOf(0L)}; var shuffle by remember{mutableStateOf(false)}; var repeat by remember{mutableIntStateOf(Player.REPEAT_MODE_OFF)}
    fun refresh(){scope.launch{val rawSongs=withContext(Dispatchers.IO){MediaLibrary.audio(context)};val rawVideos=withContext(Dispatchers.IO){MediaLibrary.videos(context)};metadataOverrides=store.metadataOverrides();songs=rawSongs.map{t->metadataOverrides[t.id]?.let{t.copy(title=it.first,artist=it.second)}?:t};videos=rawVideos.map{t->metadataOverrides[t.id]?.let{t.copy(title=it.first,artist=it.second)}?:t};status="${songs.size} songs • ${videos.size} videos found"}}
    LaunchedEffect(permissionVersion){refresh()}
    DisposableEffect(player){val l=object:Player.Listener{override fun onIsPlayingChanged(v:Boolean){playing=v};override fun onMediaItemTransition(mediaItem:MediaItem?,reason:Int){val title=mediaItem?.mediaMetadata?.title?.toString(); selected=(songs+videos).firstOrNull{it.title==title} ?: selected};override fun onPlaybackStateChanged(state:Int){duration=player?.duration?.coerceAtLeast(0L)?:0L}};player?.addListener(l);onDispose{player?.removeListener(l)}}
    LaunchedEffect(player){while(true){if(player!=null){position=player.currentPosition.coerceAtLeast(0L);duration=player.duration.coerceAtLeast(0L);playing=player.isPlaying};delay(500)}}
    fun loadLyrics(t:Track){loadingLyrics=true;scope.launch{val key=LyricsClient.cacheKey(t);val cached=store.lyrics(key);val value=cached?:withContext(Dispatchers.IO){runCatching{LyricsClient.find(t)?.display().orEmpty()}.getOrDefault("")}.also{if(it.isNotBlank())store.saveLyrics(key,it)};lyrics=value.ifBlank{"No lyrics found for this track."};loadingLyrics=false}}
    fun playFrom(list:List<Track>,track:Track){if(player==null){status="Player is connecting. Try again shortly.";return};val items=list.map{t->MediaItem.Builder().setUri(t.uri).setMediaMetadata(MediaMetadata.Builder().setTitle(t.title).setArtist(t.artist).build()).build()};val i=list.indexOfFirst{it.id==track.id}.coerceAtLeast(0);player.setMediaItems(items,i,0L);player.prepare();player.play();selected=track;store.incrementPlay(track.id);counts=store.playCounts();status="Playing ${track.title}";if(track.isVideo){audioOnly=false;showNow=true}}
    Scaffold(floatingActionButtonPosition=FabPosition.Start,floatingActionButton={if(playing && petEnabled) DancingPet(true) },containerColor=when(themeName){"Fiery Rhythms"->Color(0xFF16090C);"Radiant Nights"->Color(0xFF10071B);"Blue Ocean"->Color(0xFF061726);"Rainforest"->Color(0xFF061712);"Green Vibe"->Color(0xFF071B13);"Muso Style"->Color(0xFF100B19);else->Night},bottomBar={NavigationBar(containerColor=Color(0xFF171328)){listOf(Page.HOME,Page.MUSIC,Page.VIDEOS,Page.CONVERT,Page.PROFILE).forEach{p->NavigationBarItem(selected=if(p==Page.PROFILE) page==Page.PROFILE||page==Page.FIX||page==Page.HIDE else page==p,onClick={page=p},icon={Icon(when(p){Page.HOME->Icons.Default.Home;Page.MUSIC->Icons.Default.LibraryMusic;Page.VIDEOS->Icons.Default.VideoLibrary;Page.CONVERT->Icons.Default.AutoFixHigh;Page.PROFILE->Icons.Default.Person;Page.FIX->Icons.Default.Edit;Page.HIDE->Icons.Default.VisibilityOff},p.label)},label={Text(p.label,fontSize=10.sp)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Aqua,selectedTextColor=Aqua,indicatorColor=Purple.copy(alpha=.18f),unselectedIconColor=Color.LightGray,unselectedTextColor=Color.LightGray))}}}){pad->
        Column(Modifier.fillMaxSize().padding(pad)){
            Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Column{Text("VIBE",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Black,letterSpacing=3.sp);Text("YOUR MUSIC. YOUR MOOD.",color=Aqua,fontSize=9.sp,letterSpacing=1.5.sp)};IconButton(onClick={refresh()}){Icon(Icons.Default.Refresh,"Refresh",tint=Color.White)}}
            if(showNow){VibePlayerScreen(themeName,playerStyle,player,selected,position,duration,playing,shuffle,repeat,audioOnly,lyrics,loadingLyrics,{selected?.let{loadLyrics(it)}},{p->player?.seekTo(p)},{player?.seekToPreviousMediaItem()},{player?.seekToNextMediaItem()},{if(playing)player?.pause() else player?.play()},{shuffle=!shuffle;player?.shuffleModeEnabled=shuffle},{repeat=when(repeat){Player.REPEAT_MODE_OFF->Player.REPEAT_MODE_ONE;Player.REPEAT_MODE_ONE->Player.REPEAT_MODE_ALL;else->Player.REPEAT_MODE_OFF};player?.repeatMode=repeat},{audioOnly=!audioOnly},{showNow=false})}
            else when(page){Page.HOME->HomePage(songs.filter{it.id !in hiddenIds},videos,counts,selected,lyrics,loadingLyrics,favourites,{playFrom(songs,it)},{t->favourites=store.toggleFavourite(t.id)},{t->selected=t;loadLyrics(t)},position)
                Page.MUSIC->VibeMusicLibrary(songs.filter{it.id !in hiddenIds},videos,favourites,playlists,selected,songs.filter{it.id !in hiddenIds && store.lyrics(LyricsClient.cacheKey(it))!=null}.map{it.id}.toSet(),{playFrom(if(it.isVideo)videos else songs,it)},{favourites=store.toggleFavourite(it.id)},{store.savePlaylist("My VIBE playlist",songs.map{it.id}.toSet());playlists=store.playlists();status="Created My VIBE playlist."},{showNow=true},{page=Page.PROFILE})
                Page.VIDEOS->LibraryPage("Video library",videos,Amber,favourites,{playFrom(videos,it)},{favourites=store.toggleFavourite(it.id)})
                Page.CONVERT->ConverterPage(videos,context){status=it}
                Page.PROFILE->VibeProfilePage(themeName,playerStyle,{name->themeName=name;store.saveSetting("theme",name);status="Theme applied."},{style->playerStyle=style;store.saveSetting("player_style",style);status="Player style selected."},{page=Page.FIX},{page=Page.HIDE},{petEnabled->petEnabled=petEnabled;store.saveSetting("music_pet",petEnabled.toString())},{status="Your VIBE settings are saved on this device."})
                Page.HIDE->VibeHideScanPage(songs,hiddenIds,{track->hiddenIds=store.toggleHiddenTrack(track.id)},{refresh();status="Library rescanned."})
                Page.FIX->VibeFixSongInfoPage(songs,metadataOverrides,{track,title,artist->store.saveMetadataOverride(track.id,title,artist);metadataOverrides=store.metadataOverrides();songs=songs.map{if(it.id==track.id)it.copy(title=title,artist=artist)else it};selected=selected?.let{if(it.id==track.id)it.copy(title=title,artist=artist)else it};status="Saved corrected song information."},{track->store.removeMetadataOverride(track.id);metadataOverrides=store.metadataOverrides();songs=songs.map{if(it.id==track.id)track else it};selected=selected?.let{if(it.id==track.id)track else it};status="Restored original song information."})
            }
            
            Text(status,color=Color(0xFFB8B0CE),fontSize=10.sp,modifier=Modifier.padding(horizontal=18.dp,vertical=3.dp))
            if(!showNow) selected?.let{MiniPlayer(it,playing,{showNow=true},{if(playing)player?.pause()else player?.play()},{player?.seekToNextMediaItem()})}
        }
    }
}

@Composable private fun HomePage(songs:List<Track>,videos:List<Track>,counts:Map<Long,Int>,selected:Track?,lyrics:String,loading:Boolean,favs:Set<Long>,onPlay:(Track)->Unit,onFav:(Track)->Unit,onLyrics:(Track)->Unit,positionMs:Long){
 val popular=songs.sortedByDescending{counts[it.id]?:0}.filter{(counts[it.id]?:0)>0}.take(4);val latest=songs.sortedByDescending{it.dateAdded}.take(5)
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=18.dp,vertical=4.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp)).background(Brush.linearGradient(listOf(Color(0xFF512DA8),Color(0xFF087EAA),Color(0xFFB83280)))).padding(20.dp)){Column{Text("YOUR SOUNDTRACK",color=Color.White.copy(alpha=.8f),fontSize=10.sp,letterSpacing=2.sp);Text("Feel every beat.",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Bold);Text("${songs.size} songs • ${videos.size} videos",color=Color.White.copy(alpha=.85f),fontSize=12.sp)}}};item{SectionHeading("Most played","TOP ROTATION")};if(popular.isEmpty())item{EmptyMessage("Play music to build your most-played list.")};items(popular){TrackRow(it,Rose,favs.contains(it.id),{onPlay(it)},{onFav(it)})};item{SectionHeading("Recently added","FRESH FINDS")};items(latest){TrackRow(it,Aqua,favs.contains(it.id),{onPlay(it)},{onFav(it)})};item{SectionHeading("Favourites","YOUR COLLECTION")};items(songs.filter{it.id in favs}.take(8)){TrackRow(it,Purple,true,{onPlay(it)},{onFav(it)})};item{SectionHeading("Lyrics finder","CACHED WHEN AVAILABLE")};item{Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).padding(14.dp)){Text(selected?.title?:"Play a song to look up lyrics",color=Color.White,fontWeight=FontWeight.SemiBold);Button(onClick={selected?.let(onLyrics)},enabled=selected!=null&&!loading,colors=ButtonDefaults.buttonColors(containerColor=Purple)){Text(if(loading)"Searching…" else "Find lyrics")};if(lyrics.isNotBlank()){val timed=remember(lyrics){LyricsClient.parseLrc(lyrics)};if(timed.isNotEmpty()){val active=timed.indexOfLast{it.timeMs<=positionMs}.coerceAtLeast(0);timed.subList((active-1).coerceAtLeast(0),(active+3).coerceAtMost(timed.size)).forEachIndexed{idx,line->Text(line.text,color=if((active-1).coerceAtLeast(0)+idx==active)Aqua else Color(0xFFB8B0CE),fontSize=if((active-1).coerceAtLeast(0)+idx==active)16.sp else 12.sp,fontWeight=if((active-1).coerceAtLeast(0)+idx==active)FontWeight.Bold else FontWeight.Normal,modifier=Modifier.padding(vertical=3.dp))}}else Text(lyrics,color=Color(0xFFDAD4EA),fontSize=12.sp)}}}}
}
@Composable private fun LibraryPage(title:String,tracks:List<Track>,accent:Color,favs:Set<Long>,onTrack:(Track)->Unit,onFav:(Track)->Unit){Column(Modifier.fillMaxSize()){Text(title,color=Color.White,fontSize=21.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=18.dp,vertical=8.dp));if(tracks.isEmpty())EmptyMessage("No media found. Allow access and refresh the library.");LazyColumn{items(tracks){TrackRow(it,accent,favs.contains(it.id),{onTrack(it)},{onFav(it)})}}}}

@Composable private fun NowPlaying(player:MediaController?,track:Track?,position:Long,duration:Long,playing:Boolean,shuffle:Boolean,repeat:Int,audioOnly:Boolean,seek:(Long)->Unit,previous:()->Unit,next:()->Unit,toggle:()->Unit,toggleShuffle:()->Unit,toggleRepeat:()->Unit,toggleAudio:()->Unit,close:()->Unit){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=22.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("NOW PLAYING",color=Aqua,fontWeight=FontWeight.Bold,letterSpacing=2.sp);TextButton(onClick=close){Text("Done",color=Rose)}}
 if(track?.isVideo==true&&!audioOnly&&player!=null){AndroidView(factory={ctx->PlayerView(ctx).apply{useController=true;this.player=player;resizeMode=androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT}},modifier=Modifier.fillMaxWidth().height(230.dp))}else Box(Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Purple,Color(0xFF174C72),Rose))),contentAlignment=Alignment.Center){Icon(if(track?.isVideo==true)Icons.Default.VideoLibrary else Icons.Default.Album,null,tint=Color.White,modifier=Modifier.size(100.dp))}
 Spacer(Modifier.height(18.dp));Text(track?.title?:"Nothing playing",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(track?.artist.orEmpty(),color=Color(0xFFB8B0CE));Spacer(Modifier.height(15.dp));Slider(value=if(duration>0)(position.toFloat()/duration).coerceIn(0f,1f) else 0f,onValueChange={seek((it*duration).toLong())},colors=SliderDefaults.colors(thumbColor=Aqua,activeTrackColor=Purple,inactiveTrackColor=Card));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(timeLabel(position),color=Color.LightGray,fontSize=11.sp);Text(timeLabel(duration),color=Color.LightGray,fontSize=11.sp)}
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){IconButton(onClick=toggleShuffle){Icon(Icons.Default.Shuffle,"Shuffle",tint=if(shuffle)Aqua else Color.LightGray)};IconButton(onClick=previous){Icon(Icons.Default.SkipPrevious,"Previous",tint=Color.White,modifier=Modifier.size(32.dp))};FilledIconButton(onClick=toggle,colors=IconButtonDefaults.filledIconButtonColors(containerColor=Purple),modifier=Modifier.size(62.dp)){Icon(if(playing)Icons.Default.Pause else Icons.Default.PlayArrow,"Play pause",modifier=Modifier.size(34.dp))};IconButton(onClick=next){Icon(Icons.Default.SkipNext,"Next",tint=Color.White,modifier=Modifier.size(32.dp))};IconButton(onClick=toggleRepeat){Icon(Icons.Default.Repeat,"Repeat",tint=if(repeat==Player.REPEAT_MODE_OFF)Color.LightGray else Aqua)}}
 if(track?.isVideo==true)Row(verticalAlignment=Alignment.CenterVertically){Text("Audio-only / background playback",color=Color.White,fontSize=12.sp);Switch(checked=audioOnly,onCheckedChange={toggleAudio()},colors=SwitchDefaults.colors(checkedThumbColor=Aqua))}
 Text("Equalizer & bass enhancement depend on device audio effects and are not enabled in this build.",color=Color(0xFFB8B0CE),fontSize=11.sp)
 }
}
internal fun timeLabel(ms:Long):String{val s=ms.coerceAtLeast(0)/1000;return "%d:%02d".format(s/60,s%60)}

@Composable
private fun ConverterPage(videos: List<Track>, context: android.content.Context, report: (String) -> Unit) {
    var working by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("Video to audio", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Offline AAC extraction • export to Music", color = Aqua, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Text("AAC audio is copied without quality loss. MP3 and other codecs require a tested encoder and are not yet supported.", color = Color(0xFFD8D1E8), fontSize = 12.sp, modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Card).padding(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            items(videos) { video ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Card).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VideoFile, null, tint = Amber); Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) { Text(video.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(video.durationLabel(), color = Color.LightGray, fontSize = 10.sp) }
                    TextButton(enabled = !working, onClick = {
                        working = true
                        scope.launch {
                            val converted = withContext(Dispatchers.IO) { runCatching { OfflineAudioExtractor.extractM4a(context, video.uri) } }
                            working = false
                            val message = converted.fold(
                                { file -> runCatching { exportToMusic(context, file) }.fold({ "Exported ${file.name} to Music/VIBE." }, { "Created ${file.name}, but export failed: ${it.message}" }) },
                                { "Conversion failed: ${it.message}" }
                            )
                            report(message)
                        }
                    }) { Text(if (working) "Working…" else "Convert + export", color = Aqua) }
                }
            }
            if (videos.isEmpty()) item { EmptyMessage("No videos found.") }
        }
    }
}
private fun exportToMusic(context:android.content.Context,file:File){require(Build.VERSION.SDK_INT>=29){"Music-folder export requires Android 10 or newer."};val values=ContentValues().apply{put(MediaStore.MediaColumns.DISPLAY_NAME,file.name);put(MediaStore.MediaColumns.MIME_TYPE,"audio/mp4");if(Build.VERSION.SDK_INT>=29)put(MediaStore.MediaColumns.RELATIVE_PATH,"Music/VIBE")};val uri=context.contentResolver.insert(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),values)?:error("Could not create Music folder file");try{context.contentResolver.openOutputStream(uri)?.use{out->file.inputStream().use{it.copyTo(out)}}?:error("Could not open exported file")}catch(e:Exception){context.contentResolver.delete(uri,null,null);throw e}}

@Composable private fun TrackRow(track:Track,accent:Color,favourite:Boolean,onClick:()->Unit,onFav:()->Unit){Row(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=3.dp).clip(RoundedCornerShape(15.dp)).background(Card).clickable(onClick=onClick).padding(11.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(accent,Purple))),contentAlignment=Alignment.Center){Icon(if(track.isVideo)Icons.Default.PlayCircle else Icons.Default.MusicNote,null,tint=Color.White)};Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(track.title,color=Color.White,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(track.artist,color=Color(0xFFB8B0CE),fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis)};IconButton(onClick=onFav){Icon(if(favourite)Icons.Default.Favorite else Icons.Default.FavoriteBorder,"Favourite",tint=if(favourite)Rose else Color.LightGray)};Text(track.durationLabel(),color=Color.LightGray,fontSize=10.sp);Icon(Icons.Default.PlayArrow,null,tint=Aqua)}}
@Composable private fun MiniPlayer(track:Track,playing:Boolean,open:()->Unit,toggle:()->Unit,next:()->Unit){Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=7.dp).clip(RoundedCornerShape(17.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF30204D),Color(0xFF173C53)))).clickable(onClick=open).padding(horizontal=12.dp,vertical=7.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.GraphicEq,null,tint=Aqua);Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(track.title,color=Color.White,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(track.artist,color=Color(0xFFB8B0CE),fontSize=10.sp)};IconButton(onClick=toggle){Icon(if(playing)Icons.Default.Pause else Icons.Default.PlayArrow,"Play/pause",tint=Color.White)};IconButton(onClick=next){Icon(Icons.Default.SkipNext,"Next",tint=Aqua)}}}
@Composable private fun SectionHeading(title:String,tag:String){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Bottom){Text(title,color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold);Text(tag,color=Purple,fontSize=9.sp,letterSpacing=1.sp)}}
@Composable private fun EmptyMessage(message:String){Text(message,color=Color(0xFFB8B0CE),fontSize=12.sp,modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Card).padding(14.dp))}
