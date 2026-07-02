package com.example

import android.content.Context
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import com.example.api.GeminiClient
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ElectricYellow
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.SecondaryNeon
import com.example.ui.theme.TertiaryNeon
import com.example.ui.viewmodel.LifeOSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val vm: LifeOSViewModel = viewModel()
                LifeOSMainScreen(vm)
            }
        }
    }
}

@Composable
fun LifeOSMainScreen(vm: LifeOSViewModel) {
    val currentScreen by vm.currentScreen.collectAsStateWithLifecycle()
    val isAssistantOpen by vm.isAssistantOpen.collectAsStateWithLifecycle()
    val xpNotification by vm.xpNotification.collectAsStateWithLifecycle()
    val profile by vm.userProfile.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Dismiss notification helper
    LaunchedEffect(xpNotification) {
        if (xpNotification != null) {
            delay(4000)
            vm.dismissNotification()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Futuristic background glowing gradient circles matching Bento Grid colors
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6366F1).copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.2f),
                    radius = size.width * 0.5f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.7f),
                    radius = size.width * 0.6f
                )
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                LifeOSBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { vm.currentScreen.value = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main screen navigation routing
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        "dashboard" -> DashboardScreen(vm)
                        "habits" -> HabitsScreen(vm)
                        "finance" -> FinanceScreen(vm)
                        "health" -> HealthScreen(vm)
                        "memories" -> MemoriesScreen(vm)
                        "learning" -> LearningScreen(vm)
                        "goals" -> GoalsScreen(vm)
                        "map" -> MapScreen(vm)
                        "profile" -> ProfileScreen(vm)
                        else -> DashboardScreen(vm)
                    }
                }

                // Global float assistant trigger button
                FloatingAssistantButton(
                    onClick = { vm.isAssistantOpen.value = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )

                // Global Toast/XP Notification overlay
                xpNotification?.let { msg ->
                    XpNotificationToast(
                        message = msg,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                    )
                }
            }
        }

        // Assistant Sliding Sheet Dialog Overlay
        if (isAssistantOpen) {
            AiAssistantOverlay(
                vm = vm,
                onDismiss = { vm.isAssistantOpen.value = false }
            )
        }
    }
}

// --- NAVIGATION BAR ---
@Composable
fun LifeOSBottomBar(currentScreen: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF0F1115),
        tonalElevation = 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        val items = listOf(
            NavigationItem("dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "Home"),
            NavigationItem("habits", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "Habits"),
            NavigationItem("finance", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "Finance"),
            NavigationItem("health", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "Health"),
            NavigationItem("memories", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary, "Vault"),
            NavigationItem("profile", Icons.Filled.Person, Icons.Outlined.Person, "LifeOS")
        )

        items.forEach { item ->
            val isSelected = currentScreen == item.id
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.id) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_${item.id}")
            )
        }
    }
}

data class NavigationItem(
    val id: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val label: String
)

// --- GLOBAL XP TOAST NOTIFICATION ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun XpNotificationToast(message: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("xp_toast")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(symmetricPadding())
        ) {
            Icon(
                imageVector = Icons.Filled.OfflineBolt,
                contentDescription = "XP Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            )
        }
    }
}

// --- FLOATING ASSISTANT BUTTON ---
@Composable
fun FloatingAssistantButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.Black,
        shape = CircleShape,
        modifier = modifier
            .size(60.dp)
            .scale(pulseScale)
            .testTag("floating_ai_button"),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = "AI Assistant Open",
            modifier = Modifier.size(28.dp),
            tint = Color.Black
        )
    }
}

@Composable
fun BentoBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.25f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(Locale.getDefault()),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

// --- HELPER SPACING CONSTANTS ---
fun symmetricPadding() = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

