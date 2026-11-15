package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {
    @Query("SELECT * FROM course_modules WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun observeModulesForCourse(courseId: Long): Flow<List<ModuleEntity>>

    @Transaction
    @Query("SELECT * FROM course_modules WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun observeModulesWithProgress(courseId: Long): Flow<List<ModuleWithProgress>>

    @Transaction
    @Query(
        """
        SELECT * FROM course_modules
        WHERE title LIKE '%' || :query || '%'
           OR summary LIKE '%' || :query || '%'
           OR contentBody LIKE '%' || :query || '%'
        ORDER BY courseId ASC, orderIndex ASC
        """
    )
    fun searchModulesWithProgress(query: String): Flow<List<ModuleWithProgress>>

    @Query("SELECT * FROM course_modules WHERE courseId = :courseId ORDER BY orderIndex ASC")
    suspend fun getModulesForCourseOnce(courseId: Long): List<ModuleEntity>

    @Query("SELECT COUNT(*) FROM course_modules")
    suspend fun getModuleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: ModuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>): List<Long>

    @Update
    suspend fun updateModule(module: ModuleEntity)

    @Query("DELETE FROM course_modules WHERE id = :moduleId")
    suspend fun deleteModuleById(moduleId: Long)
}
