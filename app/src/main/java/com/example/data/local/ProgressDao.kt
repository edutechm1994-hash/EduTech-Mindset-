package com.example.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM module_progress ORDER BY updatedAt DESC")
    fun observeAllProgress(): Flow<List<ModuleProgressEntity>>

    @Query("SELECT * FROM module_progress WHERE courseId = :courseId ORDER BY updatedAt DESC")
    fun observeProgressForCourse(courseId: Long): Flow<List<ModuleProgressEntity>>

    @Query("SELECT * FROM module_progress WHERE moduleId = :moduleId")
    fun observeModuleProgress(moduleId: Long): Flow<ModuleProgressEntity?>

    @Query("SELECT * FROM module_progress WHERE courseId = :courseId")
    suspend fun getProgressForCourseOnce(courseId: Long): List<ModuleProgressEntity>

    @Query("SELECT COUNT(*) FROM module_progress WHERE courseId = :courseId AND isCompleted = 1")
    fun observeCompletedModuleCount(courseId: Long): Flow<Int>

    @Upsert
    suspend fun upsertProgress(progress: ModuleProgressEntity)

    @Query("DELETE FROM module_progress WHERE moduleId = :moduleId")
    suspend fun deleteModuleProgress(moduleId: Long)

    @Query("DELETE FROM module_progress WHERE courseId = :courseId")
    suspend fun resetCourseProgress(courseId: Long)
}
