package com.example.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.ui.graphics.vector.ImageVector

enum class ModuleDifficulty(val labelAr: String) {
    BEGINNER("مبتدئ"),
    INTERMEDIATE("متوسط"),
    ADVANCED("متقدم"),
}

enum class ModuleFormat(val labelAr: String) {
    VIDEO("فيديو تفاعلي"),
    AUDIO("بودكاست صوتي"),
    INTERACTIVE("ورشة عملية"),
    GUIDE("دليل معرفي"),
}

enum class CategoryTone {
    PRIMARY,
    SECONDARY,
    TERTIARY,
}

data class DashboardCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tone: CategoryTone,
)

data class LearningModuleItem(
    val id: String,
    val categoryId: String,
    val title: String,
    val summary: String,
    val instructor: String,
    val durationMinutes: Int,
    val lessonsCount: Int,
    val progressPercent: Int,
    val difficulty: ModuleDifficulty,
    val format: ModuleFormat,
    val xpReward: Int,
    val isBookmarked: Boolean = false,
    val isFeatured: Boolean = false,
)

data class DashboardSummaryMetrics(
    val learnerName: String,
    val activeStreakDays: Int,
    val totalXp: Int,
    val weeklyGoalMinutes: Int,
    val completedMinutes: Int,
)

data class DashboardDummySnapshot(
    val metrics: DashboardSummaryMetrics,
    val categories: List<DashboardCategory>,
    val modules: List<LearningModuleItem>,
)

object EduTechDummyDashboardData {

    val categories: List<DashboardCategory> = listOf(
        DashboardCategory(
            id = "ai_education",
            title = "الذكاء الاصطناعي التربوي",
            subtitle = "توظيف أدوات الذكاء الاصطناعي في التخطيط والتقييم الذكي",
            icon = Icons.Default.AutoAwesome,
            tone = CategoryTone.PRIMARY,
        ),
        DashboardCategory(
            id = "digital_leadership",
            title = "القيادة والتحول الرقمي",
            subtitle = "منهجية EduTech Mindset لبناء بيئات تعلّم مستدامة",
            icon = Icons.Default.RocketLaunch,
            tone = CategoryTone.SECONDARY,
        ),
        DashboardCategory(
            id = "media_studio",
            title = "استوديو الوسائط التفاعلية",
            subtitle = "تصميم الميكرو-فيديو والبودكاست التعليمي الجاذب",
            icon = Icons.Default.PermMedia,
            tone = CategoryTone.TERTIARY,
        ),
        DashboardCategory(
            id = "design_thinking",
            title = "التفكير التصميمي ومشاريع STEM",
            subtitle = "تحويل التحديات الصفية إلى نماذج إبداعية قابلة للتطبيق",
            icon = Icons.Default.DesignServices,
            tone = CategoryTone.PRIMARY,
        ),
    )

