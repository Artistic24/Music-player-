package com.vibe.music

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

private data class VibeTheme(val name:String,val colors:List<Color>,val subtitle:String)
private val vibeThemes=listOf(
 VibeTheme("Muso Style",listOf(Color(0xFFFF8A00),Color(0xFFFF168A),Color(0xFF7A0058),Color(0xFF08030A)),"Neon • Orange / magenta / black"),
 VibeTheme("Fiery Rhythms",listOf(Color(0xFFFF3D00),Color(0xFF7A001F),Color(0xFF160609),Color(0xFF050306)),"Flame • Red / ember / black"),
 VibeTheme("Radiant Nights",listOf(Color(0xFFFF27B8),Color(0xFF5420C8),Color(0xFF06112D),Color(0xFF05040C)),"Neon • Magenta / electric blue"),
 VibeTheme("Blue Ocean",listOf(Color(0xFF8DEBFF),Color(0xFF168DD1),Color(0xFF06345D),Color(0xFF061321)),"Cool • Ice blue / deep sea"),
 VibeTheme("Rainforest",listOf(Color(0xFF00D9A6),Color(0xFF087A70),Color(0xFF102A35),Color(0xFF050D13)),"Nature • Teal / midnight"),
 VibeTheme("Green Vibe",listOf(Color(0xFFB9FFD0),Color(0xFF35C878),Color(0xFF087C56),Color(0xFF07180F)),"Fresh • Mint / emerald")
)
private val vibeStyles=listOf("Top","Static","Dynamic","Spin Vinyl","Round")

