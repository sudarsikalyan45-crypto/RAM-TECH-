package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeOSDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // --- Habits ---
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("UPDATE habits SET isCompletedToday = 0")
    suspend fun resetHabitsCompletion()

    // --- Finance ---
    @Query("SELECT * FROM finance_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FinanceTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinanceTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: FinanceTransaction)

    // --- Health ---
    @Query("SELECT * FROM health_records ORDER BY timestamp DESC")
    fun getAllHealthRecords(): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE dateString = :date LIMIT 1")
    suspend fun getHealthRecordByDate(date: String): HealthRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecord)

    // --- Learning ---
    @Query("SELECT * FROM learning_items ORDER BY timestamp DESC")
    fun getAllLearningItems(): Flow<List<LearningItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningItem(item: LearningItem)

    @Delete
    suspend fun deleteLearningItem(item: LearningItem)

    // --- Goals ---
    @Query("SELECT * FROM goals ORDER BY timestamp DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    // --- Memories ---
    @Query("SELECT * FROM memory_items ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memory_items WHERE title LIKE :query OR content LIKE :query OR type LIKE :query")
    fun searchMemories(query: String): Flow<List<MemoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryItem)

    @Delete
    suspend fun deleteMemory(memory: MemoryItem)

    // --- Timeline ---
    @Query("SELECT * FROM timeline_items ORDER BY timestamp ASC")
    fun getAllTimelineItems(): Flow<List<TimelineItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineItem(item: TimelineItem)

    @Delete
    suspend fun deleteTimelineItem(item: TimelineItem)
}
