package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.CourseEntity
import com.example.data.local.MediaAssetEntity
import com.example.data.local.MediaKind
import com.example.ui.components.AccessibilityTtsEngine
import com.example.ui.components.AudioAccessibilityCard
import com.example.ui.components.CourseAudioFormatter
import com.example.ui.components.TtsIconToggleButton
import com.example.ui.components.rememberAccessibilityTtsEngine

@Composable
fun CoursesScreen(
    coursesWithModules: List<com.example.data.local.CourseWithModules>,
    matchedModules: List<com.example.data.local.ModuleWithProgress>,
    searchQuery: String,
    selectedCategory: String,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onOpenCourse: (CourseEntity) -> Unit,
    onOpenMatchedModule: (com.example.data.local.ModuleEntity) -> Unit,
    onToggleBookmark: (CourseEntity) -> Unit,
    onAddCourse: (title: String, subtitle: String, category: String, description: String, content: String, duration: Int) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val ttsEngine = rememberAccessibilityTtsEngine()
    val categories = listOf("الكل", "القيادة الرقمية", "الذكاء الاصطناعي", "الوسائط التفاعلية", "تصميم التعليم")

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "المسارات والدروس التعليمية",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("ابحث بالعنوان أو الكلمة المفتاحية في المسارات والوحدات...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح البحث")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("courses_search_input"),
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { onCategoryChange(cat) },
                                label = { Text(cat) },
                            )
                        }
                    }
                }
            }

            // Matching Modules Section from Room ModuleDao.searchModulesWithProgress
            if (searchQuery.isNotBlank() && matchedModules.isNotEmpty()) {
                item {
                    Text(
                        text = "الوحدات المطابقة (${matchedModules.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                items(matchedModules, key = { "mod_${it.module.id}" }) { modWithProg ->
                    val mod = modWithProg.module
                    val isDone = modWithProg.progress?.isCompleted == true
                    Surface(
                        onClick = { onOpenMatchedModule(mod) },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_matched_module_${mod.id}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = if (isDone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mod.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = mod.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = "${mod.durationMinutes} د",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            TtsIconToggleButton(
                                itemId = "search_mod_${mod.id}",
                                textToRead = CourseAudioFormatter.formatModuleSummaryNarration(mod),
                                ttsEngine = ttsEngine,
                                contentDescriptionListen = stringResource(R.string.tts_listen_module_summary),
                                modifier = Modifier.testTag("search_module_tts_${mod.id}"),
                            )
                        }
                    }
                }
            }

            if (coursesWithModules.isEmpty() && matchedModules.isEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("لا توجد مسارات أو وحدات مطابقة لبحثك", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "جرّب كلمة مفتاحية أخرى أو أضف درساً مخصصاً لحفظه محلياً.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                if (searchQuery.isNotBlank() && coursesWithModules.isNotEmpty()) {
                    item {
                        Text(
                            text = "المسارات المطابقة (${coursesWithModules.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                items(coursesWithModules, key = { it.course.id }) { item ->
                    CourseListCard(
                        course = item.course,
                        ttsEngine = ttsEngine,
                        onClick = { onOpenCourse(item.course) },
                        onToggleBookmark = { onToggleBookmark(item.course) },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_course_fab"),
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة درس جديد")
        }
    }

    if (showAddDialog) {
        AddCourseDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, sub, cat, desc, body, mins ->
                onAddCourse(title, sub, cat, desc, body, mins)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun CourseListCard(
    course: CourseEntity,
    ttsEngine: AccessibilityTtsEngine,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    val coverRes = when (course.coverKey) {
        "media_lab" -> R.drawable.img_track_media_lab_1794764865682
        "welcome_hero" -> R.drawable.edutech_hero_banner_1794771056331
        else -> R.drawable.img_track_future_tech_1794764866174
    }

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("course_item_${course.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp),
            ) {
                AsyncImage(
                    model = coverRes,
                    contentDescription = course.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.65f),
                    ) {
                        Text(
                            text = course.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.55f),
                    ) {
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = if (course.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "حفظ المسار",
                                tint = if (course.isBookmarked) MaterialTheme.colorScheme.secondary else Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(course.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = course.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                LinearProgressIndicator(
                    progress = { course.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50)),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${course.durationMinutes} دقيقة", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TtsIconToggleButton(
                            itemId = "course_card_${course.id}",
                            textToRead = CourseAudioFormatter.formatCourseNarration(course),
                            ttsEngine = ttsEngine,
                            contentDescriptionListen = stringResource(R.string.tts_listen_course),
                            modifier = Modifier.testTag("course_tts_button_${course.id}"),
                        )
                        Text(
                            text = if (course.isCompleted) "مكتمل ✓" else "التقدّم ${course.progressPercent}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (course.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseDetailDialog(
    courseWithModules: com.example.data.local.CourseWithModules,
    onDismiss: () -> Unit,
    onUpdateProgress: (Int) -> Unit,
    onToggleComplete: () -> Unit,
    onToggleModuleComplete: (com.example.data.local.ModuleEntity, Boolean) -> Unit,
    onOpenModule: (com.example.data.local.ModuleEntity) -> Unit,
    onDeleteCourse: () -> Unit,
    onPlayCourseMedia: (MediaAssetEntity) -> Unit,
) {
    val course = courseWithModules.course
    val orderedModules = courseWithModules.orderedModules
    val ttsEngine = rememberAccessibilityTtsEngine()

    androidx.compose.runtime.DisposableEffect(course.id) {
        onDispose { ttsEngine.stop() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = course.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    Row {
                        if (course.isUserCreated) {
                            IconButton(onClick = onDeleteCourse) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "حذف الدرس",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }
                }

                Text(course.title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "${course.instructorName} • ${course.durationMinutes} دقيقة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                AudioAccessibilityCard(
                    title = stringResource(R.string.tts_course_reader_title),
                    primaryItemId = "course_desc_${course.id}",
                    primaryText = CourseAudioFormatter.formatCourseNarration(course),
                    primaryButtonLabel = stringResource(R.string.tts_listen_course),
                    ttsEngine = ttsEngine,
                    modifier = Modifier.testTag("course_detail_tts_card"),
                )

                if (course.mediaUri.isNotBlank()) {
                    Button(
                        onClick = {
                            onPlayCourseMedia(
                                MediaAssetEntity(
                                    id = -course.id,
                                    title = course.title,
                                    category = course.category,
                                    mediaType = course.mediaType,
                                    uriString = course.mediaUri,
                                    description = course.description,
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("course_play_media_button"),
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        val kindLabel = runCatching { MediaKind.valueOf(course.mediaType).labelAr }.getOrDefault("الوسائط")
                        Text("تشغيل $kindLabel المرفق بالدرس")
                    }
                }

                if (orderedModules.isNotEmpty()) {
                    Text("وحدات المسار (${orderedModules.size})", style = MaterialTheme.typography.titleMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        orderedModules.forEach { modWithProgress ->
                            val mod = modWithProgress.module
                            val isDone = modWithProgress.progress?.isCompleted == true
                            Surface(
                                onClick = { onOpenModule(mod) },
                                shape = MaterialTheme.shapes.medium,
                                color = if (isDone) {
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (isDone) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.outlineVariant,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("course_module_card_${mod.id}"),
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = mod.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.weight(1f),
                                        )
                                        TtsIconToggleButton(
                                            itemId = "detail_mod_${mod.id}",
                                            textToRead = CourseAudioFormatter.formatModuleSummaryNarration(mod),
                                            ttsEngine = ttsEngine,
                                            contentDescriptionListen = stringResource(R.string.tts_listen_module_summary),
                                            modifier = Modifier.testTag("module_card_tts_${mod.id}"),
                                        )
                                        IconButton(
                                            onClick = { onToggleModuleComplete(mod, isDone) },
                                            modifier = Modifier.testTag("module_complete_${mod.id}"),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = if (isDone) "مكتملة" else "إتمام الوحدة",
                                                tint = if (isDone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Text(
                                        text = mod.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "التقدّم: ${modWithProgress.progress?.progressPercent ?: 0}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        OutlinedButton(
                                            onClick = { onOpenModule(mod) },
                                            modifier = Modifier.testTag("open_module_screen_${mod.id}"),
                                        ) {
                                            Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("فتح شاشة الوحدة")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("محتوى المسار التعليمي", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = course.lessonContent,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("تقدّمك المحلي", style = MaterialTheme.typography.titleSmall)
                        Text("${course.progressPercent}%", style = MaterialTheme.typography.titleSmall)
                    }
                    Slider(
                        value = course.progressPercent / 100f,
                        onValueChange = { frac -> onUpdateProgress((frac * 100).toInt()) },
                    )
                }

                OutlinedButton(
                    onClick = onToggleComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("course_complete_toggle"),
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (course.isCompleted) "تم إتمام الدرس ✓" else "تحديد كمكتمل (+50 XP)"
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCourseDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, subtitle: String, category: String, description: String, content: String, duration: Int) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("القيادة الرقمية") }
    var description by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(20) }

    val cats = listOf("القيادة الرقمية", "الذكاء الاصطناعي", "الوسائط التفاعلية", "تصميم التعليم")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("إضافة مسار أو درس محلي", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الدرس *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("العنوان الفرعي") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(cats) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                        )
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("ملخص سريع") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("محاور ومحتوى الدرس") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("المدة التقديرية: $duration دقيقة", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { duration = (duration - 5).coerceAtLeast(5) }) { Text("-5") }
                        OutlinedButton(onClick = { duration = (duration + 5).coerceAtMost(180) }) { Text("+5") }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(title, subtitle, category, description, content, duration) },
                        enabled = title.isNotBlank(),
                    ) {
                        Text("حفظ محلياً")
                    }
                }
            }
        }
    }
}