@Composable internal fun VibeProfilePage(theme:String,style:String,onTheme:(String)->Unit,onStyle:(String)->Unit,onFix:()->Unit,onHideScan:()->Unit,petEnabled:Boolean,onPetToggle:(Boolean)->Unit,onSettings:()->Unit){
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=14.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFFFF7900),Color(0xFFEC008C),Color(0xFF3210A8)))).padding(18.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF160A24)),contentAlignment=Alignment.Center){Icon(Icons.Default.Album,null,tint=Color.White,modifier=Modifier.size(40.dp))};Spacer(Modifier.width(13.dp));Column{Text("VIBE",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Black);Text("Your music. Your world.",color=Color.White.copy(alpha=.85f),fontSize=12.sp)}}}
  item{Text("PLAYER STYLE",color=Color.White,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp)}
  item{
   LazyVerticalGrid(columns=GridCells.Fixed(3),modifier=Modifier.fillMaxWidth().height(226.dp),userScrollEnabled=false,verticalArrangement=Arrangement.spacedBy(10.dp),horizontalArrangement=Arrangement.spacedBy(9.dp)){
    items(vibeStyles){styleName->
     Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).border(if(style==styleName)2.dp else 1.dp,if(style==styleName)Color(0xFFFF168A) else Color(0xFF45313F),RoundedCornerShape(15.dp)).background(Color(0xFF180D17)).clickable{onStyle(styleName)}.padding(8.dp)){
      Box(Modifier.fillMaxWidth().height(88.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(Color(0xFFFF8A00),Color(0xFFFF168A),Color(0xFF3C0A38)))),contentAlignment=Alignment.Center){
       Box(Modifier.size(if(styleName=="Round")62.dp else 68.dp).clip(if(styleName=="Round")RoundedCornerShape(50) else RoundedCornerShape(17.dp)).background(Brush.radialGradient(listOf(Color(0xFFFFD4E8),Color(0xFFFF168A),Color(0xFF340A32)))),contentAlignment=Alignment.Center){Icon(Icons.Default.Album,null,tint=Color.White,modifier=Modifier.size(38.dp))}
       if(style==styleName)Icon(Icons.Default.CheckCircle,null,tint=Color.White,modifier=Modifier.align(Alignment.TopEnd).padding(5.dp).size(16.dp))
      }
      Text(styleName,color=Color.White,fontSize=10.sp,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.padding(top=6.dp))
      Text(if(style==styleName)"Selected" else "Preview style",color=Color(0xFFFF8BC2),fontSize=8.sp)
     }
    }
   }
  }
  item{Text("THEME GALLERY",color=Color.White,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp)}
  item{
   LazyVerticalGrid(columns=GridCells.Fixed(3),modifier=Modifier.fillMaxWidth().height(430.dp),userScrollEnabled=false,verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){
    items(vibeThemes){item->
     Column(Modifier.clickable{onTheme(item.name)}){
      Box(Modifier.fillMaxWidth().height(145.dp).clip(RoundedCornerShape(14.dp)).border(if(theme==item.name)2.dp else 1.dp,if(theme==item.name)Color.White else Color(0xFF55505E),RoundedCornerShape(14.dp)).background(Brush.verticalGradient(item.colors))){
       Column(Modifier.fillMaxSize().padding(7.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){Text("VIBE",color=item.colors.first(),fontWeight=FontWeight.Black,fontSize=10.sp);Spacer(Modifier.weight(1f));Icon(Icons.Default.Search,null,tint=Color.White.copy(alpha=.7f),modifier=Modifier.size(12.dp))}
        Text("All Songs   Playlist",color=Color.White,fontSize=6.sp)
        Spacer(Modifier.height(7.dp))
        repeat(4){idx->Row(Modifier.fillMaxWidth().padding(vertical=3.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(19.dp).clip(RoundedCornerShape(4.dp)).background(Brush.linearGradient(listOf(item.colors[(idx+1)%item.colors.size],item.colors[(idx+2)%item.colors.size]))));Spacer(Modifier.width(4.dp));Column{Box(Modifier.fillMaxWidth(.82f).height(3.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha=.85f)));Spacer(Modifier.height(3.dp));Box(Modifier.fillMaxWidth(.56f).height(2.dp).background(Color.White.copy(alpha=.45f)))}}}
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth().height(17.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha=.35f)),contentAlignment=Alignment.Center){Icon(Icons.Default.PlayCircleFilled,null,tint=item.colors.first(),modifier=Modifier.size(16.dp))}
       }
       if(theme==item.name)Icon(Icons.Default.CheckCircle,null,tint=Color(0xFFFF2384),modifier=Modifier.align(Alignment.BottomEnd).padding(5.dp))
      }
      Spacer(Modifier.height(5.dp));Text(item.name,color=Color.White,fontSize=11.sp,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(item.subtitle,color=Color.LightGray,fontSize=8.sp,maxLines=1)
     }
    }
   }
  }
  item{Text("MUSIC TOOLS",color=Color.White,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp);ProfileAction(Icons.Default.AutoFixHigh,"Fix song info","Correct titles and artists, with undo",onFix);ProfileAction(Icons.Default.VisibilityOff,"Hide & Scan","Hide selected tracks from your VIBE library",onHideScan)}
  item{Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFF211B36)).padding(13.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Pets,null,tint=Color(0xFFFF2D83),modifier=Modifier.size(26.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Music Pet",color=Color.White,fontWeight=FontWeight.SemiBold);Text("Let your headphone puppy dance to the beat",color=Color(0xFFB8B0CE),fontSize=10.sp)};Switch(checked=petEnabled,onCheckedChange=onPetToggle,colors=SwitchDefaults.colors(checkedThumbColor=Color(0xFFFF2D83)))}}
  item{ProfileAction(Icons.Default.Settings,"Settings","More playback and library settings",onSettings)}
 }
}
@Composable private fun ProfileAction(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,subtitle:String,click:()->Unit){Row(Modifier.fillMaxWidth().padding(vertical=4.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xFF211B36)).clickable(onClick=click).padding(13.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Color(0xFFFF2D83),modifier=Modifier.size(26.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(title,color=Color.White,fontWeight=FontWeight.SemiBold);Text(subtitle,color=Color(0xFFB8B0CE),fontSize=10.sp)};Icon(Icons.Default.ChevronRight,null,tint=Color.LightGray)}}
@Composable internal fun VibeFixSongInfoPage(tracks:List<Track>,overrides:Map<Long,Pair<String,String>>,onSave:(Track,String,String)->Unit,onUndo:(Track)->Unit){
 var editing by remember{mutableStateOf<Track?>(null)};var title by remember{mutableStateOf("")};var artist by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize()){Text("Fix song info",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(18.dp));Text("Correct the title and artist shown in VIBE. Original audio files remain unchanged.",color=Color(0xFFB8B0CE),fontSize=11.sp,modifier=Modifier.padding(horizontal=18.dp));LazyColumn(contentPadding=PaddingValues(12.dp)){items(tracks){track->val changed=overrides[track.id];Row(Modifier.fillMaxWidth().padding(vertical=4.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF211B36)).padding(11.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.MusicNote,null,tint=Color(0xFFFF5EA8));Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(changed?.first?:track.title,color=Color.White,maxLines=1,overflow=TextOverflow.Ellipsis);Text(changed?.second?:track.artist,color=Color(0xFFB8B0CE),fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis)};TextButton(onClick={title=changed?.first?:track.title;artist=changed?.second?:track.artist;editing=track}){Text("Edit",color=Color(0xFFFF5EA8))};if(changed!=null)IconButton(onClick={onUndo(track)}){Icon(Icons.Default.Undo,"Undo",tint=Color(0xFFFF5EA8))}}}}}
 editing?.let{track->AlertDialog(onDismissRequest={editing=null},title={Text("Edit song information")},text={Column(verticalArrangement=Arrangement.spacedBy(9.dp)){OutlinedTextField(value=title,onValueChange={title=it},label={Text("Song title")},singleLine=true);OutlinedTextField(value=artist,onValueChange={artist=it},label={Text("Artist")},singleLine=true)}},confirmButton={TextButton(onClick={if(title.isNotBlank()&&artist.isNotBlank())onSave(track,title.trim(),artist.trim());editing=null}){Text("Save")}},dismissButton={TextButton(onClick={editing=null}){Text("Cancel")}},containerColor=Color(0xFF211B36),titleContentColor=Color.White,textContentColor=Color.White)}
}


