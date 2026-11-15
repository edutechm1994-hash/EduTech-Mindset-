package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.AnimatedProgressRing
import com.example.ui.components.TtsIconToggleButton
import com.example.ui.components.rememberAccessibilityTtsEngine
import kotlinx.coroutines.delay

enum class ToolkitAudience(val label: String) {
    TEACHER("أدوات المعلّم"),
    STUDENT("أدوات الطالب"),
}

object EduTechBlueprintBuilder {
    fun buildBlueprint(
        audience: ToolkitAudience,
        toolName: String,
        subjectOrTopic: String,
        stage: String,
    ): String {
        val cleanTopic = subjectOrTopic.trim().ifBlank { "الدرس التفاعلي" }
        val cleanStage = stage.trim().ifBlank { "المرحلة العامة" }
        return if (audience == ToolkitAudience.TEACHER) {
            when (toolName) {
                "تحضير درس ذكي" -> """
                    خطة تحضير درس تفاعلي — EduTech Mindset
                    الموضوع: $cleanTopic ($cleanStage)
                    1. التمهيد المحفّز (4 دقائق): عرض صورة أو لغز بصري يربط المفهوم بحياة الطالب.
                    2. العرض التفاعلي (12 دقيقة): دمج شرح مبسّط مع فيديو قصير وتساؤلات مفتوحة.
                    3. النشاط التعاوني (10 دقائق): تقسيم الصف لمجموعات لتطبيق الفكرة بمثال عملي.
                    4. التقييم الختامي (4 دقائق): 3 أسئلة سريعة لقياس الفهم وتحديد نقاط الدعم.
                """.trimIndent()
                "توليد أسئلة تقويم" -> """
                    بنك أسئلة متدرّج المستويات — $cleanTopic ($cleanStage)
                    • سؤال الفهم الأساسي: اشرح المفهوم الرئيسي لدرس «$cleanTopic» بأسلوبك الخاص.
                    • سؤال التطبيق العملي: أعطِ مثالاً من الواقع يوضّح أثر «$cleanTopic».
                    • سؤال التفكير الناقد: ماذا يحدث لو تغيّر أحد شروط أو عناصر «$cleanTopic»؟
                    • تحدّي التميّز: صمّم تلخيصاً بصرياً أو خريطة مفاهيم للدرس في 3 نقاط.
                """.trimIndent()
                else -> """
                    نشاط صفي لكسر الجمود والتفاعل — $cleanTopic ($cleanStage)
                    • فكرة النشاط (دقيقة واحدة): «تحدي البطاقة الذهبية» — يطرح المعلم فرضية مثيرة حول «$cleanTopic».
                    • التنفيذ: يناقش كل طالب زميله لمدة دقيقتين (فكّر – زاوج – شارك).
                    • المخرج: مشاركة أبرز استنتاجين وتوثيقهما على السبورة التفاعلية.
                """.trimIndent()
            }
        } else {
            when (toolName) {
                "خطة مذاكرة ذكية" -> """
                    خطة المذاكرة الذكية بدون تسويف — $cleanTopic ($cleanStage)
                    • جلسة التركيز (25 دقيقة): قراءة العناوين الرئيسية ثم فهم الأمثلة المحلولة في «$cleanTopic».
                    • الاسترجاع النشط (10 دقائق): أغلق الكتاب واكتب أهم القوانين والأفكار من الذاكرة.
                    • المراجعة المتباعدة: أعد حل مسألتين صعبتين بعد 24 ساعة ثم بعد 3 أيام.
                """.trimIndent()
                "خريطة مفاهيم" -> """
                    ملخص الخريطة الذهنية — $cleanTopic ($cleanStage)
                    [المفهوم المركزي]: $cleanTopic
                    ├─ الفكرة المحورية الأولى: التعريف الدقيق والكلمات المفتاحية
                    ├─ الفكرة المحورية الثانية: الخطوات التطبيقية والأمثلة الشائعة
                    └─ تنبيهات الاختبار: الأخطاء الشائعة وكيفية تجنّبها
                """.trimIndent()
                else -> """
                    استراتيجية الاستعداد للتقييم والامتحانات — $cleanTopic ($cleanStage)
                    • تقسيم الوحدة إلى 3 أجزاء قصيرة مع استراحة 5 دقائق بين كل جزء.
                    • التدريب تحت ضغط الوقت (مؤقت 15 دقيقة لحل نموذج اختبار).
                    • مراجعة قائمة الأخطاء وتدوين سبب كل خطأ في دفتر الملاحظات.
                """.trimIndent()
            }
        }
    }
}

