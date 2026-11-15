package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val learnerName: String = "",
    val xpPoints: Long = 0L,
    val streakDays: Long = 1L,
    val completedLessons: Long = 0L,
    val preferredTheme: String = "SYSTEM",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class CloudStudyNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val localNoteId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val tag: String = "",
    val accentKey: String = "cyan",
    val isPinned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class CloudCourseProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseTitle: String = "",
    val category: String = "",
    val progressPercent: Long = 0L,
    val isCompleted: Boolean = false,
    val isBookmarked: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

class CloudSyncRepository(
    private val db: FirebaseFirestore,
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfileDoc?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfileDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun upsertUserProfile(
        learnerName: String,
        xpPoints: Long,
        streakDays: Long,
        completedLessons: Long,
        preferredTheme: String,
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val safeName = learnerName.trim().ifBlank { "طالب EduTech المبدع" }.take(80)
        val safeTheme = if (preferredTheme in listOf("SYSTEM", "LIGHT", "DARK")) preferredTheme else "SYSTEM"
        try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "learnerName" to safeName,
                        "xpPoints" to xpPoints.coerceAtLeast(0L),
                        "streakDays" to streakDays.coerceAtLeast(0L),
                        "completedLessons" to completedLessons.coerceAtLeast(0L),
                        "preferredTheme" to safeTheme,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "learnerName" to safeName,
                    "xpPoints" to xpPoints.coerceAtLeast(0L),
                    "streakDays" to streakDays.coerceAtLeast(0L),
                    "completedLessons" to completedLessons.coerceAtLeast(0L),
                    "preferredTheme" to safeTheme,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    fun observeStudyNotes(userId: String): Flow<List<CloudStudyNote>> = flow {
        val path = "users/$userId/notes"
        emitAll(
            db.collection("users").document(userId).collection("notes")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudStudyNote::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun upsertStudyNote(
        noteId: String,
        localNoteId: Long,
        title: String,
        content: String,
        tag: String?,
        accentKey: String,
        isPinned: Boolean,
    ): String {
        val uid = requireUserId()
        val safeDocId = noteId.ifBlank { "note_${localNoteId.coerceAtLeast(1L)}" }
        val docRef = db.collection("users").document(uid).collection("notes").document(safeDocId)
        val safeTitle = title.trim().ifBlank { "ملاحظة" }.take(160)
        val safeContent = content.trim().ifBlank { "—" }.take(5000)
        val safeAccent = accentKey.trim().ifBlank { "cyan" }.take(24)
        val safeTag = tag?.trim()?.takeIf { it.isNotBlank() }?.take(60)
        try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updates = mutableMapOf<String, Any>(
                    "localNoteId" to localNoteId.coerceAtLeast(0L),
                    "title" to safeTitle,
                    "content" to safeContent,
                    "accentKey" to safeAccent,
                    "isPinned" to isPinned,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                if (safeTag != null) updates["tag"] = safeTag
                docRef.update(updates).await()
            } else {
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "localNoteId" to localNoteId.coerceAtLeast(0L),
                    "title" to safeTitle,
                    "content" to safeContent,
                    "accentKey" to safeAccent,
                    "isPinned" to isPinned,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                if (safeTag != null) payload["tag"] = safeTag
                docRef.set(payload).await()
            }
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteStudyNote(noteId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeCourseProgress(userId: String): Flow<List<CloudCourseProgress>> = flow {
        val path = "users/$userId/course_progress"
        emitAll(
            db.collection("users").document(userId).collection("course_progress")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudCourseProgress::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun upsertCourseProgress(
        progressId: String,
        courseTitle: String,
        category: String,
        progressPercent: Int,
        isCompleted: Boolean,
        isBookmarked: Boolean,
    ): String {
        val uid = requireUserId()
        val safeDocId = progressId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "course_1" }
        val docRef = db.collection("users").document(uid).collection("course_progress").document(safeDocId)
        val safeTitle = courseTitle.trim().ifBlank { "مسار" }.take(180)
        val safeCategory = category.trim().ifBlank { "عام" }.take(80)
        val clampedProgress = progressPercent.coerceIn(0, 100).toLong()
        try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "courseTitle" to safeTitle,
                        "category" to safeCategory,
                        "progressPercent" to clampedProgress,
                        "isCompleted" to isCompleted,
                        "isBookmarked" to isBookmarked,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "courseTitle" to safeTitle,
                    "category" to safeCategory,
                    "progressPercent" to clampedProgress,
                    "isCompleted" to isCompleted,
                    "isBookmarked" to isBookmarked,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                docRef.set(payload).await()
            }
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
