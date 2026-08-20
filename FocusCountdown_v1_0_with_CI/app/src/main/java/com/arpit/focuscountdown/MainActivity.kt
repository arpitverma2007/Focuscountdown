package com.arpit.focuscountdown

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arpit.focuscountdown.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

private val Cream = androidx.compose.ui.graphics.Color(0xFFF7F4EC)
private val Card = androidx.compose.ui.graphics.Color(0xFFFFFCF5)
private val Green = androidx.compose.ui.graphics.Color(0xFF315C4C)
private val Terracotta = androidx.compose.ui.graphics.Color(0xFFC47A52)

class MainActivity : ComponentActivity() {
    private lateinit var store: AppStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = AppStore(this)
        setContent { FocusApp(store) }
    }
}

@Composable
fun FocusApp(store: AppStore) {
    var tab by remember { mutableIntStateOf(0) }
    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var goals by remember { mutableStateOf(store.goals()) }
    var journal by remember { mutableStateOf(store.journal()) }
    var progress by remember { mutableStateOf(store.progress()) }

    LaunchedEffect(Unit) {
        while (true) { tick = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) }
    }

    val colors = lightColorScheme(background = Cream, surface = Card, primary = Green, secondary = Terracotta)
    MaterialTheme(colorScheme = colors) {
        Scaffold(
            containerColor = Cream,
            bottomBar = {
                NavigationBar(containerColor = Card) {
                    listOf("Home", "Goals", "Journey", "Progress", "Settings").forEachIndexed { i, name ->
                        NavigationBarItem(selected = tab == i, onClick = { tab = i },
                            icon = { Text(listOf("⌂","◷","✎","↗","⚙")[i], fontSize = 20.sp) },
                            label = { Text(name) })
                    }
                }
            }
        ) { pad ->
            when(tab) {
                0 -> HomeScreen(store, goals, progress, tick, Modifier.padding(pad), { tab = 2 })
                1 -> GoalsScreen(store, goals, { goals = store.goals() })
                2 -> JourneyScreen(store, journal, { journal = store.journal() })
                3 -> ProgressScreen(progress, journal)
                else -> SettingsScreen()
            }
        }
    }
}

@Composable
fun Header(title: String, subtitle: String? = null) {
    Column {
        Text(title.uppercase(), fontSize = 11.sp, letterSpacing = 2.sp, color = Green)
        Text(subtitle ?: "", fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HomeScreen(store: AppStore, goals: List<Goal>, progress: Map<String, DailyProgress>, now: Long, modifier: Modifier, onJourney: () -> Unit) {
    var refresh by remember { mutableIntStateOf(0) }
    val active = goals.firstOrNull { it.id == store.mainGoalId() && !it.archived } ?: goals.firstOrNull { !it.archived }
    val remaining = active?.let { max(0L, it.targetMillis - now) } ?: 0L
    val total = active?.let { max(1L, it.targetMillis - it.startMillis) } ?: 1L
    val done = (1f - remaining.toFloat()/total.toFloat()).coerceIn(0f,1f)
    val days = remaining / 86_400_000L
    val hours = (remaining / 3_600_000L) % 24
    val mins = (remaining / 60_000L) % 60
    val secs = (remaining / 1000L) % 60
    val today = progress[store.todayKey()]
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Header("Focus", active?.title ?: "Your next chapter")
            Spacer(Modifier.height(5.dp))
            Text(if(active == null) "Create a countdown to begin." else "See your time. Use it well. Remember the journey.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha=.65f))
        }
        item {
            Card(shape = RoundedCornerShape(30.dp)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(active?.icon ?: "🌱", fontSize = 28.sp)
                    Text("TIME LEFT", fontSize = 11.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(5.dp))
                    Text(if(active == null) "—" else "$days DAYS", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Green)
                    Text(String.format("%02d : %02d : %02d", hours, mins, secs), fontSize = 24.sp)
                    Spacer(Modifier.height(18.dp))
                    LinearProgressIndicator({ done }, Modifier.fillMaxWidth(), color = Green)
                    Spacer(Modifier.height(7.dp))
                    Text("${(done*100).toInt()}% of your countdown completed", fontSize = 12.sp)
                    active?.let { Text(SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(it.targetMillis)), fontSize = 12.sp) }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("TODAY", fontSize = 11.sp, letterSpacing = 2.sp, color = Green)
                    Text(if((today?.studyHours ?: 0f) > 0) "You studied ${today!!.studyHours} hours today." else "Today is still unwritten.", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("Every small step counts.", color = Terracotta)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick=onJourney) { Text("Add today's progress") }
                }
            }
        }
        item {
            Text("“${listOf("You still have time.","One day at a time.","Small progress is still progress.","Keep going — today counts.","Your future self will thank you.")[((now/86_400_000)%5).toInt()]}”",
                fontSize=18.sp, fontWeight=FontWeight.Medium, color=Green)
        }
    }
}

@Composable
fun GoalsScreen(store: AppStore, goals: List<Goal>, refresh: () -> Unit) {
    var show by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
            Header("Goals", "Your countdowns")
            Button(onClick={show=true}) { Text("+ Add") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) {
            items(goals.filter{!it.archived}) { g ->
                Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(22.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically) {
                        Text(g.icon, fontSize=28.sp); Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(g.title, fontWeight=FontWeight.Bold, fontSize=18.sp)
                            Text(SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(Date(g.targetMillis)), fontSize=12.sp)
                        }
                        if(store.mainGoalId()==g.id) Text("MAIN", color=Green, fontSize=10.sp)
                        else TextButton(onClick={store.setMainGoal(g.id); refresh()}) { Text("Set main") }
                    }
                }
            }
        }
    }
    if(show) GoalDialog(store, {show=false; refresh()})
}