@Composable
internal fun VibeHideScanPage(tracks:List<Track>,hidden:Set<Long>,onToggle:(Track)->Unit,onRescan:()->Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Hide & Scan",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.Bold)
                Text("Hidden songs stay on your device; VIBE simply leaves them out of your library.",color=Color(0xFFB8B0CE),fontSize=11.sp)
            }
            Button(onClick=onRescan,colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFF2384))) { Text("Rescan") }
        }
        Text("${hidden.size} hidden • ${tracks.size-hidden.size} visible",color=Color(0xFFFF72B5),fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp))
        LazyColumn(verticalArrangement=Arrangement.spacedBy(5.dp)) {
            items(tracks,key={it.id}) { track ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color(0xFF211B36)).clickable{onToggle(track)}.padding(horizontal=12.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically) {
                    Icon(if(track.id in hidden)Icons.Default.VisibilityOff else Icons.Default.MusicNote,null,tint=Color(0xFFFF5EA8))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(track.title,color=Color.White,maxLines=1,overflow=TextOverflow.Ellipsis)
                        Text(track.artist,color=Color(0xFFB8B0CE),fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                    }
                    Switch(checked=track.id in hidden,onCheckedChange={onToggle(track)},colors=SwitchDefaults.colors(checkedThumbColor=Color(0xFFFF2384)))
                }
            }
        }
    }
}


@Composable
internal fun VibeSettingsPage(theme:String,style:String,petEnabled:Boolean,onPetToggle:(Boolean)->Unit,onBack:()->Unit){
 LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back",tint=Color.White)};Text("Settings",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.Bold)}}
  item{Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF211B36)).padding(15.dp)){Text("Appearance",color=Color.White,fontWeight=FontWeight.Bold);Spacer(Modifier.height(7.dp));Text("Theme: $theme",color=Color(0xFFFF8BC2));Text("Player style: $style",color=Color(0xFFB8B0CE),fontSize=12.sp);Text("Change these options in Me → Theme Gallery and Player Style.",color=Color(0xFFB8B0CE),fontSize=11.sp)}}
  item{Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF211B36)).padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Pets,null,tint=Color(0xFFFF2D83),modifier=Modifier.size(27.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Music Pet",color=Color.White,fontWeight=FontWeight.SemiBold);Text("Animate the headphone puppy while music plays.",color=Color(0xFFB8B0CE),fontSize=11.sp)};Switch(checked=petEnabled,onCheckedChange=onPetToggle)}}
  item{Text("Playback controls",color=Color.White,fontWeight=FontWeight.Bold);Text("Play, pause, seek, shuffle, repeat and sleep timer are available on the Now Playing screen.",color=Color(0xFFB8B0CE),fontSize=12.sp)}
  item{Text("Library",color=Color.White,fontWeight=FontWeight.Bold);Text("Use Hide & Scan to exclude tracks from VIBE without deleting files. Rescan reloads media from Android MediaStore.",color=Color(0xFFB8B0CE),fontSize=12.sp)}
 }
}
