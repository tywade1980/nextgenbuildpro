package com.aireceptionist.app.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.aireceptionist.app.data.models.TrainingData

@Dao
interface TrainingDataDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingData(data: TrainingData)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingDataBatch(dataList: List<TrainingData>)
    
    @Update
    suspend fun updateTrainingData(data: TrainingData)
    
    @Query("SELECT * FROM training_data WHERE id = :id")
    suspend fun getTrainingData(id: String): TrainingData?
    
    @Query("SELECT * FROM training_data WHERE expectedIntent = :intent ORDER BY createdAt DESC")
    suspend fun getTrainingDataByIntent(intent: String): List<TrainingData>
    
    @Query("SELECT * FROM training_data WHERE domain = :domain ORDER BY createdAt DESC")
    suspend fun getTrainingDataByDomain(domain: String): List<TrainingData>
    
    @Query("SELECT * FROM training_data WHERE source = :source ORDER BY createdAt DESC")
    suspend fun getTrainingDataBySource(source: String): List<TrainingData>
    
    @Query("SELECT * FROM training_data WHERE language = :language ORDER BY createdAt DESC")
    suspend fun getTrainingDataByLanguage(language: String): List<TrainingData>
    
    @Query("SELECT * FROM training_data WHERE isCorrect IS NULL ORDER BY createdAt DESC")
    suspend fun getUnlabeledTrainingData(): List<TrainingData>
    
    @Query("SELECT * FROM training_data WHERE isCorrect = 0 ORDER BY createdAt DESC")
    suspend fun getIncorrectTrainingData(): List<TrainingData>
    
    @Query("SELECT DISTINCT expectedIntent FROM training_data ORDER BY expectedIntent ASC")
    suspend fun getAllIntents(): List<String>
    
    @Query("SELECT DISTINCT domain FROM training_data WHERE domain IS NOT NULL ORDER BY domain ASC")
    suspend fun getAllDomains(): List<String>
    
    @Query("DELETE FROM training_data WHERE id = :id")
    suspend fun deleteTrainingData(id: String)
    
    @Query("DELETE FROM training_data WHERE createdAt < :cutoffTime")
    suspend fun deleteOldTrainingData(cutoffTime: Long)
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM training_data")
    suspend fun getTotalTrainingDataCount(): Int
    
    @Query("SELECT COUNT(*) FROM training_data WHERE isCorrect = 1")
    suspend fun getCorrectPredictionsCount(): Int
    
    @Query("SELECT COUNT(*) FROM training_data WHERE isCorrect = 0")
    suspend fun getIncorrectPredictionsCount(): Int
    
    @Query("SELECT AVG(confidence) FROM training_data WHERE confidence IS NOT NULL")
    suspend fun getAverageConfidence(): Double?
    
    @Query("""
        SELECT expectedIntent, COUNT(*) as count 
        FROM training_data 
        GROUP BY expectedIntent 
        ORDER BY count DESC
    """)
    suspend fun getIntentDistribution(): List<IntentCount>
    
    // Live data for UI
    @Query("SELECT * FROM training_data ORDER BY createdAt DESC LIMIT 100")
    fun getRecentTrainingDataLiveData(): LiveData<List<TrainingData>>
    
    @Query("SELECT COUNT(*) FROM training_data WHERE isCorrect IS NULL")
    fun getUnlabeledCountLiveData(): LiveData<Int>
}

data class IntentCount(
    val expectedIntent: String,
    val count: Int
)