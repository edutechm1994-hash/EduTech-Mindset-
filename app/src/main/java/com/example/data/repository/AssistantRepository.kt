package com.example.data.repository

import com.example.data.local.CourseWithModules
import com.example.data.local.LearnerStatsEntity
import com.example.data.local.StudyNoteEntity
import kotlinx.coroutines.delay

data class LocalLearningSnapshot(
    val stats: LearnerStatsEntity,
    val courses: List<CourseWithModules>,
    val notes: List<StudyNoteEntity>,
)

data class AssistantReply(
    val text: String,
    val isSampleNotice: Boolean = true,
    val suggestedCourseId: Long? = null,
)

/**
 * Single abstraction boundary for Personal Assistant responses.
 * Currently backed by [LocalPlaceholderAssistantRepository] because Firebase AI Logic
 * was declined by the user; can be swapped to Firebase.ai without changing UI or ViewModel.
 */
interface AssistantRepository {
    val isCloudAiEnabled: Boolean
    suspend fun ask(prompt: String, snapshot: LocalLearningSnapshot): AssistantReply
}

class LocalPlaceholderAssistantRepository : AssistantRepository {
    override val isCloudAiEnabled: Boolean = false

    override suspend fun ask(
        prompt: String,
        snapshot: LocalLearningSnapshot,
    ): AssistantReply {
        delay(350)
        val clean = prompt.trim()
        val lower = clean.lowercase()

        val matchedCourse = snapshot.courses.firstOrNull { item ->
            item.course.title.contains(clean, ignoreCase = true) ||
                item.course.category.contains(clean, ignoreCase = true) ||
                item.orderedModules.any { it.module.title.contains(clean, ignoreCase = true) }
        }

        val completedCount = snapshot.courses.count { it.course.isCompleted }
        val nextCourse = snapshot.courses.firstOrNull { !it.course.isCompleted }
        val nextModule = nextCourse?.orderedModules?.firstOrNull { it.progress?.isCompleted != true }?.module

        val body = when {
            matchedCourse != null -> {
                val mods = matchedCourse.orderedModules.joinToString("\n") { m ->
                    val doneMark = if (m.progress?.isCompleted == true) "✓" else "•"
                    "$doneMark ${m.module.title} (${m.module.durationMinutes} د)"
                }
                """
                [رد تجريبي محلي] وجدت مساراً مطابقاً في مكتبتك:
                «${matchedCourse.course.title}» (${matchedCourse.computedProgressPercent}% مكتمل).
                
                الوحدات المتاحة:
                $mods
                """.trimIndent()
            }

            lower.contains("تقدم") || lower.contains("إنجاز") || lower.contains("xp") || lower.contains("نقاط") -> {
                """
                [رد تجريبي محلي] ملخص تقدمك الحالي يا ${snapshot.stats.learnerName}:
                • نقاط الخبرة: ${snapshot.stats.xpPoints} XP
                • المسارات المكتملة: $completedCount من ${snapshot.courses.size}
                • الملاحظات المحفوظة: ${snapshot.notes.size} ملاحظة
                ${if (nextCourse != null) "\nالخطوة المقترحة التالية: متابعة «${nextCourse.course.title}»." else "\nرائع! أتممت جميع المسارات الحالية."}
                """.trimIndent()
            }

            lower.contains("خطة") || lower.contains("اقترح") || lower.contains("ابدأ") || lower.contains("درس") -> {
                if (nextCourse != null) {
                    """
                    [رد تجريبي محلي] خطة تعلّم مقترحة وفق منهجية الأستاذ باسل الفقعاوي:
                    1. افتح مسار «${nextCourse.course.title}».
                    ${if (nextModule != null) "2. ابدأ بالوحدة: «${nextModule.title}» (${nextModule.durationMinutes} دقيقة)." else "2. راجع محتوى المسار."}
                    3. دوّن خلاصة من سطرين في دفتر ملاحظاتك لتثبيت المفهوم وكسب +15 XP.
                    """.trimIndent()
                } else {
                    "[رد تجريبي محلي] أتممت جميع المسارات! يمكنك إضافة مسار مخصص أو استيراد وسائط جديدة."
                }
            }

            lower.contains("ملاحظ") || lower.contains("لخص") -> {
                val recent = snapshot.notes.take(2).joinToString("\n") { "• ${it.title}: ${it.content}" }
                if (recent.isNotBlank()) {
                    "[رد تجريبي محلي] أحدث ملاحظاتك المحفوظة محلياً:\n$recent"
                } else {
                    "[رد تجريبي محلي] لا توجد ملاحظات محفوظة بعد. جرّب إضافة ملاحظتك الأولى في قسم ملاحظاتي."
                }
            }

            else -> {
                """
                [رد تجريبي محلي] أهلاً بك في المساعد الشخصي لمنصة EduTech Mindset بإدارة الأستاذ باسل الفقعاوي.
                لقد استلمت رسالتك: «$clean».
                أستطيع مساعدتك محلياً في متابعة تقدمك، اقتراح الوحدة التالية، أو إرشادك إلى مسار من مكتبتك (${snapshot.courses.size} مسار متاح).
                لتفعيل الإجابات الذكية التوليدية الكاملة، يتطلب الأمر تفعيل Firebase AI Logic.
                """.trimIndent()
            }
        }

        return AssistantReply(
            text = body,
            isSampleNotice = true,
            suggestedCourseId = matchedCourse?.course?.id ?: nextCourse?.course?.id,
        )
    }
}