@Composable
fun EduTechToolkitScreen(
    onBack: () -> Unit,
    onSaveBlueprintToNotes: (title: String, content: String, tag: String) -> Unit,
    onCompleteFocusSession: () -> Unit,
    onOpenWebsitePortal: () -> Unit,
    onShowNotice: (String) -> Unit,
) {
    BackHandler { onBack() }

    val clipboardManager = LocalClipboardManager.current
    val ttsEngine = rememberAccessibilityTtsEngine()

    // Pomodoro Focus Timer State
    var focusDurationMinutes by remember { mutableIntStateOf(25) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        }
        if (isTimerRunning && remainingSeconds == 0) {
            isTimerRunning = false
            onCompleteFocusSession()
        }
    }

    // Teacher & Student Blueprint Builder State
    var audience by remember { mutableStateOf(ToolkitAudience.TEACHER) }
    val teacherTools = listOf("تحضير درس ذكي", "توليد أسئلة تقويم", "نشاط كسر جمود")
    val studentTools = listOf("خطة مذاكرة ذكية", "خريطة مفاهيم", "استعداد للامتحان")
    var selectedTool by remember(audience) {
        mutableStateOf(if (audience == ToolkitAudience.TEACHER) teacherTools.first() else studentTools.first())
    }
    var topicInput by remember { mutableStateOf("") }
    var stageInput by remember { mutableStateOf("الثانوية العامة (التوجيهي)") }

    val generatedBlueprint = remember(audience, selectedTool, topicInput, stageInput) {
        EduTechBlueprintBuilder.buildBlueprint(audience, selectedTool, topicInput, stageInput)
    }

    val totalTimerSeconds = (focusDurationMinutes * 60).coerceAtLeast(1)
    val timerFraction = 1f - (remainingSeconds.toFloat() / totalTimerSeconds.toFloat())
    val formattedClock = String.format(
        java.util.Locale.US,
        "%02d:%02d",
        remainingSeconds / 60,
        remainingSeconds % 60,
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toolkit_back_button"),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "العودة")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("أدوات EduTech للمعلّم والطالب", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "مستوحاة من رؤية الأستاذ باسل الفقعاوي • edutech-mindset.pages.dev",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                // 1. Mindset Banner & Direct Website Link
                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = "«التكنولوجيا ليست بديلاً عن المعلم، بل هي سلاحه»",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(
                                text = "اجمع بين أدوات الإنتاج التربوي محلياً وبين أحدث المقالات والشروحات المنشورة على موقع المنصة.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(
                                onClick = onOpenWebsitePortal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("toolkit_open_website"),
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.open_website_in_app))
                            }
                        }
                    }
                }

                // 2. Interactive Teacher / Student Blueprint Builder
                item {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text("مصمّم القوالب الذكية", style = MaterialTheme.typography.titleLarge)
                                }
                                TtsIconToggleButton(
                                    itemId = "toolkit_blueprint_tts",
                                    textToRead = generatedBlueprint,
                                    ttsEngine = ttsEngine,
                                    contentDescriptionListen = "الاستماع للقالب",
                                    modifier = Modifier.testTag("toolkit_blueprint_tts"),
                                )
                            }

                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                ToolkitAudience.entries.forEachIndexed { index, option ->
                                    SegmentedButton(
                                        selected = audience == option,
                                        onClick = { audience = option },
                                        shape = SegmentedButtonDefaults.itemShape(index, ToolkitAudience.entries.size),
                                        label = { Text(option.label) },
                                    )
                                }
                            }

                            val toolOptions = if (audience == ToolkitAudience.TEACHER) teacherTools else studentTools
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(toolOptions) { tool ->
                                    FilterChip(
                                        selected = selectedTool == tool,
                                        onClick = { selectedTool = tool },
                                        label = { Text(tool) },
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = topicInput,
                                onValueChange = { topicInput = it },
                                label = { Text("المادة أو الموضوع (مثال: المعادلات الكيميائية / البرمجة)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("toolkit_topic_input"),
                            )
                            OutlinedTextField(
                                value = stageInput,
                                onValueChange = { stageInput = it },
                                label = { Text("المرحلة الدراسية") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = generatedBlueprint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(14.dp),
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(generatedBlueprint))
                                        onShowNotice("تم نسخ القالب إلى الحافظة")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("toolkit_copy_button"),
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("نسخ")
                                }
                                Button(
                                    onClick = {
                                        val noteTitle = "$selectedTool: ${topicInput.trim().ifBlank { stageInput }}"
                                        onSaveBlueprintToNotes(noteTitle, generatedBlueprint, audience.label)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("toolkit_save_note_button"),
                                ) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("حفظ في ملاحظاتي")
                                }
                            }
                        }
                    }
                }

                // 3. Smart Study Focus Timer (Pomodoro for Students & Lesson Prep)
                item {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text("مؤقّت الدراسة والتركيز بذكاء", style = MaterialTheme.typography.titleLarge)
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf(15 to "15 د سريعة", 25 to "25 د بومودورو", 45 to "45 د عميقة")) { (mins, label) ->
                                    FilterChip(
                                        selected = focusDurationMinutes == mins,
                                        onClick = {
                                            isTimerRunning = false
                                            focusDurationMinutes = mins
                                            remainingSeconds = mins * 60
                                        },
                                        label = { Text(label) },
                                    )
                                }
                            }

                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(136.dp)) {
                                AnimatedProgressRing(
                                    progressFraction = timerFraction.coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 10.dp,
                                )
                                Text(
                                    text = formattedClock,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { isTimerRunning = !isTimerRunning },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("toolkit_timer_toggle"),
                                ) {
                                    Icon(
                                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isTimerRunning) "إيقاف مؤقت" else "ابدأ جلسة التركيز")
                                }
                                OutlinedButton(
                                    onClick = {
                                        isTimerRunning = false
                                        remainingSeconds = focusDurationMinutes * 60
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("إعادة")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
