package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EduTechDao {

    // Media Assets
    @Query("SELECT * FROM media_assets ORDER BY isBookmarked DESC, createdAt DESC")
    fun observeAllMedia(): Flow<List<MediaAssetEntity>>

    @Query("SELECT COUNT(*) FROM media_assets")
    suspend fun getMediaCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaAssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(items: List<MediaAssetEntity>)

    @Update
    suspend fun updateMedia(item: MediaAssetEntity)

    @Query("DELETE FROM media_assets WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: Long)

    // Study Notes
    @Query("SELECT * FROM study_notes ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAllNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT COUNT(*) FROM study_notes")
    suspend fun getNoteCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<StudyNoteEntity>)

    @Update
    suspend fun updateNote(note: StudyNoteEntity)

    @Query("DELETE FROM study_notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    // Learner Stats
    @Query("SELECT * FROM learner_stats WHERE id = 1")
    fun observeLearnerStats(): Flow<LearnerStatsEntity?>

    @Query("SELECT * FROM learner_stats WHERE id = 1")
    suspend fun getLearnerStatsOnce(): LearnerStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLearnerStats(stats: LearnerStatsEntity)
}
