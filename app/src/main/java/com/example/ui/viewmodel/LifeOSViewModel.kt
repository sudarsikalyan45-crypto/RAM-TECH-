package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class LifeOSViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LifeOSRepository
    
    // --- UI Navigation State ---
    val currentScreen = MutableStateFlow("dashboard") // dashboard, habits, finance, health, memories, learning, goals, map, profile
    
    // --- Search Query for Memory Vault ---
    val memorySearchQuery = MutableStateFlow("")

    // --- AI Assistant UI States ---
    val isAssistantOpen = MutableStateFlow(false)
    val chatInput = MutableStateFlow("")
    val isAiGenerating = MutableStateFlow(false)
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("ai", "Welcome to LifeOS. I am your personal AI Operating Assistant. How can I help you customize your day, analyze your health, or plan your goals?")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // --- Notification Dialog trigger ---
    val xpNotification = MutableStateFlow<String?>(null)

    // --- Observables from Room ---
    val userProfile: StateFlow<UserProfile> = flow {
        // Fallback value while loading
        emit(UserProfile())
    }.flatMapLatest {
        repository.userProfile.map { it ?: UserProfile() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val habits: StateFlow<List<Habit>> = flow {
        emit(emptyList<Habit>())
    }.flatMapLatest {
        repository.allHabits
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<FinanceTransaction>> = flow {
        emit(emptyList<FinanceTransaction>())
    }.flatMapLatest {
        repository.allTransactions
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val healthRecords: StateFlow<List<HealthRecord>> = flow {
        emit(emptyList<HealthRecord>())
    }.flatMapLatest {
        repository.allHealthRecords
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learningItems: StateFlow<List<LearningItem>> = flow {
        emit(emptyList<LearningItem>())
    }.flatMapLatest {
        repository.allLearningItems
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<Goal>> = flow {
        emit(emptyList<Goal>())
    }.flatMapLatest {
        repository.allGoals
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryItem>> = memorySearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allMemories
            } else {
                repository.searchMemories(query)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timelineItems: StateFlow<List<TimelineItem>> = flow {
        emit(emptyList<TimelineItem>())
    }.flatMapLatest {
        repository.allTimelineItems
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LifeOSRepository(database.lifeOSDao())
        
        // Populate default data if DB is empty
        viewModelScope.launch {
            repository.userProfile.firstOrNull()?.let {
                // Already populated
            } ?: run {
                prepopulateDatabase()
            }
        }
    }

    private suspend fun prepopulateDatabase() {
        // 1. Profile
        repository.updateProfile(
            UserProfile(
                id = 1,
                level = 3,
                xp = 240,
                waterIntakeToday = 1200,
                sleepHoursToday = 7.5,
                stepsToday = 6230,
                moodToday = "Energetic",
                moneySaved = 345.50,
                onboardingCompleted = false,
                widgetsOrderCsv = "events,habits,xp,water,money"
            )
        )

        // 2. Habits
        repository.insertHabit(Habit(name = "Wake Up at 6:00 AM", streak = 5, isCompletedToday = true, xpReward = 15))
        repository.insertHabit(Habit(name = "30 Min Cardio Workout", streak = 2, isCompletedToday = false, xpReward = 20))
        repository.insertHabit(Habit(name = "Drink 3 Liters of Water", streak = 12, isCompletedToday = true, xpReward = 15))
        repository.insertHabit(Habit(name = "Code / Read for 45 Mins", streak = 7, isCompletedToday = true, xpReward = 25))
        repository.insertHabit(Habit(name = "Evening Mindfulness", streak = 0, isCompletedToday = false, xpReward = 15))

        // 3. Goals
        repository.insertGoal(Goal(title = "Save $1,000 for travel fund", type = "Monthly", isCompleted = false))
        repository.insertGoal(Goal(title = "Complete Jetpack Compose Certification", type = "Weekly", isCompleted = false))
        repository.insertGoal(Goal(title = "Maintain 8,000 daily steps", type = "Daily", isCompleted = true))
        repository.insertGoal(Goal(title = "Run a full marathon (42km)", type = "Lifetime", isCompleted = false))

        // 4. Learning
        repository.insertLearning(LearningItem(title = "Android & Kotlin Core", type = "Programming", progress = 85, streak = 14))
        repository.insertLearning(LearningItem(title = "Atomic Habits", type = "Book", progress = 45, streak = 5))
        repository.insertLearning(LearningItem(title = "German Level A1", type = "Language", progress = 20, streak = 2))

        // 5. Finance
        repository.insertTransaction(FinanceTransaction(title = "Freelance UI Project", amount = 650.0, type = "Income", category = "Freelance"))
        repository.insertTransaction(FinanceTransaction(title = "Organic Grocery Bill", amount = 78.40, type = "Expense", category = "Food"))
        repository.insertTransaction(FinanceTransaction(title = "Specialty Espresso", amount = 6.20, type = "Expense", category = "Leisure"))
        repository.insertTransaction(FinanceTransaction(title = "Monthly Gym Membership", amount = 45.0, type = "Expense", category = "Health"))

        // 6. Memory Vault
        repository.insertMemory(MemoryItem(title = "Yosemite Cabin Hike", content = "Unbelievable view from Glacier Point with crisp pine-infused air. Stored photo ID: IMG_3012.JPG", type = "Note"))
        repository.insertMemory(MemoryItem(title = "Udemy Swift UI Invoice", content = "Receipt for swift development course for our cross-platform references. Total: $12.99", type = "Receipt"))
        repository.insertMemory(MemoryItem(title = "Bachelor of Science Degree", content = "Graduation certificate with high honors from State University.", type = "Certificate"))

        // 7. Timeline
        repository.insertTimeline(TimelineItem(title = "Sunrise Wakeup & Hydrate", description = "Drank 500ml pure alkaline water to refresh body systems.", timeString = "06:15 AM", category = "Routine"))
        repository.insertTimeline(TimelineItem(title = "Gym Power Session", description = "45 minutes high intensity interval training (HIIT). Earned 20 XP.", timeString = "07:30 AM", category = "Gym"))
        repository.insertTimeline(TimelineItem(title = "Office Daily Standup", description = "Synced product development sprint metrics with team lead.", timeString = "09:15 AM", category = "Office"))
        repository.insertTimeline(TimelineItem(title = "Mindful Evening Walk", description = "Finished 5,000 steps around the city park listening to audio-book.", timeString = "06:45 PM", category = "Leisure"))

        // 8. Health record (mock for today)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        repository.insertHealthRecord(
            HealthRecord(
                steps = 6230,
                calories = 1940,
                waterIntakeMl = 1200,
                sleepHours = 7.5,
                mood = "Energetic",
                stress = "Low",
                dateString = today
            )
        )
    }

    // --- Action Handlers ---

    fun triggerNotification(message: String) {
        xpNotification.value = message
    }

    fun dismissNotification() {
        xpNotification.value = null
    }

    // --- XP Logic ---
    private fun gainXP(amount: Int) {
        viewModelScope.launch {
            val profile = userProfile.value
            val newXp = profile.xp + amount
            val previousLevel = profile.level
            val newLevel = (newXp / 100) + 1
            
            repository.updateProfile(
                profile.copy(
                    xp = newXp,
                    level = newLevel
                )
            )

            if (newLevel > previousLevel) {
                triggerNotification("⚡ LEVEL UP! You reached Level $newLevel! 🏆 Unlocked Cyber Slate theme decoration and a rewards lootbox.")
            } else {
                triggerNotification("⚡ +$amount XP earned towards Level $newLevel!")
            }
        }
    }

    // --- Habit Actions ---
    fun toggleHabit(habit: Habit) {
        viewModelScope.launch {
            val isNowCompleted = !habit.isCompletedToday
            val newStreak = if (isNowCompleted) habit.streak + 1 else maxOf(0, habit.streak - 1)
            val updated = habit.copy(
                isCompletedToday = isNowCompleted,
                streak = newStreak
            )
            repository.updateHabit(updated)
            
            if (isNowCompleted) {
                gainXP(habit.xpReward)
                // Increment steps as typical gamified habit
                if (habit.name.contains("Walk", true) || habit.name.contains("Cardio", true)) {
                    addSteps(2500)
                }
            }
        }
    }

    fun addHabit(name: String, reward: Int) {
        viewModelScope.launch {
            repository.insertHabit(Habit(name = name, xpReward = reward))
            gainXP(10) // 10 XP for setting a goal habit
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // --- Finance Actions ---
    fun addTransaction(title: String, amount: Double, type: String, category: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                FinanceTransaction(title = title, amount = amount, type = type, category = category)
            )
            val currentProfile = userProfile.value
            val netImpact = if (type == "Income") amount else -amount
            val newSavings = maxOf(0.0, currentProfile.moneySaved + netImpact)
            repository.updateProfile(currentProfile.copy(moneySaved = newSavings))
            
            if (type == "Income") {
                gainXP(15) // earn XP for earning or saving
            } else {
                gainXP(5) // smaller reward for tracking budgets
            }
        }
    }

    fun deleteTransaction(transaction: FinanceTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            val currentProfile = userProfile.value
            val reverseImpact = if (transaction.type == "Income") -transaction.amount else transaction.amount
            repository.updateProfile(currentProfile.copy(moneySaved = maxOf(0.0, currentProfile.moneySaved + reverseImpact)))
        }
    }

    // --- Health Actions ---
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val profile = userProfile.value
            val updatedWater = profile.waterIntakeToday + amountMl
            repository.updateProfile(profile.copy(waterIntakeToday = updatedWater))
            
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existingRecord = repository.getHealthRecordByDate(today) ?: HealthRecord(dateString = today)
            repository.insertHealthRecord(
                existingRecord.copy(
                    waterIntakeMl = existingRecord.waterIntakeMl + amountMl,
                    timestamp = System.currentTimeMillis()
                )
            )
            
            gainXP(5)
        }
    }

    fun addSteps(amount: Int) {
        viewModelScope.launch {
            val profile = userProfile.value
            val updatedSteps = profile.stepsToday + amount
            repository.updateProfile(profile.copy(stepsToday = updatedSteps))
            
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existingRecord = repository.getHealthRecordByDate(today) ?: HealthRecord(dateString = today)
            repository.insertHealthRecord(
                existingRecord.copy(
                    steps = existingRecord.steps + amount,
                    calories = existingRecord.calories + (amount * 0.04).toInt(),
                    timestamp = System.currentTimeMillis()
                )
            )
            
            gainXP((amount / 1000) * 10) // 10 XP per 1,000 steps
        }
    }

    fun updateSleepAndMood(sleepHours: Double, mood: String, stress: String) {
        viewModelScope.launch {
            val profile = userProfile.value
            repository.updateProfile(profile.copy(sleepHoursToday = sleepHours, moodToday = mood))
            
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existingRecord = repository.getHealthRecordByDate(today) ?: HealthRecord(dateString = today)
            repository.insertHealthRecord(
                existingRecord.copy(
                    sleepHours = sleepHours,
                    mood = mood,
                    stress = stress,
                    timestamp = System.currentTimeMillis()
                )
            )
            
            gainXP(15)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val profile = userProfile.value
            repository.updateProfile(profile.copy(onboardingCompleted = true))
            triggerNotification("🚀 Welcome to LifeOS! Your system has been successfully initialized.")
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            val profile = userProfile.value
            repository.updateProfile(profile.copy(onboardingCompleted = false))
        }
    }

    fun updateWidgetsOrder(newCsv: String) {
        viewModelScope.launch {
            val profile = userProfile.value
            repository.updateProfile(profile.copy(widgetsOrderCsv = newCsv))
        }
    }

    // --- Learning Actions ---
    fun addLearningItem(title: String, type: String) {
        viewModelScope.launch {
            repository.insertLearning(LearningItem(title = title, type = type))
            gainXP(10)
        }
    }

    fun incrementLearningProgress(item: LearningItem) {
        viewModelScope.launch {
            val newProgress = minOf(100, item.progress + 10)
            val updated = item.copy(
                progress = newProgress,
                streak = if (newProgress % 30 == 0) item.streak + 1 else item.streak
            )
            repository.insertLearning(updated)
            
            gainXP(20) // Study progress reward
            if (newProgress == 100) {
                gainXP(50) // completion bonus
                triggerNotification("🎓 Mastery unlocked! You finished learning: ${item.title}!")
            }
        }
    }

    fun deleteLearningItem(item: LearningItem) {
        viewModelScope.launch {
            repository.deleteLearning(item)
        }
    }

    // --- Goal Actions ---
    fun addGoal(title: String, type: String) {
        viewModelScope.launch {
            repository.insertGoal(Goal(title = title, type = type))
            gainXP(10)
        }
    }

    fun toggleGoal(goal: Goal) {
        viewModelScope.launch {
            val isNowCompleted = !goal.isCompleted
            repository.insertGoal(goal.copy(isCompleted = isNowCompleted))
            
            if (isNowCompleted) {
                gainXP(40) // substantial goal completion XP
                triggerNotification("🎯 Target Achieved: ${goal.title}! +40 XP")
            }
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // --- Memory Actions ---
    fun addMemory(title: String, content: String, type: String) {
        viewModelScope.launch {
            repository.insertMemory(MemoryItem(title = title, content = content, type = type))
            gainXP(15)
        }
    }

    fun deleteMemory(memory: MemoryItem) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    // --- Timeline Actions ---
    fun addTimelineEvent(title: String, description: String, timeString: String, category: String) {
        viewModelScope.launch {
            repository.insertTimeline(
                TimelineItem(title = title, description = description, timeString = timeString, category = category)
            )
            gainXP(10)
        }
    }

    fun deleteTimelineEvent(item: TimelineItem) {
        viewModelScope.launch {
            repository.deleteTimeline(item)
        }
    }

    // --- AI Assistant Logic ---
    
    fun sendAssistantMessage() {
        val input = chatInput.value
        if (input.isBlank()) return
        
        viewModelScope.launch {
            // Append User message
            _chatMessages.value = _chatMessages.value + ChatMessage("user", input)
            chatInput.value = ""
            isAiGenerating.value = true
            
            // Build rich contextual background prompt
            val p = userProfile.value
            val hList = habits.value.joinToString { "${it.name} (${if (it.isCompletedToday) "Done" else "Pending"})" }
            val gList = goals.value.joinToString { "${it.title} (${if (it.isCompleted) "Completed" else "In Progress"})" }
            val mList = memories.value.take(4).joinToString { "${it.title}: ${it.content}" }
            
            val sysInstruction = """
                You are LifeOS, the premium, super-intelligent futuristic personal operating system.
                The user relies on you as a lifestyle partner, financial planner, health analyst, study advisor, and secure data organizer.
                
                Current User Context:
                - Level: ${p.level} (XP: ${p.xp}/100)
                - Steps taken today: ${p.stepsToday}
                - Water intake today: ${p.waterIntakeToday} ml
                - Sleep today: ${p.sleepHoursToday} hours
                - Mood today: ${p.moodToday}
                - Tracked Savings: $${String.format(Locale.getDefault(), "%.2f", p.moneySaved)}
                
                Tracked Habits: $hList
                Active Goals: $gList
                Recent Memories: $mList
                
                Be concise, empathetic, motivating, and incredibly insightful. Speak with an intelligent, sleek, futuristic Apple-inspired tone. Avoid generic motivational quotes; focus on data-driven positive reinforcement and actionable steps.
            """.trimIndent()
            
            val aiResponseText = GeminiClient.generateContent(input, sysInstruction)
            _chatMessages.value = _chatMessages.value + ChatMessage("ai", aiResponseText)
            isAiGenerating.value = false
        }
    }

    fun getAiQuickRecommendation(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val p = userProfile.value
            val pendingHabits = habits.value.filter { !it.isCompletedToday }.map { it.name }
            val contextPrompt = """
                Generate one short single-sentence (max 18 words) high-impact personalized daily recommendation based on these metrics:
                Steps: ${p.stepsToday}, Water: ${p.waterIntakeToday}ml, Mood: ${p.moodToday}, Savings: $${p.moneySaved}.
                Uncompleted tasks/habits: ${pendingHabits.joinToString()}.
                Make it futuristic, direct, and actionable!
            """.trimIndent()
            val rec = GeminiClient.generateContent(contextPrompt, "You are the LifeOS Core AI Engine.")
            onResult(rec)
        }
    }
}
