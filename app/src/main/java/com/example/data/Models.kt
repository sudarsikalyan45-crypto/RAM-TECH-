package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val streak: Int = 0,
    val isCompletedToday: Boolean = false,
    val xpReward: Int = 15
)

@Entity(tableName = "finance_transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // "Income" or "Expense"
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val steps: Int = 0,
    val calories: Int = 0,
    val waterIntakeMl: Int = 0,
    val sleepHours: Double = 0.0,
    val mood: String = "Neutral",
    val stress: String = "Low",
    val dateString: String, // e.g. "2026-07-02"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_items")
data class LearningItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "Book", "Course", "Programming", "Language"
    val progress: Int = 0, // 0 to 100
    val streak: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "Daily", "Weekly", "Monthly", "Yearly", "Lifetime"
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "memory_items")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val type: String, // "Note", "Bill", "Receipt", "Certificate", "Medical"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "timeline_items")
data class TimelineItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timeString: String, // e.g. "08:30 AM"
    val category: String, // "Routine", "Gym", "Meals", "Office", "Leisure", "Sleep"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Only 1 profile row
    val level: Int = 1,
    val xp: Int = 0,
    val waterIntakeToday: Int = 0,
    val sleepHoursToday: Double = 0.0,
    val stepsToday: Int = 0,
    val moodToday: String = "Neutral",
    val moneySaved: Double = 0.0,
    val onboardingCompleted: Boolean = false,
    val widgetsOrderCsv: String = "events,habits,xp,water,money"
)
