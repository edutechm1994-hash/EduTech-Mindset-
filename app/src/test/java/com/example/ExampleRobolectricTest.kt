package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CourseEntity
import com.example.data.local.EduTechDatabase
import com.example.data.local.MediaKind
import com.example.data.local.ModuleEntity
import com.example.data.local.ModuleProgressEntity
import com.example.data.local.StudyNoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("EduTech Mindset", appName)
    }

    @Test
    fun `room schema persists courses modules and progress with cascade delete`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, EduTechDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val courseDao = db.courseDao()
        val moduleDao = db.moduleDao()
        val progressDao = db.progressDao()
        val eduTechDao = db.eduTechDao()

        val courseId = courseDao.insertCourse(
            CourseEntity(
                title = "مسار تجريبي",
                subtitle = "إدارة الأستاذ باسل الفقعاوي",
                category = "القيادة الرقمية",
                description = "اختبار التخزين المحلي",
                lessonContent = "محتوى الدرس",
                durationMinutes = 15,
            )
        )

        val moduleId1 = moduleDao.insertModule(
            ModuleEntity(
                courseId = courseId,
                orderIndex = 1,
                title = "الوحدة الأولى",
                summary = "ملخص الوحدة الأولى",
                contentBody = "محتوى تفصيلي",
                durationMinutes = 7,
                mediaType = MediaKind.VIDEO.name,
            )
        )
        val moduleId2 = moduleDao.insertModule(
            ModuleEntity(
                courseId = courseId,
                orderIndex = 2,
                title = "الوحدة الثانية",
                summary = "ملخص الوحدة الثانية",
                contentBody = "محتوى تطبيقي",
                durationMinutes = 8,
                mediaType = MediaKind.DOCUMENT.name,
            )
        )

        progressDao.upsertProgress(
            ModuleProgressEntity(
                moduleId = moduleId1,
                courseId = courseId,
                progressPercent = 100,
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
            )
        )

        val relation = courseDao.observeCourseWithModules(courseId).first()!!
        assertEquals(2, relation.orderedModules.size)
        assertEquals(50, relation.computedProgressPercent)
        assertTrue(relation.orderedModules.first().progress?.isCompleted == true)
        assertEquals(null, relation.orderedModules[1].progress)
        assertTrue(moduleId2 > 0)

        eduTechDao.insertNote(
            StudyNoteEntity(
                title = "ملاحظة محلية",
                content = "محتوى محفوظ في قاعدة البيانات",
            )
        )
        assertEquals(1, eduTechDao.observeAllNotes().first().size)

        // Verify CASCADE delete removes child modules & progress
        courseDao.deleteCourseById(courseId)
        assertEquals(0, moduleDao.getModulesForCourseOnce(courseId).size)
        assertEquals(0, progressDao.getProgressForCourseOnce(courseId).size)

        db.close()
    }

    @Test
    fun `room search filters courses and modules by title and keyword`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, EduTechDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val courseDao = db.courseDao()
        val moduleDao = db.moduleDao()

        val aiCourseId = courseDao.insertCourse(
            CourseEntity(
                title = "أساسيات الذكاء الاصطناعي",
                subtitle = "إدارة الأستاذ باسل الفقعاوي",
                category = "الذكاء الاصطناعي",
                description = "مفاهيم التعلم الآلي وتطبيقاته في التعليم",
                lessonContent = "مقدمة في الشبكات العصبية",
                durationMinutes = 30,
            )
        )
        val mediaCourseId = courseDao.insertCourse(
            CourseEntity(
                title = "إنتاج الوسائط التفاعلية",
                subtitle = "تصميم المحتوى الرقمي",
                category = "الوسائط التفاعلية",
                description = "المونتاج والصوتيات التعليمية",
                lessonContent = "معايير الفيديو والصوت",
                durationMinutes = 25,
            )
        )

        moduleDao.insertModule(
            ModuleEntity(
                courseId = aiCourseId,
                orderIndex = 1,
                title = "هندسة الأوامر الذكية",
                summary = "صياغة البرومبت للمعلمين",
                contentBody = "أمثلة عملية في التخطيط التربوي",
                durationMinutes = 12,
                mediaType = MediaKind.VIDEO.name,
            )
        )
        moduleDao.insertModule(
            ModuleEntity(
                courseId = mediaCourseId,
                orderIndex = 1,
                title = "هندسة الصوت التعليمي",
                summary = "تسجيل البودكاست الصفي",
                contentBody = "تقنيات إزالة الضوضاء",
                durationMinutes = 10,
                mediaType = MediaKind.AUDIO.name,
            )
        )

        val coursesByKeyword = courseDao.searchCoursesWithModules("البرومبت").first()
        assertEquals(1, coursesByKeyword.size)
        assertEquals(aiCourseId, coursesByKeyword.first().course.id)

        val modulesByTitle = moduleDao.searchModulesWithProgress("البودكاست").first()
        assertEquals(1, modulesByTitle.size)
        assertEquals("هندسة الصوت التعليمي", modulesByTitle.first().module.title)

        val assistantRepo = com.example.data.repository.LocalPlaceholderAssistantRepository()
        val reply = assistantRepo.ask(
            prompt = "اقترح لي درساً",
            snapshot = com.example.data.repository.LocalLearningSnapshot(
                stats = com.example.data.local.LearnerStatsEntity(xpPoints = 120),
                courses = courseDao.observeCoursesWithModules().first(),
                notes = emptyList(),
            ),
        )
        assertTrue(reply.isSampleNotice)
        assertTrue(reply.suggestedCourseId != null)
        assertTrue(reply.text.contains("رد تجريبي محلي"))

        db.close()
    }

    @Test
    fun `tts formatter builds course description and module summary narration cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val course = CourseEntity(
            title = "الذكاء الاصطناعي في التعليم",
            subtitle = "الأستاذ باسل الفقعاوي",
            category = "الذكاء الاصطناعي",
            description = "مسار تطبيقي لبناء الدروس الذكية",
            lessonContent = "محتوى",
            durationMinutes = 45,
        )
        val module = ModuleEntity(
            courseId = 1L,
            orderIndex = 1,
            title = "أساسيات البرومبت",
            summary = "تعلم صياغة الأوامر التعليمية الفعالة",
            contentBody = "أمثلة تطبيقية لتصميم الاختبارات",
            durationMinutes = 15,
            mediaType = MediaKind.VIDEO.name,
        )

        val courseNarration = com.example.ui.components.CourseAudioFormatter.formatCourseNarration(course)
        assertTrue(courseNarration.contains("الذكاء الاصطناعي في التعليم"))
        assertTrue(courseNarration.contains("مسار تطبيقي لبناء الدروس الذكية"))
        assertTrue(courseNarration.contains("45 دقيقة"))

        val moduleSummary = com.example.ui.components.CourseAudioFormatter.formatModuleSummaryNarration(module)
        assertTrue(moduleSummary.contains("أساسيات البرومبت"))
        assertTrue(moduleSummary.contains("تعلم صياغة الأوامر التعليمية الفعالة"))

        val moduleFull = com.example.ui.components.CourseAudioFormatter.formatModuleFullNarration(module)
        assertTrue(moduleFull.contains("أمثلة تطبيقية لتصميم الاختبارات"))

        val ttsEngine = com.example.ui.components.AccessibilityTtsEngine(context)
        ttsEngine.setSpeechRate(1.25f)
        assertEquals(1.25f, ttsEngine.state.value.speechRate, 0.01f)
        ttsEngine.speak("mod_1", moduleSummary)
        ttsEngine.stop()
        assertEquals(null, ttsEngine.state.value.activeItemId)
        ttsEngine.release()
    }

    @Test
    fun `edutech blueprint builder generates teacher and student plans with website link`() {
        val teacherPlan = com.example.ui.screens.EduTechBlueprintBuilder.buildBlueprint(
            audience = com.example.ui.screens.ToolkitAudience.TEACHER,
            toolName = "تحضير درس ذكي",
            subjectOrTopic = "الذكاء الاصطناعي في الصف",
            stage = "المرحلة الثانوية",
        )
        assertTrue(teacherPlan.contains("EduTech Mindset"))
        assertTrue(teacherPlan.contains("الذكاء الاصطناعي في الصف"))

        val studentPlan = com.example.ui.screens.EduTechBlueprintBuilder.buildBlueprint(
            audience = com.example.ui.screens.ToolkitAudience.STUDENT,
            toolName = "خطة مذاكرة ذكية",
            subjectOrTopic = "الكيمياء العضوية",
            stage = "التوجيهي",
        )
        assertTrue(studentPlan.contains("الكيمياء العضوية"))
        assertTrue(studentPlan.contains("25 دقيقة"))
        assertEquals("https://edutech-mindset.pages.dev", com.example.ui.screens.EDUTECH_WEBSITE_URL)
    }

    @Test
    fun `datastore persists theme mode and dynamic color preferences`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefsRepo = com.example.data.local.UserPreferencesRepository(context)

        prefsRepo.setThemeMode(com.example.data.local.ThemeMode.LIGHT)
        prefsRepo.setDynamicColor(false)
        var saved = prefsRepo.preferencesFlow.first()
        assertEquals(com.example.data.local.ThemeMode.LIGHT, saved.themeMode)
        assertEquals(false, saved.dynamicColorEnabled)
        assertEquals(false, saved.resolveDarkTheme(isSystemDark = true))

        prefsRepo.setThemeMode(com.example.data.local.ThemeMode.SYSTEM)
        prefsRepo.setDynamicColor(true)
        saved = prefsRepo.preferencesFlow.first()
        assertEquals(com.example.data.local.ThemeMode.SYSTEM, saved.themeMode)
        assertEquals(true, saved.dynamicColorEnabled)
        assertEquals(true, saved.resolveDarkTheme(isSystemDark = true))
        assertEquals(false, saved.resolveDarkTheme(isSystemDark = false))

        prefsRepo.setDarkTheme(true)
        saved = prefsRepo.preferencesFlow.first()
        assertEquals(com.example.data.local.ThemeMode.DARK, saved.themeMode)
        assertEquals(true, saved.resolveDarkTheme(isSystemDark = false))
    }
}
