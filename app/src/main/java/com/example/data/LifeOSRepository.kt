package com.example.data

import kotlinx.coroutines.flow.Flow

class LifeOSRepository(private val dao: LifeOSDao) {

    // --- User Profile ---
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()

    suspend fun updateProfile(profile: UserProfile) = dao.insertOrUpdateProfile(profile)

    // --- Habits ---
    val allHabits: Flow<List<Habit>> = dao.getAllHabits()

    suspend fun insertHabit(habit: Habit) = dao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)
    suspend fun resetHabits() = dao.resetHabitsCompletion()

    // --- Finance ---
    val allTransactions: Flow<List<FinanceTransaction>> = dao.getAllTransactions()

    suspend fun insertTransaction(transaction: FinanceTransaction) = dao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinanceTransaction) = dao.deleteTransaction(transaction)

    // --- Health ---
    val allHealthRecords: Flow<List<HealthRecord>> = dao.getAllHealthRecords()

    suspend fun getHealthRecordByDate(date: String): HealthRecord? = dao.getHealthRecordByDate(date)
    suspend fun insertHealthRecord(record: HealthRecord) = dao.insertHealthRecord(record)

    // --- Learning ---
    val allLearningItems: Flow<List<LearningItem>> = dao.getAllLearningItems()

    suspend fun insertLearning(item: LearningItem) = dao.insertLearningItem(item)
    suspend fun deleteLearning(item: LearningItem) = dao.deleteLearningItem(item)

    // --- Goals ---
    val allGoals: Flow<List<Goal>> = dao.getAllGoals()

    suspend fun insertGoal(goal: Goal) = dao.insertGoal(goal)
    suspend fun deleteGoal(goal: Goal) = dao.deleteGoal(goal)

    // --- Memories ---
    val allMemories: Flow<List<MemoryItem>> = dao.getAllMemories()

    fun searchMemories(query: String): Flow<List<MemoryItem>> = dao.searchMemories("%$query%")

    suspend fun insertMemory(memory: MemoryItem) = dao.insertMemory(memory)
    suspend fun deleteMemory(memory: MemoryItem) = dao.deleteMemory(memory)

    // --- Timeline ---
    val allTimelineItems: Flow<List<TimelineItem>> = dao.getAllTimelineItems()

    suspend fun insertTimeline(item: TimelineItem) = dao.insertTimelineItem(item)
    suspend fun deleteTimeline(item: TimelineItem) = dao.deleteTimelineItem(item)
}
