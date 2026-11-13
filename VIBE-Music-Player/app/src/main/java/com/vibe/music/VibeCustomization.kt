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
 VibeTheme("Muso Style",listOf(Color(0xFFFF7900),Color(0xFFEC008C),Color(0xFF3210A8),Color(0xFF080812)),"Hot • Orange / pink / violet"),
 VibeTheme("Fiery Rhythms",listOf(Color(0xFFFF3D00),Color(0xFF7A001F),Color(0xFF160609),Color(0xFF050306)),"Flame • Red / ember / black"),
 VibeTheme("Radiant Nights",listOf(Color(0xFFFF27B8),Color(0xFF5420C8),Color(0xFF06112D),Color(0xFF05040C)),"Neon • Magenta / electric blue"),
 VibeTheme("Blue Ocean",listOf(Color(0xFF8DEBFF),Color(0xFF168DD1),Color(0xFF06345D),Color(0xFF061321)),"Cool • Ice blue / deep sea"),
 VibeTheme("Rainforest",listOf(Color(0xFF00D9A6),Color(0xFF087A70),Color(0xFF102A35),Color(0xFF050D13)),"Nature • Teal / midnight"),
 VibeTheme("Green Vibe",listOf(Color(0xFFB9FFD0),Color(0xFF35C878),Color(0xFF087C56),Color(0xFF07180F)),"Fresh • Mint / emerald")
)
private val vibeStyles=listOf("Top","Static","Dynamic","Spin Vinyl","Round")

@Composable internal fun VibeProfilePage(theme:String,style:String,onTheme:(String)->Unit,onStyle:(String)->Unit,onFix:()->Unit,onSettings:()->Unit){
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=14.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Brush.linearGradient(listOf(Color(0xFFFF7900),Color(0xFFEC008C),Color(0xFF3210A8)))).padding(18.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF160A24)),contentAlignment=Alignment.Center){Icon(Icons.Default.Album,null,tint=Color.White,modifier=Modifier.size(40.dp))};Spacer(Modifier.width(13.dp));Column{Text("VIBE",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Black);Text("Your music. Your world.",color=Color.White.copy(alpha=.85f),fontSize=12.sp)}}}
  item{Text("PLAYER STYLE",color=Color.White,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp)}
  item{Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){vibeStyles.forEach{item->FilterChip(selected=style==item,onClick={onStyle(item)},label={Text(item)},colors=FilterChipDefaults.filterChipColors(selectedContainerColor=Color(0xFFFF2384),selectedLabelColor=Color.White))}}}
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
  item{Text("MUSIC TOOLS",color=Color.White,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp);ProfileAction(Icons.Default.AutoFixHigh,"Fix song info","Correct titles and artists, with undo",onFix);ProfileAction(Icons.Default.Settings,"Settings","More playback and library settings",onSettings)}
 }
}
@Composable private fun ProfileAction(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,subtitle:String,click:()->Unit){Row(Modifier.fillMaxWidth().padding(vertical=4.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xFF211B36)).clickable(onClick=click).padding(13.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Color(0xFFFF2D83),modifier=Modifier.size(26.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(title,color=Color.White,fontWeight=FontWeight.SemiBold);Text(subtitle,color=Color(0xFFB8B0CE),fontSize=10.sp)};Icon(Icons.Default.ChevronRight,null,tint=Color.LightGray)}}
@Composable internal fun VibeFixSongInfoPage(tracks:List<Track>,overrides:Map<Long,Pair<String,String>>,onSave:(Track,String,String)->Unit,onUndo:(Track)->Unit){
 var editing by remember{mutableStateOf<Track?>(null)};var title by remember{mutableStateOf("")};var artist by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize()){Text("Fix song info",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(18.dp));Text("Correct the title and artist shown in VIBE. Original audio files remain unchanged.",color=Color(0xFFB8B0CE),fontSize=11.sp,modifier=Modifier.padding(horizontal=18.dp));LazyColumn(contentPadding=PaddingValues(12.dp)){items(tracks){track->val changed=overrides[track.id];Row(Modifier.fillMaxWidth().padding(vertical=4.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF211B36)).padding(11.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.MusicNote,null,tint=Color(0xFFFF5EA8));Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(changed?.first?:track.title,color=Color.White,maxLines=1,overflow=TextOverflow.Ellipsis);Text(changed?.second?:track.artist,color=Color(0xFFB8B0CE),fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis)};TextButton(onClick={title=changed?.first?:track.title;artist=changed?.second?:track.artist;editing=track}){Text("Edit",color=Color(0xFFFF5EA8))};if(changed!=null)IconButton(onClick={onUndo(track)}){Icon(Icons.Default.Undo,"Undo",tint=Color(0xFFFF5EA8))}}}}}
 editing?.let{track->AlertDialog(onDismissRequest={editing=null},title={Text("Edit song information")},text={Column(verticalArrangement=Arrangement.spacedBy(9.dp)){OutlinedTextField(value=title,onValueChange={title=it},label={Text("Song title")},singleLine=true);OutlinedTextField(value=artist,onValueChange={artist=it},label={Text("Artist")},singleLine=true)}},confirmButton={TextButton(onClick={if(title.isNotBlank()&&artist.isNotBlank())onSave(track,title.trim(),artist.trim());editing=null}){Text("Save")}},dismissButton={TextButton(onClick={editing=null}){Text("Cancel")}},containerColor=Color(0xFF211B36),titleContentColor=Color.White,textContentColor=Color.White)}
}