@Composable
fun GoalDialog(store: AppStore, close: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("🎯") }
    var target by remember { mutableLongStateOf(Calendar.getInstance().apply{add(Calendar.MONTH,1)}.timeInMillis) }
    val context = androidx.compose.ui.platform.LocalContext.current
    AlertDialog(onDismissRequest=close, title={Text("Create countdown")}, text={
        Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(title,{title=it},label={Text("Goal / exam name")},singleLine=true)
            OutlinedTextField(icon,{icon=it},label={Text("Icon")},singleLine=true)
            Text("Target: ${SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(Date(target))}")
            Button(onClick={
                val c=Calendar.getInstance().apply{timeInMillis=target}
                DatePickerDialog(context,{_,y,m,d->
                    c.set(y,m,d)
                    TimePickerDialog(context,{_,h,min->c.set(Calendar.HOUR_OF_DAY,h);c.set(Calendar.MINUTE,min);target=c.timeInMillis},
                        c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),false).show()
                },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()
            }) { Text("Choose date & time") }
        }
    }, confirmButton={
        Button(enabled=title.isNotBlank(),onClick={
            val now=System.currentTimeMillis()
            val g=Goal(System.currentTimeMillis(),title,now,target,icon.ifBlank{"🎯"})
            val list=store.goals(); list.add(g); store.saveGoals(list)
            if(store.mainGoalId()==-1L) store.setMainGoal(g.id)
            close()
        }) { Text("Create") }
    }, dismissButton={TextButton(onClick=close){Text("Cancel")}})
}

@Composable
fun JourneyScreen(store: AppStore, journal: List<JournalEntry>, refresh: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("🙂") }
    var achievement by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Header("My Journey", "Remember the days")
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(text,{text=it},Modifier.fillMaxWidth(),minLines=4,label={Text("What happened today?")},placeholder={Text("Progress, thoughts, wins, lessons...")})
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(hours,{hours=it},Modifier.weight(1f),label={Text("Study hours")})
            OutlinedTextField(mood,{mood=it},Modifier.weight(1f),label={Text("Mood")})
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(achievement,{achievement=it},Modifier.fillMaxWidth(),label={Text("Today's achievement")})
        Spacer(Modifier.height(8.dp))
        Button(enabled=text.isNotBlank(),onClick={
            val list=store.journal().toMutableList()
            list.add(0,JournalEntry(System.currentTimeMillis(),System.currentTimeMillis(),text,hours.toFloatOrNull()?:0f,mood,achievement))
            store.saveJournal(list); text="";hours="";achievement="";refresh()
        }) { Text("Save today's entry") }
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) {
            items(journal) { e ->
                Card(shape=RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(Date(e.dateMillis))}  ${e.mood}",color=Green,fontSize=12.sp)
                        Text(e.text,fontSize=16.sp)
                        if(e.studyHours>0) Text("📚 ${e.studyHours} hours",fontSize=12.sp)
                        if(e.achievement.isNotBlank()) Text("✦ ${e.achievement}",color=Terracotta,fontSize=13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(progress: Map<String, DailyProgress>, journal: List<JournalEntry>) {
    val totalHours=journal.sumOf{it.studyHours.toDouble()}.toFloat()
    val tasks=progress.values.sumOf{it.tasks}
    val studiedDays=progress.values.count{it.studyHours>0}
    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Header("Progress", "Your effort, visible")
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            StatCard("STUDY", "${"%.1f".format(totalHours)}h", Modifier.weight(1f))
            StatCard("DAYS", "$studiedDays", Modifier.weight(1f))
            StatCard("TASKS", "$tasks", Modifier.weight(1f))
        }
        Spacer(Modifier.height(18.dp))
        Card(shape=RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Progress calendar",fontWeight=FontWeight.Bold,fontSize=20.sp)
                Spacer(Modifier.height(12.dp))
                Text("Days with study activity are recorded here. More detailed calendar heatmaps can be added without changing your stored data.")
                Spacer(Modifier.height(14.dp))
                progress.toSortedMap().entries.takeLast(14).forEach {
                    Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.SpaceBetween) {
                        Text(it.key); Text("${it.value.studyHours}h  •  ${it.value.tasks} tasks",color=Green)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Keep building. Consistency beats intensity.",color=Terracotta,fontSize=16.sp)
    }
}

@Composable
fun StatCard(label:String,value:String,modifier:Modifier) {
    Card(modifier,shape=RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp)) {
        Text(label,fontSize=9.sp,letterSpacing=1.5.sp,color=Green)
        Text(value,fontSize=24.sp,fontWeight=FontWeight.Bold)
    }}
}

@Composable
fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Header("Settings","Make it yours")
        Spacer(Modifier.height(16.dp))
        Card(shape=RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("Focus Countdown v1.0",fontWeight=FontWeight.Bold,fontSize=20.sp)
                Text("A calm place to see your time, track your progress and remember your journey.")
                Text("• Works offline\n• Local data storage\n• Home-screen widget\n• Exact countdown dates\n• Multiple goals")
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Tip: add the main countdown widget to your home screen so your goal stays visible without opening the app.",color=Terracotta)
    }
}
