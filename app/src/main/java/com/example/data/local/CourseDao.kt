package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY isBookmarked DESC, createdAt DESC")
    fun observeAllCourses(): Flow<List<CourseEntity>>

    @Transaction
    @Query("SELECT * FROM courses ORDER BY isBookmarked DESC, createdAt DESC")
    fun observeCoursesWithModules(): Flow<List<CourseWithModules>>

    @Transaction
    @Query(
        """
        SELECT * FROM courses
        WHERE title LIKE '%' || :query || '%'
           OR subtitle LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR lessonContent LIKE '%' || :query || '%'
           OR id IN (
               SELECT DISTINCT courseId FROM course_modules
               WHERE title LIKE '%' || :query || '%'
                  OR summary LIKE '%' || :query || '%'
                  OR contentBody LIKE '%' || :query || '%'
           )
        ORDER BY isBookmarked DESC, createdAt DESC
        """
    )
    fun searchCoursesWithModules(query: String): Flow<List<CourseWithModules>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun observeCourseWithModules(courseId: Long): Flow<CourseWithModules?>

    @Query("SELECT * FROM courses WHERE category = :category ORDER BY isBookmarked DESC, createdAt DESC")
    fun observeCoursesByCategory(category: String): Flow<List<CourseEntity>>

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>): List<Long>

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: Long)
}
