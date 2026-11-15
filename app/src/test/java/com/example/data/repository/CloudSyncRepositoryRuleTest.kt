package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class CloudSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun `authenticated user can sync profile notes and course progress`() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = CloudSyncRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertUserProfile(
                learnerName = "أحمد باسل",
                xpPoints = 250L,
                streakDays = 3L,
                completedLessons = 2L,
                preferredTheme = "DARK",
            )
        }

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(uid).first { it != null }
        }
        assertEquals("أحمد باسل", profile?.learnerName)
        assertEquals(250L, profile?.xpPoints)

        val uniqueNoteId = "note_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertStudyNote(
                noteId = uniqueNoteId,
                localNoteId = 7L,
                title = "ملاحظة سحابية",
                content = "محتوى متزامن مع Firestore",
                tag = "اختبار",
                accentKey = "cyan",
                isPinned = true,
            )
        }

        val notes = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeStudyNotes(uid).first { list -> list.any { it.id == uniqueNoteId } }
        }
        assertTrue(notes.any { it.title == "ملاحظة سحابية" })

        val uniqueProgressId = "prog_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertCourseProgress(
                progressId = uniqueProgressId,
                courseTitle = "الذكاء الاصطناعي في التعليم",
                category = "الذكاء الاصطناعي",
                progressPercent = 75,
                isCompleted = false,
                isBookmarked = true,
            )
        }

        val progressList = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCourseProgress(uid).first { list -> list.any { it.id == uniqueProgressId } }
        }
        assertTrue(progressList.any { it.progressPercent == 75L })
    }

    @Test
    fun `cross-user note access fails with PERMISSION_DENIED`() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = CloudSyncRepository(firestore)
        val uniqueNoteId = "note_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.upsertStudyNote(
                noteId = uniqueNoteId,
                localNoteId = 99L,
                title = "ملاحظة خاصة",
                content = "بيانات سرية",
                tag = null,
                accentKey = "gold",
                isPinned = false,
            )
        }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("notes").document(uniqueNoteId)
                    .get()
                    .await()
            }
            fail("Expected PERMISSION_DENIED for cross-user note access")
        } catch (e: Exception) {
            val fsError = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
        }
    }

    @Test
    fun `unauthenticated user observing notes fails with PERMISSION_DENIED`() = runBlocking {
        auth.signOut()
        val repo = CloudSyncRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeStudyNotes("some_user_123").first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated caller")
        } catch (e: Exception) {
            val fsError = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_edutech@test.com"
        const val BOB_EMAIL = "bob_edutech@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