    val modules: List<LearningModuleItem> = listOf(
        LearningModuleItem(
            id = "mod_ai_1",
            categoryId = "ai_education",
            title = "هندسة الأوامر التعليمية للمعلّم المبدع",
            summary = "صياغة أوامر دقيقة لتوليد خطط دراسية متدرجة الصعوبة وأنشطة تفاعلية تراعي الفروق الفردية.",
            instructor = "أ. باسل أشرف الفقعاوي",
            durationMinutes = 24,
            lessonsCount = 6,
            progressPercent = 72,
            difficulty = ModuleDifficulty.INTERMEDIATE,
            format = ModuleFormat.INTERACTIVE,
            xpReward = 120,
            isBookmarked = true,
            isFeatured = true,
        ),
        LearningModuleItem(
            id = "mod_ai_2",
            categoryId = "ai_education",
            title = "التقييم التكويني المدعوم بالتحليلات الذكية",
            summary = "بناء اختبارات تشخيصية فورية وتغذية راجعة مخصصة لكل متعلّم دون تعقيد تقني.",
            instructor = "أ. باسل أشرف الفقعاوي",
            durationMinutes = 18,
            lessonsCount = 4,
            progressPercent = 35,
            difficulty = ModuleDifficulty.BEGINNER,
            format = ModuleFormat.VIDEO,
            xpReward = 90,
        ),
        LearningModuleItem(
            id = "mod_ai_3",
            categoryId = "ai_education",
            title = "حوكمة وأخلاقيات البيانات في الصفوف الذكية",
            summary = "مبادئ حماية خصوصية الطلبة والتحقق التربوي من مخرجات النماذج اللغوية الحديثة.",
            instructor = "فريق EduTech Mindset",
            durationMinutes = 15,
            lessonsCount = 3,
            progressPercent = 100,
            difficulty = ModuleDifficulty.ADVANCED,
            format = ModuleFormat.GUIDE,
            xpReward = 110,
            isBookmarked = true,
        ),
        LearningModuleItem(
            id = "mod_lead_1",
            categoryId = "digital_leadership",
            title = "مثلث عقلية التعليم والتكنولوجيا",
            summary = "التوازن العملي بين المعلّم الميسّر، التقنية الهادفة، والتعلّم النشط داخل الحصة.",
            instructor = "أ. باسل أشرف الفقعاوي",
            durationMinutes = 20,
            lessonsCount = 5,
            progressPercent = 85,
            difficulty = ModuleDifficulty.BEGINNER,
            format = ModuleFormat.VIDEO,
            xpReward = 100,
            isBookmarked = true,
        ),
        LearningModuleItem(
            id = "mod_lead_2",
            categoryId = "digital_leadership",
            title = "خارطة طريق التحول من التلقين إلى التمكين",
            summary = "استراتيجيات عملية لقيادة التغيير التربوي وبناء ثقافة التجريب الإيجابي في المدرسة.",
            instructor = "أ. باسل أشرف الفقعاوي",
            durationMinutes = 28,
            lessonsCount = 7,
            progressPercent = 20,
            difficulty = ModuleDifficulty.INTERMEDIATE,
            format = ModuleFormat.GUIDE,
            xpReward = 140,
        ),
        LearningModuleItem(
            id = "mod_media_1",
            categoryId = "media_studio",
            title = "قاعدة الدقائق الست في الميكرو-فيديو التعليمي",
            summary = "تقنيات كتابة السيناريو التعليمي المكثّف والإخراج البصري الذي يرفع التركيز.",
            instructor = "استوديو EduTech",
            durationMinutes = 16,
            lessonsCount = 4,
            progressPercent = 50,
            difficulty = ModuleDifficulty.BEGINNER,
            format = ModuleFormat.VIDEO,
            xpReward = 85,
        ),
        LearningModuleItem(
            id = "mod_media_2",
            categoryId = "media_studio",
            title = "البودكاست التربوي والسرد القصصي الصوتي",
            summary = "تحويل المفاهيم المجردة إلى حلقات صوتية شيّقة تدعم التعلّم المرن أثناء التنقل.",
            instructor = "استوديو EduTech",
            durationMinutes = 22,
            lessonsCount = 5,
            progressPercent = 0,
            difficulty = ModuleDifficulty.INTERMEDIATE,
            format = ModuleFormat.AUDIO,
            xpReward = 95,
        ),
        LearningModuleItem(
            id = "mod_design_1",
            categoryId = "design_thinking",
            title = "مراحل التفكير التصميمي الخمس في التعليم",
            summary = "من التعاطف مع احتياج المتعلّم إلى بناء النموذج الأولي واختبار الأثر داخل الصف.",
            instructor = "مختبر الابتكار",
            durationMinutes = 26,
            lessonsCount = 5,
            progressPercent = 60,
            difficulty = ModuleDifficulty.INTERMEDIATE,
            format = ModuleFormat.INTERACTIVE,
            xpReward = 130,
        ),
        LearningModuleItem(
            id = "mod_design_2",
            categoryId = "design_thinking",
            title = "تحديات مشاريع STEM متعددة التخصصات",
            summary = "تصميم مهام أدائية تربط العلوم والتقنية والهندسة والرياضيات بواقع حياة الطلبة.",
            instructor = "مختبر الابتكار",
            durationMinutes = 30,
            lessonsCount = 6,
            progressPercent = 10,
            difficulty = ModuleDifficulty.ADVANCED,
            format = ModuleFormat.INTERACTIVE,
            xpReward = 150,
        ),
    )

    fun createInitialSnapshot(): DashboardDummySnapshot = DashboardDummySnapshot(
        metrics = DashboardSummaryMetrics(
            learnerName = "مبدع EduTech",
            activeStreakDays = 7,
            totalXp = 1280,
            weeklyGoalMinutes = 180,
            completedMinutes = 132,
        ),
        categories = categories,
        modules = modules,
    )
}
