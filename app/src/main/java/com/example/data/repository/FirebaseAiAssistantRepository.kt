package com.example.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "FirebaseAiAssistant"

class FirebaseAiAssistantRepository : AssistantRepository {
    override val isCloudAiEnabled: Boolean = true

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    أنت المساعد الذكي والشخصي الرسمي لمنصة "EduTech Mindset — عقلية التعليم والتكنولوجيا" بإدارة الأستاذ باسل أشرف الفقعاوي.
                    مهمتك: مساعدة المعلمين والطلاب بالعربية الفصحى الواضحة وبأسلوب تربوي عملي وموجز ومنظم.
                    استند عند الاقتراح على بيانات المتعلّم ومساراته وملاحظاته المرفقة في السياق.
                    """.trimIndent()
                )
            },
        )
    }

    override suspend fun ask(
        prompt: String,
        snapshot: LocalLearningSnapshot,
    ): AssistantReply {
        val enrichedPrompt = buildEnrichedPrompt(prompt, snapshot)
        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(enrichedPrompt)
            }
            val text = response.text?.trim().orEmpty()
            if (text.isBlank()) {
                throw IllegalStateException("أعاد النموذج استجابة فارغة. حاول مرة أخرى.")
            }
            AssistantReply(
                text = text,
                isSampleNotice = false,
                suggestedCourseId = matchSuggestedCourseId(prompt, text, snapshot),
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Firebase AI generation timed out", e)
            throw IllegalStateException("انتهت مهلة الاتصال بالمساعد الذكي (60 ثانية). يُرجى المحاولة مجدداً.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed", e)
            throw e
        }
    }

    fun askStream(
        prompt: String,
        snapshot: LocalLearningSnapshot,
    ): Flow<String> = flow {
        val enrichedPrompt = buildEnrichedPrompt(prompt, snapshot)
        withTimeout(60_000L) {
            model.generateContentStream(enrichedPrompt).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    emit(chunkText)
                }
            }
        }
    }.catch { e ->
        if (e is CancellationException && e !is TimeoutCancellationException) throw e
        Log.e(TAG, "Firebase AI streaming failed", e)
        throw e
    }

    private fun buildEnrichedPrompt(prompt: String, snapshot: LocalLearningSnapshot): String {
        val coursesSummary = snapshot.courses.take(6).joinToString("\n") { item ->
            "- ${item.course.title} (${item.course.category}) — التقدم: ${item.computedProgressPercent}% — الوحدات: ${item.orderedModules.joinToString("، ") { it.module.title }}"
        }
        val notesSummary = snapshot.notes.take(4).joinToString("\n") { note ->
            "- ${note.title}: ${note.content.take(120)}"
        }
        return """
            سياق المتعلّم الحالي:
            - الاسم: ${snapshot.stats.learnerName}
            - نقاط الخبرة: ${snapshot.stats.xpPoints} XP • الدروس المنجزة: ${snapshot.stats.completedLessons}
            - المسارات المتاحة:
            $coursesSummary
            - أحدث الملاحظات:
            ${notesSummary.ifBlank { "لا توجد ملاحظات بعد" }}

            طلب المستخدم:
            ${prompt.trim()}
        """.trimIndent()
    }

    private fun matchSuggestedCourseId(
        prompt: String,
        replyText: String,
        snapshot: LocalLearningSnapshot,
    ): Long? {
        return snapshot.courses.firstOrNull { item ->
            prompt.contains(item.course.title, ignoreCase = true) ||
                replyText.contains(item.course.title, ignoreCase = true) ||
                item.orderedModules.any { prompt.contains(it.module.title, ignoreCase = true) }
        }?.course?.id ?: snapshot.courses.firstOrNull { !it.course.isCompleted }?.course?.id
    }
}