// --- DASHBOARD / HOME SCREEN ---
@Composable
fun UpcomingEventsWidget(timelineItems: List<TimelineItem>, vm: LifeOSViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().testTag("widget_events")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f), CircleShape)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Event, "Events Icon", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        BentoBadge(text = "TIMELINE AGENDA", color = MaterialTheme.colorScheme.secondary)
                        Text("Upcoming Events", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (timelineItems.isEmpty()) {
                Text(
                    text = "No events logged for today. Tap Operating Hubs to schedule some.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    timelineItems.take(3).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.timeString,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.width(65.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    if (item.description.isNotBlank()) {
                                        Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitStreaksWidget(habits: List<Habit>, vm: LifeOSViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().testTag("widget_habits")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ElectricYellow.copy(alpha = 0.10f), CircleShape)
                            .border(BorderStroke(1.dp, ElectricYellow.copy(alpha = 0.25f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.LocalFireDepartment, "Streak Icon", tint = ElectricYellow, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        BentoBadge(text = "HABITS CORE", color = ElectricYellow)
                        Text("Active Habit Streaks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val ongoingHabits = habits.filter { it.streak > 0 }
            if (ongoingHabits.isEmpty()) {
                Text(
                    text = "No active habit streaks. Complete habits daily to build streaks!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ongoingHabits.forEach { habit ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (habit.isCompletedToday) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = "Status",
                                    tint = if (habit.isCompletedToday) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(habit.name, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalFireDepartment, "Streak Fire", tint = ElectricYellow, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${habit.streak} days", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricYellow)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyXpWidget(profile: UserProfile, vm: LifeOSViewModel) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().testTag("widget_xp")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MilitaryTech, "Level Badge", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        BentoBadge(text = "OS ENGINE STATUS", color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Level ${profile.level}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
                
                Text("${profile.xp % 100}/100 XP", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            val progressFraction = (profile.xp % 100).toFloat() / 100f
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.05f)
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Completed habits, study hubs, goals and finance updates build XP. Reach level 100 to fully synchronize LifeOS.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun WaterIntakeWidget(profile: UserProfile, vm: LifeOSViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().testTag("widget_water")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryNeon.copy(alpha = 0.10f), CircleShape)
                            .border(BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.25f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.LocalDrink, "Water Icon", tint = PrimaryNeon, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        BentoBadge(text = "HYDRATION CORE", color = PrimaryNeon)
                        Text("Water Intake", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                
                Text("${profile.waterIntakeToday} / 2500 ml", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryNeon)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val waterProgress = minOf(1.0f, profile.waterIntakeToday.toFloat() / 2500f)
            LinearProgressIndicator(
                progress = { waterProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryNeon,
                trackColor = Color.White.copy(alpha = 0.05f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { vm.addWater(250) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon.copy(alpha = 0.15f), contentColor = PrimaryNeon),
                    modifier = Modifier.weight(1f).testTag("add_water_250_btn"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("+250ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { vm.addWater(500) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon.copy(alpha = 0.15f), contentColor = PrimaryNeon),
                    modifier = Modifier.weight(1f).testTag("add_water_500_btn"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("+500ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MoneySavedWidget(profile: UserProfile, vm: LifeOSViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().testTag("widget_money")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SecondaryNeon.copy(alpha = 0.10f), CircleShape)
                            .border(BorderStroke(1.dp, SecondaryNeon.copy(alpha = 0.25f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.AccountBalanceWallet, "Savings Icon", tint = SecondaryNeon, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        BentoBadge(text = "VAULT RESERVES", color = SecondaryNeon)
                        Text("Money Saved", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                
                Text("$${String.format(Locale.getDefault(), "%.2f", profile.moneySaved)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = EmeraldGreen)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Keep logging incomes and tracking smart budgets to grow your offshore reserve vault balance.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 14.sp
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Button(
                onClick = { vm.currentScreen.value = "finance" },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryNeon.copy(alpha = 0.15f), contentColor = SecondaryNeon),
                modifier = Modifier.align(Alignment.End).testTag("go_to_savings_btn"),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Open Finance Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingTourOverlay(profile: UserProfile, vm: LifeOSViewModel) {
    var currentStep by remember { mutableStateOf(0) }
    
    // Step-specific simulated state values for interactive examples!
    var aiSimulatorPromptSelected by remember { mutableStateOf<String?>(null) }
    var aiSimulatorResponse by remember { mutableStateOf("") }
    var aiSimulatorTyping by remember { mutableStateOf(false) }
    
    var simulatedXp by remember { mutableStateOf(0) }
    var simulatedLevel by remember { mutableStateOf(1) }
    
    val simulatedTimelineEvents = remember { mutableStateListOf<String>() }
    var simulatedNewEventName by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    
    // Reset temporary variables on step change
    LaunchedEffect(currentStep) {
        aiSimulatorPromptSelected = null
        aiSimulatorResponse = ""
        aiSimulatorTyping = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .clickable(enabled = false) {} // block click propagation
            .padding(20.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14171E)),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(PrimaryNeon, SecondaryNeon))),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("onboarding_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Step indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LifeOS COGNITIVE SYNC",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryNeon,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "${currentStep + 1} / 5",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // STEP CONTENT
                when (currentStep) {
                    0 -> {
                        Text(
                            text = "WELCOME TO LifeOS",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.85f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    brush = Brush.sweepGradient(listOf(PrimaryNeon, SecondaryNeon, TertiaryNeon, PrimaryNeon)),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
                                    alpha = 0.35f
                                )
                                drawCircle(
                                    color = PrimaryNeon.copy(alpha = 0.15f),
                                    radius = size.width / 2 * pulseScale
                                )
                            }
                            Icon(Icons.Filled.SettingsSuggest, "OS Core", tint = PrimaryNeon, modifier = Modifier.size(48.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "LifeOS is a unified gamified environment designed to coordinate your entire lifestyle. Tap into habits, track finances, log fitness diagnostics, query memories, and level up your life through direct milestones.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                    
                    1 -> {
                        Text(
                            text = "CUSTOM BENTO SYSTEM",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Interact below to test widget prioritization:",
                            fontSize = 11.sp,
                            color = PrimaryNeon,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var miniWidgetsOrder by remember { mutableStateOf(listOf("Daily XP", "Water Intake", "Money Saved")) }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            miniWidgetsOrder.forEachIndexed { idx, name ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    if (idx > 0) {
                                                        val newList = miniWidgetsOrder.toMutableList()
                                                        val tmp = newList[idx]
                                                        newList[idx] = newList[idx - 1]
                                                        newList[idx - 1] = tmp
                                                        miniWidgetsOrder = newList
                                                    }
                                                },
                                                enabled = idx > 0,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Filled.ArrowUpward, "Up", tint = if (idx > 0) Color.White else Color.Gray, modifier = Modifier.size(14.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (idx < miniWidgetsOrder.size - 1) {
                                                        val newList = miniWidgetsOrder.toMutableList()
                                                        val tmp = newList[idx]
                                                        newList[idx] = newList[idx + 1]
                                                        newList[idx + 1] = tmp
                                                        miniWidgetsOrder = newList
                                                    }
                                                },
                                                enabled = idx < miniWidgetsOrder.size - 1,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Filled.ArrowDownward, "Down", tint = if (idx < miniWidgetsOrder.size - 1) Color.White else Color.Gray, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Optimize your screen focus! Add, delete, and re-order widgets instantly via the custom Bento system to fit your workflow.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                    
                    2 -> {
                        Text(
                            text = "AI CORE INTELLIGENCE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap a simulated query to see Gemini compile life advice:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val queries = listOf(
                            "Optimize my steps today",
                            "Analyze my budget limits"
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            queries.forEach { q ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                                        .clickable {
                                            aiSimulatorPromptSelected = q
                                            aiSimulatorTyping = true
                                            aiSimulatorResponse = "Thinking..."
                                            scope.launch {
                                                kotlinx.coroutines.delay(600)
                                                aiSimulatorResponse = when (q) {
                                                    "Optimize my steps today" -> "🏃 LifeOS intelligence recommends adding a 15-minute quick cardio log. Take water now. Current stats are +150 XP pending!"
                                                    "Analyze my budget limits" -> "💳 Your cash reserve is $345.50. You saved $650 today from freelance UI. Recommend keeping coffee spending below $10."
                                                    else -> ""
                                                }
                                                aiSimulatorTyping = false
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(q, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                        
                        if (aiSimulatorPromptSelected != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("PROMPT: $aiSimulatorPromptSelected", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(aiSimulatorResponse, fontSize = 12.sp, color = Color.White, lineHeight = 16.sp)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Query your Gemini Core using natural language. It references your health records, memory items, and goals to provide exact guidance.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                    
                    3 -> {
                        Text(
                            text = "GAMIFIED XP ENGINE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Simulate completing a habit to gain XP:",
                            fontSize = 11.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                simulatedXp += 25
                                if (simulatedXp >= 100) {
                                    simulatedLevel += 1
                                    simulatedXp = 0
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Simulate +25 XP Claim", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Level $simulatedLevel", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                    Text("$simulatedXp / 100 XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val simulatedFrac = simulatedXp.toFloat() / 100f
                                LinearProgressIndicator(
                                    progress = { simulatedFrac },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = EmeraldGreen,
                                    trackColor = Color.White.copy(alpha = 0.05f)
                                )
                                if (simulatedLevel > 1) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("⚡ LEVEL UP GAINED!", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Every habit toggle, learning hub sprint, money savings goal, and health update yields XP points, motivating you to upgrade your life level.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                    
                    4 -> {
                        Text(
                            text = "DAILY TIMELINE LOG",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Log a sample routine timeline event:",
                            fontSize = 11.sp,
                            color = TertiaryNeon,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = simulatedNewEventName,
                                onValueChange = { simulatedNewEventName = it },
                                placeholder = { Text("e.g. Read Book", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontSize = 11.sp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (simulatedNewEventName.isNotBlank()) {
                                        simulatedTimelineEvents.add(simulatedNewEventName)
                                        simulatedNewEventName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TertiaryNeon),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add Log", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        if (simulatedTimelineEvents.isEmpty()) {
                            Text("Type and click 'Add Log' above to try it out!", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            ) {
                                simulatedTimelineEvents.take(2).forEach { ev ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).background(TertiaryNeon, CircleShape))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("10:00 AM - $ev", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Map out your day sequentially. Set schedule blocks in Operating Hubs or log quick events directly to build a permanent offline archive.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // BACK / NEXT NAVIGATION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        TextButton(
                            onClick = { currentStep -= 1 },
                            modifier = Modifier.testTag("onboarding_back_btn")
                        ) {
                            Text("BACK", color = Color.White.copy(alpha = 0.6f))
                        }
                    } else {
                        TextButton(
                            onClick = { vm.completeOnboarding() },
                            modifier = Modifier.testTag("onboarding_skip_btn")
                        ) {
                            Text("SKIP TOUR", color = Color.White.copy(alpha = 0.4f))
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (currentStep < 4) {
                                currentStep += 1
                            } else {
                                vm.completeOnboarding()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("onboarding_next_btn")
                    ) {
                        Text(
                            text = if (currentStep < 4) "NEXT" else "SYNCHRONIZE NOW",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(vm: LifeOSViewModel) {
    val profile by vm.userProfile.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val timeline by vm.timelineItems.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    var aiRecText by remember { mutableStateOf("Tap the generate button below to get real-time AI recommendations based on your habits.") }
    var isRecGenerating by remember { mutableStateOf(false) }

    // Quick system info values
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    val clockTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    val dateText = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date())

    // Emergency mode dialog trigger
    var showEmergencyDialog by remember { mutableStateOf(false) }
    // Voice Mode simulator
    var showVoiceDialog by remember { mutableStateOf(false) }
    
    // Customize Widgets state
    var showCustomizeDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Dashboard Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GOOD MORNING, ALEX",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = "LifeOS",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.White
                        )
                        Text(
                            text = dateText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Emergency SOS Button
                    Button(
                        onClick = { showEmergencyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("sos_dashboard_button")
                    ) {
                        Icon(Icons.Filled.Sos, contentDescription = "SOS", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // ACTIONS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Customize Widgets Button
                    OutlinedButton(
                        onClick = { showCustomizeDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f).testTag("customize_widgets_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.DashboardCustomize, "Customize", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Customize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Interactive Tour Button
                    OutlinedButton(
                        onClick = { vm.resetOnboarding() },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f).testTag("tour_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Help, "Tour", tint = ElectricYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guided Tour", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // VITALS PANEL (STEPS, MOOD)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Steps Panel
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            BentoBadge(text = "HEALTH", color = EmeraldGreen)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${profile.stepsToday}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Steps Tracked", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }

                    // Mood Panel
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            BentoBadge(text = "MOOD", color = ElectricYellow)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = profile.moodToday,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Self Assessment", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // DYNAMIC COMPOSABLE BENTO WIDGETS
            val activeWidgets = profile.widgetsOrderCsv.split(",").filter { it.isNotBlank() }
            activeWidgets.forEach { widgetId ->
                item(key = widgetId) {
                    when (widgetId) {
                        "events" -> UpcomingEventsWidget(timeline, vm)
                        "habits" -> HabitStreaksWidget(habits, vm)
                        "xp" -> DailyXpWidget(profile, vm)
                        "water" -> WaterIntakeWidget(profile, vm)
                        "money" -> MoneySavedWidget(profile, vm)
                    }
                }
            }

            // WEATHER & GENERAL TELEMETRY
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudQueue, "Weather", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Cyber Skies", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("72°F / Light Fog", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.BatteryChargingFull, "Battery", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$batteryLevel% Energy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccessTime, "Clock", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(clockTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }

            // CORE AI RECOMMENDATION CARD (INTEGRATED GEMINI PROMPTER)
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                    border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "AI recommend",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LifeOS AI Recommendation",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            if (isRecGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = aiRecText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                isRecGenerating = true
                                vm.getAiQuickRecommendation { result ->
                                    aiRecText = result
                                    isRecGenerating = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.align(Alignment.End).testTag("generate_ai_rec_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Synchronize AI", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // DAILY TIMELINE COLLAPSIBLE CARD
            item {
                var isTimelineExpanded by remember { mutableStateOf(false) }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Timeline, "Timeline Icon", tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Daily Activity Timeline", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            IconButton(onClick = { isTimelineExpanded = !isTimelineExpanded }) {
                                Icon(
                                    imageVector = if (isTimelineExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = "Expand Timeline"
                                )
                            }
                        }

                        if (isTimelineExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            if (timeline.isEmpty()) {
                                Text("No timeline logs logged. Set up events in your day.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    timeline.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.timeString,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.width(65.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            
                                            IconButton(
                                                onClick = { vm.deleteTimelineEvent(item) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Filled.Close, "Delete log", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            // Inline quick add timeline
                            var quickTitle by remember { mutableStateOf("") }
                            var quickDesc by remember { mutableStateOf("") }
                            var quickTime by remember { mutableStateOf("") }
                            
                            OutlinedTextField(
                                value = quickTitle,
                                onValueChange = { quickTitle = it },
                                placeholder = { Text("Event title (e.g. Coding)", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 12.sp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = quickDesc,
                                    onValueChange = { quickDesc = it },
                                    placeholder = { Text("Details", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 12.sp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = quickTime,
                                    onValueChange = { quickTime = it },
                                    placeholder = { Text("08:00 PM", fontSize = 12.sp) },
                                    modifier = Modifier.width(90.dp),
                                    textStyle = TextStyle(fontSize = 12.sp),
                                    singleLine = true
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (quickTitle.isNotBlank() && quickTime.isNotBlank()) {
                                        vm.addTimelineEvent(quickTitle, quickDesc, quickTime, "Routine")
                                        quickTitle = ""
                                        quickDesc = ""
                                        quickTime = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("add_timeline_event_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Log Life Event", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // OTHER SECTIONS LINKS / TILES GRID
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Operating Hubs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HubTile("Learning Center", Icons.Filled.School, MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                            vm.currentScreen.value = "learning"
                        }
                        HubTile("Goal Tracker", Icons.Filled.TrendingUp, MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) {
                            vm.currentScreen.value = "goals"
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HubTile("Life Memory Map", Icons.Filled.Map, EmeraldGreen, Modifier.weight(1f)) {
                            vm.currentScreen.value = "map"
                        }
                        HubTile("Voice AI Core", Icons.Filled.Mic, ElectricYellow, Modifier.weight(1f)) {
                            showVoiceDialog = true
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // ONBOARDING TOUR OVERLAY
        if (!profile.onboardingCompleted) {
            OnboardingTourOverlay(profile = profile, vm = vm)
        }
    }

    // WIDGETS CUSTOMIZATION ALERTDIALOG
    if (showCustomizeDialog) {
        val defaultAllWidgets = listOf("events", "habits", "xp", "water", "money")
        val currentActive = profile.widgetsOrderCsv.split(",").filter { it.isNotBlank() }
        val currentInactive = defaultAllWidgets.filter { !currentActive.contains(it) }

        AlertDialog(
            onDismissRequest = { showCustomizeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DashboardCustomize, "Customize Dashboard", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Customize Bento Grid", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Toggle visibility and tap arrows to reorder widgets on your home screen.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // ACTIVE WIDGETS SECTION
                    Text(
                        text = "ACTIVE WORKSPACE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (currentActive.isEmpty()) {
                        Text("No active widgets. Add some below!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentActive.forEachIndexed { index, widgetId ->
                                val name = when (widgetId) {
                                    "events" -> "Upcoming Events"
                                    "habits" -> "Habit Streaks"
                                    "xp" -> "Daily XP"
                                    "water" -> "Water Intake"
                                    "money" -> "Money Saved"
                                    else -> widgetId
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Checkbox(
                                                checked = true,
                                                onCheckedChange = {
                                                    val newList = currentActive.toMutableList()
                                                    newList.removeAt(index)
                                                    vm.updateWidgetsOrder(newList.joinToString(","))
                                                },
                                                modifier = Modifier.testTag("toggle_off_$widgetId")
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    if (index > 0) {
                                                        val newList = currentActive.toMutableList()
                                                        val temp = newList[index]
                                                        newList[index] = newList[index - 1]
                                                        newList[index - 1] = temp
                                                        vm.updateWidgetsOrder(newList.joinToString(","))
                                                    }
                                                },
                                                enabled = index > 0,
                                                modifier = Modifier.size(28.dp).testTag("move_up_$widgetId")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowUpward,
                                                    contentDescription = "Move Up",
                                                    tint = if (index > 0) Color.White else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (index < currentActive.size - 1) {
                                                        val newList = currentActive.toMutableList()
                                                        val temp = newList[index]
                                                        newList[index] = newList[index + 1]
                                                        newList[index + 1] = temp
                                                        vm.updateWidgetsOrder(newList.joinToString(","))
                                                    }
                                                },
                                                enabled = index < currentActive.size - 1,
                                                modifier = Modifier.size(28.dp).testTag("move_down_$widgetId")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDownward,
                                                    contentDescription = "Move Down",
                                                    tint = if (index < currentActive.size - 1) Color.White else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // INACTIVE WIDGETS SECTION
                    Text(
                        text = "AVAILABLE WIDGETS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (currentInactive.isEmpty()) {
                        Text("All widgets are currently active.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentInactive.forEach { widgetId ->
                                val name = when (widgetId) {
                                    "events" -> "Upcoming Events"
                                    "habits" -> "Habit Streaks"
                                    "xp" -> "Daily XP"
                                    "water" -> "Water Intake"
                                    "money" -> "Money Saved"
                                    else -> widgetId
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = false,
                                            onCheckedChange = {
                                                val newList = currentActive.toMutableList()
                                                newList.add(widgetId)
                                                vm.updateWidgetsOrder(newList.joinToString(","))
                                            },
                                            modifier = Modifier.testTag("toggle_on_$widgetId")
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showCustomizeDialog = false },
                    modifier = Modifier.testTag("close_customize_dialog_btn")
                ) {
                    Text("DONE", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // EMERGENCYSOS OVERLAY DIALOG
    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            icon = { Icon(Icons.Filled.CrisisAlert, "Emergency Alert", tint = CrimsonRed, modifier = Modifier.size(40.dp)) },
            title = { Text("EMERGENCY SOS SIGNAL", fontWeight = FontWeight.Black, color = CrimsonRed, textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Activating full LifeOS survival beacon. This will transmit your telemetry, bio-profile and critical coordinates.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Telemetry card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GPS Coordinates: 17.3850° N, 78.4867° E", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CrimsonRed)
                            Text("Current Region: Hyderabad, India", fontSize = 11.sp, color = CrimsonRed)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Allergy: Penicillin | Blood: O Positive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Primary SOS Contacts:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+91 98480 22338 (Emergency)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("FAKE COUNTDOWN SIMULATION: Sending signal in 3 seconds...", fontSize = 11.sp, color = CrimsonRed)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.triggerNotification("🚨 SOS Emergency contacts and hospital systems alerted with location telemetry.")
                        showEmergencyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("TRIGGER SOS SIGNAL NOW", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("ABORT BEACON")
                }
            }
        )
    }

    // VOICE SIMULATOR DIALOG
    if (showVoiceDialog) {
        var voiceTextPrompt by remember { mutableStateOf("") }
        var voiceResponseText by remember { mutableStateOf("Ready to receive voice command. Press Simulated Mic to ask LifeOS anything.") }
        var isVoiceGenerating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Mic, "Mic", tint = ElectricYellow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voice Assistant Core")
                }
            },
            text = {
                Column {
                    Text("Provides full system accessibility via speech guidance. Talk naturally:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple soundwave canvas animation simulator
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        val waveColor = ElectricYellow
                        val lines = 24
                        val gap = size.width / lines
                        for (i in 0 until lines) {
                            val h = if (isVoiceGenerating) {
                                (10..35).random().toFloat()
                            } else {
                                6f
                            }
                            drawLine(
                                color = waveColor.copy(alpha = if (isVoiceGenerating) 0.9f else 0.4f),
                                start = Offset(i * gap, size.height / 2 - h),
                                end = Offset(i * gap, size.height / 2 + h),
                                strokeWidth = 3f
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = voiceTextPrompt,
                        onValueChange = { voiceTextPrompt = it },
                        label = { Text("Simulated Speech Input") },
                        placeholder = { Text("e.g. How much money did I save today?") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = voiceResponseText,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (voiceTextPrompt.isNotBlank()) {
                            isVoiceGenerating = true
                            voiceResponseText = "Translating speech..."
                            // Leverage the viewModel's AI engine to simulate actual speech intelligence responses
                            val promptContext = "Answer this speech-guided question from the user: '$voiceTextPrompt'. Be extremely direct, conversational, and read-aloud-friendly as if you are a voice synthesizer speaker (max 40 words)."
                            android.util.Log.d("VoiceMode", "Speech translation query: $voiceTextPrompt")
                            
                            // Call Gemini
                            kotlinx.coroutines.GlobalScope.launch {
                                val reply = com.example.api.GeminiClient.generateContent(promptContext, "You are a voice assistant synthesizer.")
                                voiceResponseText = reply
                                isVoiceGenerating = false
                                vm.triggerNotification("🔊 Voice Assist: Reading out reply")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricYellow, contentColor = Color.Black)
                ) {
                    Text("TRANSLATE SPEECH")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoiceDialog = false }) {
                    Text("DISCONNECT")
                }
            }
        )
    }
}

@Composable
fun HubTile(title: String, icon: ImageVector, iconColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(65.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, title, tint = iconColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// --- HABITS TRACKER SCREEN ---
@Composable
fun HabitsScreen(vm: LifeOSViewModel) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    var newHabitTitle by remember { mutableStateOf("") }
    var rewardXp by remember { mutableStateOf("15") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Personal Habits Hub", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Daily tasks build passive streak flame indicators and earn high XP bonuses.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // Quick add row
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Enlist New Custom Habit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newHabitTitle,
                        onValueChange = { newHabitTitle = it },
                        placeholder = { Text("e.g. Read 15 pages", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )
                    OutlinedTextField(
                        value = rewardXp,
                        onValueChange = { rewardXp = it },
                        placeholder = { Text("XP", fontSize = 12.sp) },
                        modifier = Modifier.width(60.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )
                    
                    Button(
                        onClick = {
                            if (newHabitTitle.isNotBlank()) {
                                val xpNum = rewardXp.toIntOrNull() ?: 15
                                vm.addHabit(newHabitTitle, xpNum)
                                newHabitTitle = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_habit_button")
                    ) {
                        Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (habits.isEmpty()) {
                item {
                    Text("No habits found. Create some above!", modifier = Modifier.padding(12.dp))
                }
            } else {
                items(habits) { habit ->
                    HabitItemRow(habit = habit, onToggle = { vm.toggleHabit(habit) }, onDelete = { vm.deleteHabit(habit) })
                }
            }
        }
    }
}

@Composable
fun HabitItemRow(habit: Habit, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            if (habit.isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(symmetricPadding()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = habit.isCompletedToday,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("habit_checkbox_${habit.id}")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = habit.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, "Streak", tint = ElectricYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${habit.streak} day streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("+${habit.xpReward} XP Reward", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, "Delete Habit", tint = CrimsonRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// --- SMART FINANCE SCREEN ---
@Composable
fun FinanceScreen(vm: LifeOSViewModel) {
    val txs by vm.transactions.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var isExpense by remember { mutableStateOf(true) }

    val categories = listOf("Food", "Salary", "Freelance", "Leisure", "Bills", "Health", "Investments")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Smart Finance ledger", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("AI analyzes savings and provides actionable wealth recommendations.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // Ledger Panel adding
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Log Transaction Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Toggle Expense/Income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isExpense = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpense) CrimsonRed else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Expense", color = Color.White)
                    }
                    
                    Button(
                        onClick = { isExpense = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isExpense) EmeraldGreen else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Income", color = if (!isExpense) Color.Black else Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Title (e.g. Coffee)", fontSize = 12.sp) },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        placeholder = { Text("Amount", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category scroll
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amt > 0) {
                            vm.addTransaction(title, amt, if (isExpense) "Expense" else "Income", category)
                            title = ""
                            amountStr = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_tx_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Record Transaction", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ledger History
        Text("Transaction History", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (txs.isEmpty()) {
                item {
                    Text("No transactions logged. Build savings history above.")
                }
            } else {
                items(txs) { tx ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (tx.type == "Income") EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(
                                                alpha = 0.15f
                                            ), CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (tx.type == "Income") Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                        contentDescription = tx.type,
                                        tint = if (tx.type == "Income") EmeraldGreen else CrimsonRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(tx.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(tx.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${if (tx.type == "Income") "+" else "-"}$${String.format(Locale.getDefault(), "%.2f", tx.amount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (tx.type == "Income") EmeraldGreen else CrimsonRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { vm.deleteTransaction(tx) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Close, "Delete", tint = CrimsonRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- HEALTH CENTER SCREEN ---
@Composable
fun HealthScreen(vm: LifeOSViewModel) {
    val records by vm.healthRecords.collectAsStateWithLifecycle()
    val profile by vm.userProfile.collectAsStateWithLifecycle()

    var sleepHoursText by remember { mutableStateOf("7.5") }
    var currentMood by remember { mutableStateOf("Energetic") }
    var currentStress by remember { mutableStateOf("Low") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Health & Fitness Center", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Track physical parameters and log vital states to earn XP coefficients.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Steps tracking widget
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Simulated Telemetry Pedometer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${profile.stepsToday}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = EmeraldGreen)
                        Text("Steps Walked Today", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = { vm.addSteps(1500) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_steps_button")
                    ) {
                        Icon(Icons.Filled.Add, "add steps", tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+1,500 Steps", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Hydration tracker widget
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Hydration Tracking Station", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${profile.waterIntakeToday} ml", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text("Water Consumed Today", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { vm.addWater(250) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+250ml", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { vm.addWater(500) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+500ml", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sleep & mood logs panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Log Daily Sleep & Psychological States", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sleepHoursText,
                        onValueChange = { sleepHoursText = it },
                        label = { Text("Sleep hours") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    OutlinedTextField(
                        value = currentMood,
                        onValueChange = { currentMood = it },
                        label = { Text("Current Mood") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = currentStress,
                        onValueChange = { currentStress = it },
                        label = { Text("Stress Level") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    Button(
                        onClick = {
                            val sleep = sleepHoursText.toDoubleOrNull() ?: 7.0
                            vm.updateSleepAndMood(sleep, currentMood, currentStress)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("log_health_btn")
                    ) {
                        Text("Log Vital Vibe")
                    }
                }
            }
        }

        // Health history overview
        Text("Vital Log Archives", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            items(records) { rec ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Date: ${rec.dateString}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Steps: ${rec.steps} | Calories: ${rec.calories} kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Water: ${rec.waterIntakeMl}ml | Sleep: ${rec.sleepHours}h", fontSize = 11.sp)
                            Text("Vibe: ${rec.mood} (${rec.stress})", fontSize = 11.sp, color = ElectricYellow, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- MEMORY VAULT SCREEN ---
@Composable
fun MemoriesScreen(vm: LifeOSViewModel) {
    val query by vm.memorySearchQuery.collectAsStateWithLifecycle()
    val memories by vm.memories.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Note") }

    val types = listOf("Note", "Bill", "Receipt", "Certificate", "Medical")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Memory Vault", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Safely store files, journals, receipts and medical records offline.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // Search panel
        OutlinedTextField(
            value = query,
            onValueChange = { vm.memorySearchQuery.value = it },
            placeholder = { Text("Search your entire life vault instantly...") },
            leadingIcon = { Icon(Icons.Filled.Search, "search icon") },
            modifier = Modifier.fillMaxWidth().testTag("memory_search_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Add memory panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Earmark New Memory Vault Entry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Title (e.g. Hyderabad Trip)", fontSize = 12.sp) },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("Detailed entry", fontSize = 12.sp) },
                        modifier = Modifier.weight(2f),
                        textStyle = TextStyle(fontSize = 12.sp)
                    )
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Type:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    types.forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t, fontSize = 9.sp) }
                        )
                    }
                }

                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            vm.addMemory(title, content, selectedType)
                            title = ""
                            content = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_memory_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Secure Earmark Vault Entry")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List memories
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (memories.isEmpty()) {
                item {
                    Text("No matched logs in the security vault.", modifier = Modifier.padding(12.dp))
                }
            } else {
                items(memories) { memory ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (memory.type) {
                                            "Certificate" -> Icons.Filled.Badge
                                            "Bill", "Receipt" -> Icons.Filled.ReceiptLong
                                            "Medical" -> Icons.Filled.MedicalServices
                                            else -> Icons.Filled.Bookmark
                                        },
                                        contentDescription = "type icon",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = memory.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                IconButton(
                                    onClick = { vm.deleteMemory(memory) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, "Delete", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = memory.content,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Vault Locked | Category: ${memory.type}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- LEARNING HUB SCREEN ---
@Composable
fun LearningScreen(vm: LifeOSViewModel) {
    val items by vm.learningItems.collectAsStateWithLifecycle()
    var studyTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Programming") }

    val categories = listOf("Programming", "Book", "Language", "Skill")

    // Quizzing simulator
    var quizText by remember { mutableStateOf("Press bottom action to run live AI Study Mentor Quiz generator on your topics.") }
    var isQuizGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Cognitive Learning Hub", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Track curriculum progress and summon interactive AI-quizzes on subject files.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Add progress course
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Incorporate Study Module", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = studyTitle,
                        onValueChange = { studyTitle = it },
                        placeholder = { Text("e.g. Jetpack Compose Certification", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    Button(
                        onClick = {
                            if (studyTitle.isNotBlank()) {
                                vm.addLearningItem(studyTitle, selectedCategory)
                                studyTitle = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_learning_btn")
                    ) {
                        Text("Track", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 9.sp) }
                        )
                    }
                }
            }
        }

        // Active courses / items
        Text("Active Curriculum Modules", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            if (items.isEmpty()) {
                item {
                    Text("No studying topics tracked. Set one up above.")
                }
            } else {
                items(items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Category: ${item.type} | Study streak: ${item.streak} days", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Row {
                                    IconButton(onClick = { vm.incrementLearningProgress(item) }) {
                                        Icon(Icons.Filled.PlayArrow, "Study Progress", tint = EmeraldGreen)
                                    }
                                    IconButton(onClick = { vm.deleteLearningItem(item) }) {
                                        Icon(Icons.Filled.Delete, "Delete", tint = CrimsonRed)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { item.progress / 100f },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("${item.progress}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // AI QUIZ PREVIEW STATION
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Revision Quiz Master", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    if (isQuizGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = quizText,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isQuizGenerating = true
                        val topics = items.joinToString { it.title }
                        val prompt = "Based on these study modules: '$topics', generate 2 simple, interactive multi-choice quiz questions with answer options (be extremely short and concise)."
                        kotlinx.coroutines.GlobalScope.launch {
                            val reply = com.example.api.GeminiClient.generateContent(prompt, "You are a professional quiz master.")
                            quizText = reply
                            isQuizGenerating = false
                            vm.triggerNotification("🎓 New Quiz compiled successfully. Solve to gain +30 XP!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End).testTag("quiz_ai_btn"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Assemble AI Quiz", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

// --- GOALS SCREEN ---
@Composable
fun GoalsScreen(vm: LifeOSViewModel) {
    val goals by vm.goals.collectAsStateWithLifecycle()
    var goalTitle by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("Monthly") }

    val periods = listOf("Daily", "Weekly", "Monthly", "Lifetime")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Strategic Goals Board", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Breakdown large objectives and synchronize target trackers to level up.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // Create goal
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enlist New Life Objective", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        placeholder = { Text("e.g. Finish marathon run", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp)
                    )

                    Button(
                        onClick = {
                            if (goalTitle.isNotBlank()) {
                                vm.addGoal(goalTitle, selectedPeriod)
                                goalTitle = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_goal_btn")
                    ) {
                        Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    periods.forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(period, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List objectives
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            if (goals.isEmpty()) {
                item {
                    Text("No goals set. Formulate lifetime objectives above.")
                }
            } else {
                items(goals) { goal ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (goal.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, if (goal.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = goal.isCompleted,
                                    onCheckedChange = { vm.toggleGoal(goal) },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("goal_checkbox_${goal.id}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = goal.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text("Timeframe: ${goal.type}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            IconButton(
                                onClick = { vm.deleteGoal(goal) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Delete, "Delete", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- LIFE MAP SCREEN (pinned visited places memories) ---
@Composable
fun MapScreen(vm: LifeOSViewModel) {
    // Elegant localized visited memories map list showing tagged pins
    val places = listOf(
        LifePin("Glacier Point, Yosemite, CA", "Stunning panoramic valley view hike with friends.", "April 12, 2026", "37.7276° N, 119.5743° W"),
        LifePin("Balkampet, Hyderabad, India", "Revisited old college campus and local food eateries.", "June 10, 2026", "17.4475° N, 78.4482° E"),
        LifePin("Shibuya, Tokyo, Japan", "Night city photography and fresh ramen dining.", "March 24, 2026", "35.6580° N, 139.7016° E")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Geographic Life Map", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Interactive security log tying memories and diaries to exact physical GPS pins.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Conceptual premium globe canvas drawing
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Background futuristic vector layout
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                    // Draw grid coordinate lines
                    for (i in 1..8) {
                        drawLine(strokeColor, Offset(0f, (i * size.height / 8)), Offset(size.width, (i * size.height / 8)), 1.5f)
                        drawLine(strokeColor, Offset((i * size.width / 8), 0f), Offset((i * size.width / 8), size.height), 1.5f)
                    }
                    
                    // Draw mapped markers
                    drawCircle(Color(0xFF00E5FF), 8f, Offset(size.width * 0.4f, size.height * 0.5f))
                    drawCircle(Color(0xFFFF2E93), 8f, Offset(size.width * 0.7f, size.height * 0.3f))
                    drawCircle(Color(0xFF00E676), 8f, Offset(size.width * 0.2f, size.height * 0.7f))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Satellite, "Satellite Telemetry", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("GLOBAL GPS TELEMETRY ACTIVE", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text("3 secure mapped memory pins actively synchronized offline.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Text("Synchronized Coordinates Pins", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(places) { pin ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pin.locationName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(pin.coordinates, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pin.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Logged on: ${pin.date}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

data class LifePin(
    val locationName: String,
    val description: String,
    val date: String,
    val coordinates: String
)

// --- PROFILE & ACCREDITATIONS HUB ---
@Composable
fun ProfileScreen(vm: LifeOSViewModel) {
    val profile by vm.userProfile.collectAsStateWithLifecycle()

    val badges = listOf(
        BadgeItem("Habit Hero", "Achieve a 5+ streak on habits", Icons.Filled.LocalFireDepartment, ElectricYellow, true),
        BadgeItem("Wealth Guard", "Save over $100 in Smart Finance", Icons.Filled.Shield, MaterialTheme.colorScheme.primary, true),
        BadgeItem("Cyber Scholar", "Reach 80% on study modules", Icons.Filled.MilitaryTech, MaterialTheme.colorScheme.secondary, true),
        BadgeItem("SOS Active", "Setup emergency rescue telemetry", Icons.Filled.CrisisAlert, CrimsonRed, true),
        BadgeItem("Life Master", "Reach Operating Level 100", Icons.Filled.EmojiEvents, Color(0xFFFFD600), false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("LifeOS System Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        
        // RPG Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulated Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ), CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A1",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("Agent 01 - Sudarsi Kalyan", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Primary system administrator", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Operating Level ${profile.level}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }
        }

        Text("Unlocked Achievements & Badges", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(badges) { badge ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(badge.color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(badge.icon, badge.name, tint = badge.color)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(badge.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(badge.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (badge.isUnlocked) {
                            Icon(Icons.Filled.Verified, "unlocked", tint = EmeraldGreen)
                        } else {
                            Icon(Icons.Filled.Lock, "locked", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }
}

data class BadgeItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isUnlocked: Boolean
)

// --- CHAT / AI ASSISTANT OVERLAY SLIDING BOTTOM SHEET ---
@Composable
fun AiAssistantOverlay(vm: LifeOSViewModel, onDismiss: () -> Unit) {
    val messages by vm.chatMessages.collectAsStateWithLifecycle()
    val chatInput by vm.chatInput.collectAsStateWithLifecycle()
    val isGenerating by vm.isAiGenerating.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_assistant_panel"),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "AI Assistant Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "LifeOS Core Intelligence",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Powered by Gemini 3.5 Flash",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_assistant_btn")
                ) {
                    Icon(Icons.Filled.Close, "close assistant", modifier = Modifier.size(24.dp))
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Chat Messages list
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                messages.forEach { msg ->
                    val isAi = msg.sender == "ai"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isAi) 2.dp else 16.dp,
                                bottomEnd = if (isAi) 16.dp else 2.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAi) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = if (isAi) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
                
                if (isGenerating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Core AI synthesizing context...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                
                // Auto scroll to bottom when new messages append
                LaunchedEffect(messages.size, isGenerating) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Input panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { vm.chatInput.value = it },
                    placeholder = { Text("Ask anything... e.g. Analyze my finances") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        vm.sendAssistantMessage()
                        keyboardController?.hide()
                    })
                )

                IconButton(
                    onClick = {
                        vm.sendAssistantMessage()
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(44.dp)
                        .testTag("send_ai_chat_btn")
                ) {
                    Icon(Icons.Filled.Send, "send prompt", tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
