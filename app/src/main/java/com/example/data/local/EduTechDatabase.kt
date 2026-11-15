package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CourseEntity::class,
        ModuleEntity::class,
        ModuleProgressEntity::class,
        MediaAssetEntity::class,
        StudyNoteEntity::class,
        LearnerStatsEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class EduTechDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun moduleDao(): ModuleDao
    abstract fun progressDao(): ProgressDao
    abstract fun eduTechDao(): EduTechDao

    companion object {
        @Volatile
        private var INSTANCE: EduTechDatabase? = null

        fun getInstance(context: Context): EduTechDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EduTechDatabase::class.java,
                    "edutech_mindset_local.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
