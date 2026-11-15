package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.R
import com.example.data.local.AppPreferences
import com.example.data.local.CourseDao
import com.example.data.local.CourseEntity
import com.example.data.local.CourseWithModules
import com.example.data.local.EduTechDao
import com.example.data.local.LearnerStatsEntity
import com.example.data.local.LocalMediaStorageManager
import com.example.data.local.MediaAssetEntity
import com.example.data.local.MediaKind
import com.example.data.local.ModuleDao
import com.example.data.local.ModuleEntity
import com.example.data.local.ModuleProgressEntity
import com.example.data.local.ProgressDao
import com.example.data.local.StudyNoteEntity
import com.example.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class EduTechRepository(
    private val context: Context,
    private val courseDao: CourseDao,
    private val moduleDao: ModuleDao,
    private val progressDao: ProgressDao,
    private val dao: EduTechDao,
    private val prefsRepo: UserPreferencesRepository,
    private val mediaStorage: LocalMediaStorageManager,
) {
    val courses: Flow<List<CourseEntity>> = courseDao.observeAllCourses()
    val coursesWithModules: Flow<List<CourseWithModules>> = courseDao.observeCoursesWithModules()
    val mediaAssets: Flow<List<MediaAssetEntity>> = dao.observeAllMedia()
    val studyNotes: Flow<List<StudyNoteEntity>> = dao.observeAllNotes()
    val learnerStats: Flow<LearnerStatsEntity?> = dao.observeLearnerStats()
    val preferences: Flow<AppPreferences> = prefsRepo.preferencesFlow

    fun observeCourseWithModules(courseId: Long): Flow<CourseWithModules?> =
        courseDao.observeCourseWithModules(courseId)

    fun searchCoursesWithModules(query: String): Flow<List<CourseWithModules>> =
        if (query.isBlank()) courseDao.observeCoursesWithModules()
        else courseDao.searchCoursesWithModules(query.trim())

    fun searchModulesWithProgress(query: String): Flow<List<com.example.data.local.ModuleWithProgress>> =
        moduleDao.searchModulesWithProgress(query.trim())

    suspend fun ensureSeeded() {
        val pkg = context.packageName
        val introVideoUri = "android.resource://$pkg/${R.raw.edutech_intro_video}"
        val podcastAudioUri = "android.resource://$pkg/${R.raw.edutech_podcast_audio}"
        val heroDrawableUri = "android.resource://$pkg/${R.drawable.img_welcome_hero_1794764865345}"
        val futureTechDrawableUri = "android.resource://$pkg/${R.drawable.img_track_future_tech_1794764866174}"
        val mediaLabDrawableUri = "android.resource://$pkg/${R.drawable.img_track_media_lab_1794764865682}"

        if (dao.getLearnerStatsOnce() == null) {
            dao.upsertLearnerStats(
                LearnerStatsEntity(
                    id = 1,
                    learnerName = "طالب EduTech المبدع",
                    xpPoints = 150,
                    streakDays = 1,
                    completedLessons = 0,
                )
            )
        }

        if (courseDao.getCourseCount() == 0) {
            val insertedIds = courseDao.insertCourses(
                listOf(
                    CourseEntity(
                        title = "منهجية عقلية التعليم والتكنولوجيا",
                        subtitle = "مدخل استراتيجي مع الأستاذ باسل الفقعاوي",
                        category = "القيادة الرقمية",
                        description = "كيف ننتقل من مجرد استخدام الأدوات الرقمية إلى بناء عقلية تعليمية ابتكارية تصنع الأثر داخل الصف وخارجه.",
                        lessonContent = """
                            • المحور الأول: الفرق بين رقمنة المحتوى والتحول الرقمي الحقيقي في التعليم.
                            • المحور الثاني: مثلث EduTech Mindset (المعلّم الميسّر + التقنية الهادفة + التعلّم النشط).
                            • المحور الثالث: تصميم تجارب تعلّم مرنة تراعي الفروق الفردية وتحفّز الفضول المعرفي.
                            • تطبيق عملي: اختر درساً تقليدياً وأعد صياغته باستخدام وسائط تفاعلية وأسئلة استكشافية.
                        """.trimIndent(),
                        durationMinutes = 18,
                        progressPercent = 33,
                        isBookmarked = true,
                        mediaType = MediaKind.VIDEO.name,
                        mediaUri = introVideoUri,
                        coverKey = "welcome_hero",
                    ),
                    CourseEntity(
                        title = "الذكاء الاصطناعي التوليدي للمعلمين",
                        subtitle = "تصميم المحتوى والتقييمات الذكية بأمان",
                        category = "الذكاء الاصطناعي",
                        description = "دليل عملي لتوظيف أدوات الذكاء الاصطناعي في بناء الخطط الدراسية، تبسيط المفاهيم، وتوليد الأنشطة الصفية.",
                        lessonContent = """
                            • هندسة الأوامر التعليمية (Educational Prompting) بدقة تربوية عالية.
                            • توليد أمثلة واقعية وتدريبات متدرجة الصعوبة لكل مستوى دراسي.
                            • أخلاقيات الذكاء الاصطناعي وحماية خصوصية الطلبة في البيئة الرقمية.
                            • ورشة عمل: أنشئ ملخصاً تفاعلياً واحفظه في دفتر ملاحظاتك المحلي.
                        """.trimIndent(),
                        durationMinutes = 25,
                        progressPercent = 0,
                        mediaType = MediaKind.IMAGE.name,
                        mediaUri = futureTechDrawableUri,
                        coverKey = "future_tech",
                    ),
                    CourseEntity(
                        title = "استوديو إنتاج الوسائط التعليمية",
                        subtitle = "الفيديو التفاعلي والبودكاست التعليمي",
                        category = "الوسائط التفاعلية",
                        description = "مهارات إنتاج مقاطع فيديو تعليمية قصيرة، تسجيلات صوتية واضحة، وإنفوجرافيك بصري يجذب انتباه المتعلم.",
                        lessonContent = """
                            • قاعدة الدقائق الست: لماذا يتفوق الميكرو-فيديو في ترسيخ المعلومة؟
                            • تصميم الصوت التعليمي (Podcasting) وبناء سرد قصصي شيّق.
                            • التناسق البصري والألوان المريحة للعين في العروض والشرائح.
                            • تجربة الاستوديو: استمع لنموذج البودكاست أو استورد مقاطعك من جهازك!
                        """.trimIndent(),
                        durationMinutes = 22,
                        progressPercent = 0,
                        mediaType = MediaKind.AUDIO.name,
                        mediaUri = podcastAudioUri,
                        coverKey = "media_lab",
                    ),
                    CourseEntity(
                        title = "التفكير التصميمي وبناء المشاريع",
                        subtitle = "تحويل التحديات الصفية إلى حلول إبداعية",
                        category = "تصميم التعليم",
                        description = "تطبيق خطوات التفكير التصميمي الخمس (التعاطف، التحديد، التفكير، النموذج الأولي، الاختبار) داخل البيئة المدرسية.",
                        lessonContent = """
                            • كيف يفهم المعلم احتياجات المتعلمين بعمق قبل اختيار الأداة التقنية؟
                            • جلسات العصف الذهني السريع لبناء حلول تعليمية بأبسط الموارد.
                            • اختبار النماذج الأولية وجمع التغذية الراجعة البنّاءة من الطلبة.
                        """.trimIndent(),
                        durationMinutes = 16,
                        progressPercent = 0,
                        mediaType = MediaKind.IMAGE.name,
                        mediaUri = heroDrawableUri,
                        coverKey = "welcome_hero",
                    ),
                )
            )

            if (moduleDao.getModuleCount() == 0 && insertedIds.size >= 4) {
                val c1 = insertedIds[0]
                val c2 = insertedIds[1]
                val c3 = insertedIds[2]
                val c4 = insertedIds[3]

                val m1Ids = moduleDao.insertModules(
                    listOf(
                        ModuleEntity(
                            courseId = c1,
                            orderIndex = 1,
                            title = "الوحدة ١: الرؤية والرسالة التربوية",
                            summary = "مقدمة موشن جرافك حول التحول من التلقين إلى التمكين الرقمي.",
                            contentBody = "التركيز على تغيير طريقة التفكير قبل اختيار التطبيقات، وبناء بيئة تعليمية محفّزة.",
                            durationMinutes = 5,
                            mediaType = MediaKind.VIDEO.name,
                            mediaUri = introVideoUri,
                        ),
                        ModuleEntity(
                            courseId = c1,
                            orderIndex = 2,
                            title = "الوحدة ٢: مثلث EduTech Mindset",
                            summary = "التوازن بين المعلم الميسّر والتقنية الهادفة والتعلم النشط.",
                            contentBody = "كل تقنية تُضاف للحصة يجب أن تخدم هدفاً تعليمياً واضحاً وتزيد من تفاعل الطالب.",
                            durationMinutes = 7,
                            mediaType = MediaKind.DOCUMENT.name,
                        ),
                        ModuleEntity(
                            courseId = c1,
                            orderIndex = 3,
                            title = "الوحدة ٣: ورشة إعادة تصميم درس",
                            summary = "تطبيق عملي خطوة بخطوة لتحويل درس تقليدي إلى تجربة تفاعلية.",
                            contentBody = "اختر مفهوماً أساسياً، أضف سؤالاً محفزاً ووسيلة بصرية، ثم قِس الفهم بتغذية راجعة فورية.",
                            durationMinutes = 6,
                            mediaType = MediaKind.AUDIO.name,
                            mediaUri = podcastAudioUri,
                        ),
                    )
                )
                if (m1Ids.isNotEmpty()) {
                    progressDao.upsertProgress(
                        ModuleProgressEntity(
                            moduleId = m1Ids.first(),
                            courseId = c1,
                            progressPercent = 100,
                            isCompleted = true,
                            completedAt = System.currentTimeMillis(),
                        )
                    )
                }

                moduleDao.insertModules(
                    listOf(
                        ModuleEntity(
                            courseId = c2,
                            orderIndex = 1,
                            title = "الوحدة ١: هندسة الأوامر التربوية",
                            summary = "كيف تصيغ أوامر دقيقة لتوليد خطط دراسية متكاملة.",
                            contentBody = "تحديد الفئة العمرية، الهدف السلوكي، أسلوب التقييم، ومراعاة الفروق الفردية.",
                            durationMinutes = 9,
                            mediaType = MediaKind.IMAGE.name,
                            mediaUri = futureTechDrawableUri,
                        ),
                        ModuleEntity(
                            courseId = c2,
                            orderIndex = 2,
                            title = "الوحدة ٢: التقييم الذكي وأخلاقيات الخصوصية",
                            summary = "معايير حماية بيانات الطلبة والتحقق من دقة المحتوى.",
                            contentBody = "عدم مشاركة بيانات الطلبة الشخصية واستخدام الذكاء الاصطناعي كمساعد لا كبديل عن المعلم.",
                            durationMinutes = 8,
                            mediaType = MediaKind.DOCUMENT.name,
                        ),
                        ModuleEntity(
                            courseId = c2,
                            orderIndex = 3,
                            title = "الوحدة ٣: بناء بنك أنشطة محلي",
                            summary = "توليد تدريبات متدرجة وحفظها للاستخدام الصفي المباشر.",
                            contentBody = "صمّم ثلاثة مستويات من التحديات وشاركها في دفترك التعليمي المحلي.",
                            durationMinutes = 8,
                            mediaType = MediaKind.DOCUMENT.name,
                        ),
                    )
                )

                moduleDao.insertModules(
                    listOf(
                        ModuleEntity(
                            courseId = c3,
                            orderIndex = 1,
                            title = "الوحدة ١: قاعدة الميكرو-فيديو التعليمي",
                            summary = "إنتاج مقاطع قصيرة عالية التركيز والتأثير البصري.",
                            contentBody = "التحضير لنص واضح من 3 دقائق مع أمثلة بصرية جذابة.",
                            durationMinutes = 8,
                            mediaType = MediaKind.VIDEO.name,
                            mediaUri = introVideoUri,
                        ),
                        ModuleEntity(
                            courseId = c3,
                            orderIndex = 2,
                            title = "الوحدة ٢: البودكاست الصوتي والوضوح",
                            summary = "تسجيل صوتي احترافي وسرد قصصي يجذب المستمع.",
                            contentBody = "استخدم نبرة حيوية وتوقفات مدروسة واستمع للملف الصوتي المرفق.",
                            durationMinutes = 7,
                            mediaType = MediaKind.AUDIO.name,
                            mediaUri = podcastAudioUri,
                        ),
                        ModuleEntity(
                            courseId = c3,
                            orderIndex = 3,
                            title = "الوحدة ٣: التصميم البصري للوسائط",
                            summary = "تنسيق الألوان والإنفوجرافيك داخل بيئة التعلّم.",
                            contentBody = "التباين العالي والخطوط الواضحة يعززان الفهم ويقللان التشتت البصري.",
                            durationMinutes = 7,
                            mediaType = MediaKind.IMAGE.name,
                            mediaUri = mediaLabDrawableUri,
                        ),
                    )
                )

                moduleDao.insertModules(
                    listOf(
                        ModuleEntity(
                            courseId = c4,
                            orderIndex = 1,
                            title = "الوحدة ١: التعاطف وتحديد التحدي",
                            summary = "فهم احتياجات المتعلمين الحقيقية قبل تصميم الحلول.",
                            contentBody = "الملاحظة الصفية والاستماع المباشر لتحديات الطلبة.",
                            durationMinutes = 8,
                            mediaType = MediaKind.IMAGE.name,
                            mediaUri = heroDrawableUri,
                        ),
                        ModuleEntity(
                            courseId = c4,
                            orderIndex = 2,
                            title = "الوحدة ٢: النموذج الأولي والاختبار",
                            summary = "تجربة الفكرة سريعاً وتحسينها بالتغذية الراجعة.",
                            contentBody = "ابنِ نموذجاً بسيطاً في 15 دقيقة واختبره مع مجموعة صغيرة.",
                            durationMinutes = 8,
                            mediaType = MediaKind.DOCUMENT.name,
                        ),
                    )
                )
            }
        }

        if (dao.getMediaCount() == 0) {
            dao.insertMediaList(
                listOf(
                    MediaAssetEntity(
                        title = "موشن جرافك: رؤية EduTech Mindset",
                        category = "القيادة الرقمية",
                        mediaType = MediaKind.VIDEO.name,
                        uriString = introVideoUri,
                        description = "عرض موشن جرافك تعريفي بمنهجية عقلية التعليم والتكنولوجيا بإدارة الأستاذ باسل الفقعاوي.",
                        fileSizeBytes = 134_000L,
                        durationLabel = "00:08",
                        isBookmarked = true,
                        isBundled = true,
                    ),
                    MediaAssetEntity(
                        title = "بودكاست: العقلية الرقمية للمعلّم المبدع",
                        category = "الوسائط التفاعلية",
                        mediaType = MediaKind.AUDIO.name,
                        uriString = podcastAudioUri,
                        description = "مقطع صوتي تفاعلي مع محلل موجات صوتية فوري حول مهارات القرن الحادي والعشرين.",
                        fileSizeBytes = 193_000L,
                        durationLabel = "00:12",
                        isBookmarked = true,
                        isBundled = true,
                    ),
                    MediaAssetEntity(
                        title = "إنفوجرافيك: مسار التعلم الذكي",
                        category = "الذكاء الاصطناعي",
                        mediaType = MediaKind.IMAGE.name,
                        uriString = futureTechDrawableUri,
                        description = "خريطة بصرية تفاعلية عالية الدقة توضح تكامل البيانات والكتاب الرقمي.",
                        fileSizeBytes = 240_000L,
                        durationLabel = "HD",
                        isBundled = true,
                    ),
                    MediaAssetEntity(
                        title = "لوحة استوديو الوسائط المتعددة",
                        category = "الوسائط التفاعلية",
                        mediaType = MediaKind.IMAGE.name,
                        uriString = mediaLabDrawableUri,
                        description = "مرجع بصري لعناصر إنتاج المحتوى المرئي والمسموع في التعليم الحديث.",
                        fileSizeBytes = 225_000L,
                        durationLabel = "HD",
                        isBundled = true,
                    ),
                    MediaAssetEntity(
                        title = "الدليل التطبيقي: تصميم الحصة التفاعلية",
                        category = "تصميم التعليم",
                        mediaType = MediaKind.DOCUMENT.name,
                        uriString = "guide://edutech_interactive_lesson",
                        description = """
                            الدليل التطبيقي لبناء حصة دراسية ذكية — إعداد الأستاذ باسل الفقعاوي

                            1. التمهيد المحفّز (3 دقائق):
                            ابدأ بسؤال إشكالي أو صورة/فيديو قصير يثير الفضول ويربط الدرس بواقع الطالب.

                            2. الاستكشاف المدعوم بالتقنية (10 دقائق):
                            قدّم المفهوم عبر وسائط متعددة (فيديو قصير + مخطط بصري + مقطع صوتي) ليختار الطالب مساره المفضّل.

                            3. التطبيق والإنتاج (12 دقيقة):
                            اطلب من الطلبة توثيق استنتاجاتهم في ملاحظات رقمية أو تسجيل صوتي موجز.

                            4. التأمل والتقييم الفوري (5 دقائق):
                            لخّص أهم ثلاث أفكار، وقِس الفهم بسؤال تطبيقي سريع، ثم احفظ المخرجات للمراجعة المستمرة.
                        """.trimIndent(),
                        fileSizeBytes = 12_400L,
                        durationLabel = "دليل",
                        isBundled = true,
                    ),
                )
            )
        }

        if (dao.getNoteCount() == 0) {
            dao.insertNotes(
                listOf(
                    StudyNoteEntity(
                        title = "قاعدة التوازن في EduTech Mindset",
                        content = "التقنية وسيلة لتمكين الفكر التربوي وليست بديلاً عنه. الهدف دائماً هو تعميق الفهم وبناء شغف التعلّم المستمر.",
                        tag = "فكرة ملهمة",
                        accentKey = "gold",
                        isPinned = true,
                    ),
                    StudyNoteEntity(
                        title = "خطة تطبيق الوسائط في درسي القادم",
                        content = "سأستخدم فيديو تمهيدي قصير ثم أطلب من الطلاب تدوين خلاصة مركّزة وإرفاق صورة توضيحية.",
                        tag = "خطة تطبيق",
                        accentKey = "cyan",
                    ),
                )
            )
        }
    }

    suspend fun setThemeMode(mode: com.example.data.local.ThemeMode) = prefsRepo.setThemeMode(mode)

    suspend fun toggleDarkTheme(enabled: Boolean) = prefsRepo.setDarkTheme(enabled)

    suspend fun setDynamicColor(enabled: Boolean) = prefsRepo.setDynamicColor(enabled)

    suspend fun toggleHighMotion(enabled: Boolean) = prefsRepo.setHighMotion(enabled)

    suspend fun setCompletedWelcome(completed: Boolean) = prefsRepo.setCompletedWelcome(completed)

    suspend fun updateFounderAvatarFromUri(uri: Uri) {
        val copied = mediaStorage.copyUriToLocalStorage(uri)
        prefsRepo.setFounderAvatarUri(copied.uriString)
    }

    suspend fun updateLearnerName(newName: String) {
        val current = dao.getLearnerStatsOnce() ?: LearnerStatsEntity()
        dao.upsertLearnerStats(current.copy(learnerName = newName.trim().ifBlank { current.learnerName }))
    }

    suspend fun toggleModuleCompleted(course: CourseEntity, module: ModuleEntity, currentlyCompleted: Boolean) {
        val nowCompleted = !currentlyCompleted
        val existing = progressDao.getProgressForCourseOnce(course.id).firstOrNull { it.moduleId == module.id }
        progressDao.upsertProgress(
            ModuleProgressEntity(
                moduleId = module.id,
                courseId = course.id,
                progressPercent = if (nowCompleted) 100 else 0,
                isCompleted = nowCompleted,
                lastPositionMs = if (nowCompleted) existing?.lastPositionMs ?: 0L else 0L,
                completedAt = if (nowCompleted) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis(),
            )
        )
        recalculateCourseProgress(course)
        if (nowCompleted) {
            awardXp(25, incrementCompletedLesson = false)
        }
    }

    suspend fun updateModulePlaybackPosition(
        course: CourseEntity,
        module: ModuleEntity,
        positionMs: Long,
        durationMs: Long,
    ) {
        if (durationMs <= 0L) return
        val existing = progressDao.getProgressForCourseOnce(course.id).firstOrNull { it.moduleId == module.id }
        val pct = ((positionMs * 100L) / durationMs).toInt().coerceIn(0, 100)
        val mergedPct = if (existing?.isCompleted == true) 100 else maxOf(existing?.progressPercent ?: 0, pct)
        val nowCompleted = existing?.isCompleted == true || pct >= 95
        progressDao.upsertProgress(
            ModuleProgressEntity(
                moduleId = module.id,
                courseId = course.id,
                progressPercent = mergedPct,
                isCompleted = nowCompleted,
                lastPositionMs = positionMs,
                completedAt = existing?.completedAt ?: if (nowCompleted) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis(),
            )
        )
        recalculateCourseProgress(course)
    }

    suspend fun attachVideoToModule(
        module: ModuleEntity,
        localUri: Uri?,
        externalUrl: String?,
    ) {
        val finalUri = when {
            localUri != null -> mediaStorage.copyUriToLocalStorage(localUri).uriString
            !externalUrl.isNullOrBlank() -> externalUrl.trim()
            else -> return
        }
        moduleDao.updateModule(
            module.copy(
                mediaType = MediaKind.VIDEO.name,
                mediaUri = finalUri,
            )
        )
        awardXp(10, incrementCompletedLesson = false)
    }

    private suspend fun recalculateCourseProgress(course: CourseEntity) {
        val modules = moduleDao.getModulesForCourseOnce(course.id)
        if (modules.isEmpty()) return
        val progressMap = progressDao.getProgressForCourseOnce(course.id).associateBy { it.moduleId }
        val avgProgress = modules.sumOf { progressMap[it.id]?.progressPercent ?: 0 } / modules.size
        val allCompleted = modules.all { progressMap[it.id]?.isCompleted == true }
        val wasCompleted = course.isCompleted

        courseDao.updateCourse(
            course.copy(
                progressPercent = avgProgress.coerceIn(0, 100),
                isCompleted = allCompleted,
            )
        )
        if (!wasCompleted && allCompleted) {
            awardXp(50, incrementCompletedLesson = true)
        }
    }

    suspend fun updateCourseProgress(course: CourseEntity, newProgress: Int) {
        val clamped = newProgress.coerceIn(0, 100)
        val wasCompleted = course.isCompleted
        val nowCompleted = clamped >= 100
        courseDao.updateCourse(
            course.copy(
                progressPercent = clamped,
                isCompleted = nowCompleted,
            )
        )
        if (!wasCompleted && nowCompleted) {
            awardXp(50, incrementCompletedLesson = true)
        }
    }

    suspend fun toggleCourseCompleted(course: CourseEntity) {
        val nowCompleted = !course.isCompleted
        val newProgress = if (nowCompleted) 100 else 50
        courseDao.updateCourse(
            course.copy(
                isCompleted = nowCompleted,
                progressPercent = newProgress,
            )
        )
        val modules = moduleDao.getModulesForCourseOnce(course.id)
        modules.forEach { mod ->
            progressDao.upsertProgress(
                ModuleProgressEntity(
                    moduleId = mod.id,
                    courseId = course.id,
                    progressPercent = if (nowCompleted) 100 else 0,
                    isCompleted = nowCompleted,
                    completedAt = if (nowCompleted) System.currentTimeMillis() else null,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
        if (nowCompleted) {
            awardXp(50, incrementCompletedLesson = true)
        }
    }

    suspend fun toggleCourseBookmark(course: CourseEntity) {
        courseDao.updateCourse(course.copy(isBookmarked = !course.isBookmarked))
    }

    suspend fun addCustomCourse(
        title: String,
        subtitle: String,
        category: String,
        description: String,
        lessonContent: String,
        durationMinutes: Int,
    ) {
        val courseId = courseDao.insertCourse(
            CourseEntity(
                title = title.trim(),
                subtitle = subtitle.trim().ifBlank { "مسار مخصّص محفوظ محلياً" },
                category = category,
                description = description.trim().ifBlank { "درس تعليمي مضاف إلى مكتبتك المحلية." },
                lessonContent = lessonContent.trim().ifBlank { description.trim() },
                durationMinutes = durationMinutes.coerceIn(1, 300),
                coverKey = "future_tech",
                instructorName = "إضافة محلية • بإشراف الأستاذ باسل الفقعاوي",
                isUserCreated = true,
            )
        )
        moduleDao.insertModule(
            ModuleEntity(
                courseId = courseId,
                orderIndex = 1,
                title = "الوحدة الأساسية: ${title.trim()}",
                summary = subtitle.trim().ifBlank { "المحتوى الرئيسي للمسار" },
                contentBody = lessonContent.trim().ifBlank { description.trim() },
                durationMinutes = durationMinutes.coerceIn(1, 300),
                mediaType = MediaKind.DOCUMENT.name,
            )
        )
        awardXp(20, incrementCompletedLesson = false)
    }

    suspend fun deleteCourse(course: CourseEntity) {
        courseDao.deleteCourseById(course.id)
    }

    suspend fun importLocalMediaFromUri(
        sourceUri: Uri,
        customTitle: String,
        category: String,
        description: String,
    ): MediaAssetEntity {
        val copied = mediaStorage.copyUriToLocalStorage(sourceUri)
        val finalTitle = customTitle.trim().ifBlank {
            copied.fileName.substringBeforeLast('.').ifBlank { "ملف وسائط محلي" }
        }
        val entity = MediaAssetEntity(
            title = finalTitle,
            category = category,
            mediaType = copied.detectedKind.name,
            uriString = copied.uriString,
            description = description.trim().ifBlank { "ملف محفوظ محلياً داخل مساحة التطبيق الآمنة على هاتفك." },
            fileSizeBytes = copied.sizeBytes,
            durationLabel = copied.detectedKind.labelAr,
            isBundled = false,
            isInternalFile = true,
        )
        val insertedId = dao.insertMedia(entity)
        awardXp(15, incrementCompletedLesson = false)
        return entity.copy(id = insertedId)
    }

    suspend fun addExternalMediaLink(
        title: String,
        url: String,
        kind: MediaKind,
        category: String,
        description: String,
    ) {
        dao.insertMedia(
            MediaAssetEntity(
                title = title.trim(),
                category = category,
                mediaType = kind.name,
                uriString = url.trim(),
                description = description.trim().ifBlank { "وسائط تعليمية مضافة بالرابط." },
                durationLabel = kind.labelAr,
                isBundled = false,
                isInternalFile = false,
            )
        )
        awardXp(10, incrementCompletedLesson = false)
    }

    suspend fun toggleMediaBookmark(item: MediaAssetEntity) {
        dao.updateMedia(item.copy(isBookmarked = !item.isBookmarked))
    }

    suspend fun deleteMedia(item: MediaAssetEntity) {
        if (item.isInternalFile) {
            mediaStorage.deleteLocalFileIfInternal(item.uriString)
        }
        dao.deleteMediaById(item.id)
    }

    suspend fun saveNote(
        existingId: Long? = null,
        title: String,
        content: String,
        tag: String,
        accentKey: String,
        attachedUri: Uri? = null,
        existingMediaUri: String = "",
        existingMediaType: String = "",
    ) {
        var finalUri = existingMediaUri
        var finalType = existingMediaType
        if (attachedUri != null) {
            val copied = mediaStorage.copyUriToLocalStorage(attachedUri)
            finalUri = copied.uriString
            finalType = copied.detectedKind.name
        }
        val note = StudyNoteEntity(
            id = existingId ?: 0L,
            title = title.trim(),
            content = content.trim(),
            tag = tag,
            accentKey = accentKey,
            attachedMediaUri = finalUri,
            attachedMediaType = finalType,
            updatedAt = System.currentTimeMillis(),
        )
        dao.insertNote(note)
        if (existingId == null || existingId == 0L) {
            awardXp(15, incrementCompletedLesson = false)
        }
    }

    suspend fun toggleNotePin(note: StudyNoteEntity) {
        dao.updateNote(note.copy(isPinned = !note.isPinned))
    }

    suspend fun deleteNote(note: StudyNoteEntity) {
        if (note.attachedMediaUri.startsWith("file://")) {
            mediaStorage.deleteLocalFileIfInternal(note.attachedMediaUri)
        }
        dao.deleteNoteById(note.id)
    }

    suspend fun getInternalStorageUsedBytes(): Long = mediaStorage.calculateTotalInternalStorageBytes()

    suspend fun rewardFocusSession() {
        awardXp(15, incrementCompletedLesson = false)
    }

    private suspend fun awardXp(points: Int, incrementCompletedLesson: Boolean) {
        val current = dao.getLearnerStatsOnce() ?: LearnerStatsEntity()
        val today = System.currentTimeMillis() / 86_400_000L
        val nextStreak = when {
            today == current.lastActiveDay -> current.streakDays
            today == current.lastActiveDay + 1 -> current.streakDays + 1
            else -> 1
        }
        dao.upsertLearnerStats(
            current.copy(
                xpPoints = current.xpPoints + points,
                streakDays = nextStreak,
                completedLessons = if (incrementCompletedLesson) current.completedLessons + 1 else current.completedLessons,
                lastActiveDay = today,
            )
        )
    }
}
