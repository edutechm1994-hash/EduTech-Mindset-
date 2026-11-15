package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.CourseWithModules
import com.example.data.local.MediaAssetEntity
import com.example.data.local.MediaKind
import com.example.data.local.ModuleEntity
import com.example.data.local.ModuleWithProgress
import com.example.ui.components.AudioAccessibilityCard
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.CourseAudioFormatter
import com.example.ui.components.VideoPlayer
import com.example.ui.components.rememberAccessibilityTtsEngine

@Composable
fun CourseModuleScreen(
    courseWithModules: CourseWithModules,
    activeModuleWithProgress: ModuleWithProgress,
    onBack: () -> Unit,
    onSelectModule: (ModuleEntity) -> Unit,
    onToggleModuleComplete: (ModuleEntity, Boolean) -> Unit,
    onPlaybackPositionChanged: (ModuleEntity, Long, Long) -> Unit,
    onAttachLocalVideo: (ModuleEntity, Uri) -> Unit,
    onAttachVideoUrl: (ModuleEntity, String) -> Unit,
    onOpenFullscreenMedia: (MediaAssetEntity) -> Unit,
) {
    BackHandler { onBack() }

    val orderedModules = courseWithModules.orderedModules
    val currentIndex = orderedModules.indexOfFirst { it.module.id == activeModuleWithProgress.module.id }
        .coerceAtLeast(0)
    val module = activeModuleWithProgress.module
    val progress = activeModuleWithProgress.progress
    val isDone = progress?.isCompleted == true
    val kind = runCatching { MediaKind.valueOf(module.mediaType) }.getOrDefault(MediaKind.DOCUMENT)

    val ttsEngine = rememberAccessibilityTtsEngine()
    androidx.compose.runtime.DisposableEffect(module.id) {
        onDispose { ttsEngine.stop() }
    }

    var showAttachUrlDialog by remember { mutableStateOf(false) }

    val localVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onAttachLocalVideo(module, uri)
            }
        },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
            ) {
                // Top Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("module_screen_back"),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "العودة للمسار")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = courseWithModules.course.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "الوحدة ${currentIndex + 1} من ${orderedModules.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isDone) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = "${progress?.progressPercent ?: 0}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isDone) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                // Scrollable Module Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 1. Embedded Multimedia Section
                    when {
                        kind == MediaKind.VIDEO && module.mediaUri.isNotBlank() -> {
                            VideoPlayer(
                                videoSource = module.mediaUri,
                                autoPlay = false,
                                initialPositionMs = progress?.lastPositionMs ?: 0L,
                                onPositionChanged = { pos, dur ->
                                    onPlaybackPositionChanged(module, pos, dur)
                                },
                                onPlaybackEnded = {
                                    if (!isDone) {
                                        onToggleModuleComplete(module, false)
                                    }
                                },
                                onToggleFullscreen = {
                                    onOpenFullscreenMedia(
                                        MediaAssetEntity(
                                            id = -20_000L - module.id,
                                            title = module.title,
                                            category = courseWithModules.course.category,
                                            mediaType = MediaKind.VIDEO.name,
                                            uriString = module.mediaUri,
                                            description = module.summary,
                                        )
                                    )
                                },
                            )
                        }
                        kind == MediaKind.AUDIO && module.mediaUri.isNotBlank() -> {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                        Text("محتوى صوتي تفاعلي للوحدة", style = MaterialTheme.typography.titleSmall)
                                    }
                                    AudioWaveformVisualizer(
                                        isPlaying = false,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(64.dp),
                                    )
                                    Button(
                                        onClick = {
                                            onOpenFullscreenMedia(
                                                MediaAssetEntity(
                                                    id = -20_000L - module.id,
                                                    title = module.title,
                                                    category = courseWithModules.course.category,
                                                    mediaType = MediaKind.AUDIO.name,
                                                    uriString = module.mediaUri,
                                                    description = module.summary,
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("تشغيل في استوديو الصوت")
                                    }
                                }
                            }
                        }
                        kind == MediaKind.IMAGE && module.mediaUri.isNotBlank() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.large)
                                    .clickable {
                                        onOpenFullscreenMedia(
                                            MediaAssetEntity(
                                                id = -20_000L - module.id,
                                                title = module.title,
                                                category = courseWithModules.course.category,
                                                mediaType = MediaKind.IMAGE.name,
                                                uriString = module.mediaUri,
                                                description = module.summary,
                                            )
                                        )
                                    },
                            ) {
                                AsyncImage(
                                    model = module.mediaUri,
                                    contentDescription = module.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                        else -> {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("وحدة معرفية نصية", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = "يمكنك إرفاق فيديو من هاتفك أو رابط فيديو لهذه الوحدة في أي وقت.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Attach / Switch Video Source Controls (Local File or URL)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                localVideoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("attach_local_video_button"),
                        ) {
                            Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("فيديو من الهاتف", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = { showAttachUrlDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("attach_url_video_button"),
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("رابط فيديو URL", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    // 3. Module Title & Curriculum Content + Auditory Accessibility Reader
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${module.summary} • ${module.durationMinutes} دقيقة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )

                    AudioAccessibilityCard(
                        title = stringResource(R.string.tts_module_reader_title),
                        primaryItemId = "module_summary_${module.id}",
                        primaryText = CourseAudioFormatter.formatModuleSummaryNarration(module),
                        primaryButtonLabel = stringResource(R.string.tts_listen_module_summary),
                        secondaryItemId = "module_full_${module.id}",
                        secondaryText = CourseAudioFormatter.formatModuleFullNarration(module),
                        secondaryButtonLabel = stringResource(R.string.tts_listen_full_module),
                        ttsEngine = ttsEngine,
                        modifier = Modifier.testTag("module_screen_tts_card"),
                    )

                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text("المحتوى التعليمي للوحدة", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = module.contentBody,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                    // 4. Overall Course Progress Bar + Module Complete Button
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("إجمالي تقدم المسار", style = MaterialTheme.typography.labelLarge)
                            Text("${courseWithModules.computedProgressPercent}%", style = MaterialTheme.typography.labelLarge)
                        }
                        LinearProgressIndicator(
                            progress = { courseWithModules.computedProgressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                        )
                    }

                    Button(
                        onClick = { onToggleModuleComplete(module, isDone) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("module_screen_complete_button"),
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isDone) "الوحدة مكتملة ✓ (إلغاء)" else "إتمام هذه الوحدة (+25 XP)")
                    }
                }

                // Bottom Prev/Next Module Navigation Bar
                if (orderedModules.size > 1) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (currentIndex > 0) {
                                        onSelectModule(orderedModules[currentIndex - 1].module)
                                    }
                                },
                                enabled = currentIndex > 0,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("السابقة")
                            }
                            Text(
                                text = "${currentIndex + 1} / ${orderedModules.size}",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Button(
                                onClick = {
                                    if (currentIndex < orderedModules.lastIndex) {
                                        onSelectModule(orderedModules[currentIndex + 1].module)
                                    }
                                },
                                enabled = currentIndex < orderedModules.lastIndex,
                            ) {
                                Text("التالية")
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAttachUrlDialog) {
        AttachVideoUrlDialog(
            initialUrl = if (module.mediaType == MediaKind.VIDEO.name) module.mediaUri else "",
            onDismiss = { showAttachUrlDialog = false },
            onConfirm = { url ->
                onAttachVideoUrl(module, url)
                showAttachUrlDialog = false
            },
        )
    }
}

@Composable
private fun AttachVideoUrlDialog(
    initialUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var urlInput by remember { mutableStateOf(initialUrl) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("ربط فيديو بالوحدة (URL)", style = MaterialTheme.typography.titleLarge)
                }
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("رابط MP4 أو HLS مباشر") },
                    placeholder = { Text("https://example.com/lesson.mp4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(urlInput.trim()) },
                        enabled = urlInput.isNotBlank(),
                    ) {
                        Text("تشغيل وحفظ")
                    }
                }
            }
        }
    }
}
