package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CourseEntity
import com.example.data.local.MediaAssetEntity
import com.example.data.local.MediaKind
import com.example.ui.components.AnimatedProgressRing
import com.example.ui.viewmodel.EduTechUiState

@Composable
fun HomeScreen(
    state: EduTechUiState,
    onReplayWelcome: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onOpenCourse: (CourseEntity) -> Unit,
    onOpenMedia: (MediaAssetEntity) -> Unit,
    onNavigateCourses: () -> Unit,
    onNavigateAssistant: () -> Unit,
    onNavigateMedia: () -> Unit,
    onNavigateNotes: () -> Unit,
    onNavigateMentor: () -> Unit,
    onNavigateToolkit: () -> Unit,
    onNavigateWebsite: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val introVideo = state.mediaAssets.firstOrNull { it.mediaType == MediaKind.VIDEO.name }
    val introAudio = state.mediaAssets.firstOrNull { it.mediaType == MediaKind.AUDIO.name }
    val completionFraction = if (state.courses.isEmpty()) 0f else {
        state.courses.count { it.isCompleted }.toFloat() / state.courses.size.toFloat()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Top Header with Learner Greeting, XP, Replay Welcome & Theme Switch
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { -20 },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onNavigateMentor)
                                .padding(4.dp)
                                .testTag("home_open_mentor"),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFDFEFE),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.size(48.dp),
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_edutech_official_logo),
                                    contentDescription = "شعار EduTech Mindset الرسمي",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = "أهلاً، ${state.stats.learnerName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "EduTech Mindset • أ. باسل أشرف الفقعاوي",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "${state.stats.xpPoints} XP",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                            IconButton(
                                onClick = onReplayWelcome,
                                modifier = Modifier.testTag("replay_welcome_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "عرض شاشة الترحيب والموشن",
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                            }
                            val isEffectiveDark = state.preferences.resolveDarkTheme(isSystemInDarkTheme())
                            IconButton(
                                onClick = { onToggleTheme(!isEffectiveDark) },
                                modifier = Modifier.testTag("toggle_theme_button"),
                            ) {
                                Icon(
                                    imageVector = if (isEffectiveDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (isEffectiveDark) "التبديل إلى الوضع النهاري" else "التبديل إلى الوضع الليلي",
                                )
                            }
                        }
                    }
                }
            }

            // 2. Hero Multimedia Showcase Card
            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(216.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.edutech_hero_banner_1794771056331),
                            contentDescription = "خلفية EduTech Mindset",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF07111E).copy(alpha = 0.25f),
                                            Color(0xFF07111E).copy(alpha = 0.92f),
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF00D4FF).copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.55f)),
                            ) {
                                Text(
                                    text = "عقلية التعليم والتكنولوجيا • استوديو الوسائط",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF8CEBFF),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "منصتك التعليمية الشاملة بتخزين محلي كامل",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    if (introVideo != null) {
                                        Button(
                                            onClick = { onOpenMedia(introVideo) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("home_play_intro_video"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF00D4FF),
                                                contentColor = Color(0xFF07111E),
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("فيديو الترحيب", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    if (introAudio != null) {
                                        OutlinedButton(
                                            onClick = { onOpenMedia(introAudio) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("home_play_intro_audio"),
                                            border = BorderStroke(1.dp, Color(0xFFFFB703)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD166)),
                                            shape = RoundedCornerShape(14.dp),
                                        ) {
                                            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("بودكاست", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Live Local Storage & Progress Telemetry Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateCourses),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                                AnimatedProgressRing(
                                    progressFraction = completionFraction,
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 5.dp,
                                )
                                Text(
                                    text = "${(completionFraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            Column {
                                Text("الإنجاز", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = "${state.courses.count { it.isCompleted }}/${state.courses.size} مسار",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateMedia),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                modifier = Modifier.size(40.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Column {
                                Text("محلياً", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = "${state.mediaAssets.size} وسائط • ${formatBytesCompact(state.internalStorageBytes)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            // 3b. Quick Personal & Voice Assistant Entry Card
            item {
                Surface(
                    onClick = onNavigateAssistant,
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_open_assistant"),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "المساعد الشخصي والصوتي",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "المساعد الشخصي والصوتي",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "تحدّث أو اسأل عن مساراتك وتقدّمك التعليمي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }

            // 3c. Website Ideas & Portal Strip: Teacher/Student Toolkit + edutech-mindset.pages.dev
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        onClick = onNavigateToolkit,
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_open_toolkit"),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp),
                            )
                            Column {
                                Text("أدوات EduTech", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                Text(
                                    text = "للمعلّم والطالب • بومودورو",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = onNavigateWebsite,
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_open_website"),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp),
                            )
                            Column {
                                Text("موقع المنصة", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                Text(
                                    text = "edutech-mindset.pages.dev",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            // 4. Featured Courses Section
            item {
                SectionHeaderRow(
                    title = "المسارات التعليمية المميزة",
                    actionText = "عرض الكل",
                    onAction = onNavigateCourses,
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.courses.take(5), key = { it.id }) { course ->
                        HomeCoursePreviewCard(
                            course = course,
                            onClick = { onOpenCourse(course) },
                        )
                    }
                }
            }

            // 5. Quick Multimedia Library Strip
            item {
                SectionHeaderRow(
                    title = "مكتبة الوسائط المتعددة",
                    actionText = "استوديو الوسائط",
                    onAction = onNavigateMedia,
                )
            }

            items(state.mediaAssets.take(4), key = { it.id }) { media ->
                HomeMediaRowCard(
                    media = media,
                    onClick = { onOpenMedia(media) },
                )
            }

            // 6. Pinned / Recent Notes Card
            if (state.notes.isNotEmpty()) {
                item {
                    SectionHeaderRow(
                        title = "أحدث الملاحظات المحفوظة",
                        actionText = "دفتر الملاحظات",
                        onAction = onNavigateNotes,
                    )
                }
                item {
                    val topNote = state.notes.first()
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateNotes),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(topNote.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    topNote.tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                            Text(
                                text = topNote.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    actionText: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(
            text = actionText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun HomeCoursePreviewCard(
    course: CourseEntity,
    onClick: () -> Unit,
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
            .width(248.dp)
            .testTag("home_course_card_${course.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
            ) {
                Image(
                    painter = painterResource(id = coverRes),
                    contentDescription = course.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.62f),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart),
                ) {
                    Text(
                        text = course.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                LinearProgressIndicator(
                    progress = { course.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${course.durationMinutes} دقيقة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (course.isCompleted) "مكتمل ✓" else "${course.progressPercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (course.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeMediaRowCard(
    media: MediaAssetEntity,
    onClick: () -> Unit,
) {
    val kind = runCatching { MediaKind.valueOf(media.mediaType) }.getOrDefault(MediaKind.DOCUMENT)
    val icon = when (kind) {
        MediaKind.VIDEO -> Icons.Default.PlayArrow
        MediaKind.AUDIO -> Icons.Default.GraphicEq
        MediaKind.IMAGE -> Icons.Default.PermMedia
        MediaKind.DOCUMENT -> Icons.Default.MenuBook
    }
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_media_card_${media.id}"),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                modifier = Modifier.size(46.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = kind.labelAr, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${kind.labelAr} • ${media.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (media.isBookmarked) {
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = "محفوظ",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

fun formatBytesCompact(bytes: Long): String {
    if (bytes <= 0L) return "جاهز"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(java.util.Locale.US, "%.0f KB", kb)
    val mb = kb / 1024.0
    return String.format(java.util.Locale.US, "%.1f MB", mb)
}
