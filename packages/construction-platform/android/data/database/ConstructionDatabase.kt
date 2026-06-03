package com.constructionmanager.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.constructionmanager.data.database.dao.MaterialDao
import com.constructionmanager.data.database.dao.ProjectDao
import com.constructionmanager.data.database.entity.MaterialEntity
import com.constructionmanager.data.database.entity.ProjectEntity
import com.constructionmanager.data.database.entity.DatabaseConverters

@Database(
    entities = [
        ProjectEntity::class,
        MaterialEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class ConstructionDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun materialDao(): MaterialDao
    
    companion object {
        const val DATABASE_NAME = "construction_manager_database"
        
        @Volatile
        private var INSTANCE: ConstructionDatabase? = null
        
        fun getDatabase(context: Context): ConstructionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConstructionDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback : RoomDatabase.Callback() {
        // Add any database initialization logic here
    }
}