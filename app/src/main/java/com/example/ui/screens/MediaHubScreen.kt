package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.MediaAssetEntity
import com.example.data.local.MediaKind

@Composable
fun MediaHubScreen(
    mediaAssets: List<MediaAssetEntity>,
    internalStorageBytes: Long,
    onOpenMedia: (MediaAssetEntity) -> Unit,
    onToggleBookmark: (MediaAssetEntity) -> Unit,
    onDeleteMedia: (MediaAssetEntity) -> Unit,
    onImportLocalUri: (uri: Uri, title: String, category: String, description: String) -> Unit,
    onAddExternalLink: (title: String, url: String, kind: MediaKind, category: String, description: String) -> Unit,
) {
    var selectedFilter by remember { mutableStateOf<MediaKind?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }

    val filtered = remember(mediaAssets, selectedFilter) {
        if (selectedFilter == null) mediaAssets
        else mediaAssets.filter { it.mediaType == selectedFilter?.name }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columnCount = if (maxWidth >= 640.dp) 3 else 2

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 820.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "استوديو ومكتبة الوسائط",
                            style = MaterialTheme.typography.headlineMedium,
                        )

                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    modifier = Modifier.size(44.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Storage,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "الخزنة المحلية للوسائط (${mediaAssets.size} ملف)",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        text = "المساحة المستوردة على جهازك: ${formatBytesCompact(internalStorageBytes)} • تعمل بدون إنترنت",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedFilter == null,
                                    onClick = { selectedFilter = null },
                                    label = { Text("الكل (${mediaAssets.size})") },
                                )
                            }
                            items(MediaKind.entries) { kind ->
                                val count = mediaAssets.count { it.mediaType == kind.name }
                                FilterChip(
                                    selected = selectedFilter == kind,
                                    onClick = { selectedFilter = kind },
                                    label = { Text("${kind.labelAr} ($count)") },
                                )
                            }
                        }
                    }
                }

                if (filtered.isEmpty()) {
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
                                Text("لا توجد وسائط في هذا التصنيف", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "اضغط على زر (+) لإضافة فيديو أو صورة أو ملف صوتي أو PDF من هاتفك.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                } else {
                    val rows = filtered.chunked(columnCount)
                    items(rows) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            rowItems.forEach { asset ->
                                MediaAssetGridCard(
                                    asset = asset,
                                    onClick = { onOpenMedia(asset) },
                                    onToggleBookmark = { onToggleBookmark(asset) },
                                    onDelete = { onDeleteMedia(asset) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(columnCount - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showImportDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_media_fab"),
            ) {
                Icon(Icons.Default.Add, contentDescription = "استيراد وسائط")
            }
        }
    }

    if (showImportDialog) {
        ImportMediaDialog(
            onDismiss = { showImportDialog = false },
            onImportLocalUri = { uri, title, cat, desc ->
                onImportLocalUri(uri, title, cat, desc)
                showImportDialog = false
            },
            onAddExternalLink = { title, url, kind, cat, desc ->
                onAddExternalLink(title, url, kind, cat, desc)
                showImportDialog = false
            },
        )
    }
}

@Composable
private fun MediaAssetGridCard(
    asset: MediaAssetEntity,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val kind = runCatching { MediaKind.valueOf(asset.mediaType) }.getOrDefault(MediaKind.DOCUMENT)

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.testTag("media_grid_card_${asset.id}"),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp),
            ) {
                when (kind) {
                    MediaKind.IMAGE -> {
                        AsyncImage(
                            model = asset.uriString,
                            contentDescription = asset.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    MediaKind.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0A2540), Color(0xFF005A8D))
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "فيديو", tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                    MediaKind.AUDIO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2B1B00), Color(0xFF7A4B00))
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.GraphicEq,
                                contentDescription = "صوت",
                                tint = Color(0xFFFFD166),
                                modifier = Modifier.size(44.dp),
                            )
                        }
                    }
                    MediaKind.DOCUMENT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0C2E2B), Color(0xFF106B5E))
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = "مستند",
                                tint = Color(0xFF86EFAC),
                                modifier = Modifier.size(42.dp),
                            )
                        }
                    }
                }

                // Kind Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                ) {
                    Text(
                        text = kind.labelAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }

                // Bookmark Icon
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(34.dp),
                ) {
                    Icon(
                        imageVector = if (asset.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "حفظ",
                        tint = if (asset.isBookmarked) Color(0xFFFFB703) else Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = asset.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (asset.fileSizeBytes > 0) formatBytesCompact(asset.fileSizeBytes) else asset.durationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!asset.isBundled) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "حذف الملف",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportMediaDialog(
    onDismiss: () -> Unit,
    onImportLocalUri: (uri: Uri, title: String, category: String, description: String) -> Unit,
    onAddExternalLink: (title: String, url: String, kind: MediaKind, category: String, description: String) -> Unit,
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var useUrlMode by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf(MediaKind.VIDEO) }
    var category by remember { mutableStateOf("الوسائط التفاعلية") }
    var description by remember { mutableStateOf("") }

    val visualMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedUri = uri
                useUrlMode = false
            }
        },
    )

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                selectedUri = uri
                useUrlMode = false
            }
        },
    )

    val categories = listOf("الوسائط التفاعلية", "القيادة الرقمية", "الذكاء الاصطناعي", "تصميم التعليم")

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
                Text("إضافة وسائط إلى التخزين المحلي", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "اختر ملفاً من جهازك ليُنسخ ويُحفظ محلياً بأمان داخل التطبيق.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Picker Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            visualMediaPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_visual_media_button"),
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("صورة / فيديو", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = {
                            documentPicker.launch(arrayOf("audio/*", "application/pdf", "image/*", "video/*"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_audio_pdf_button"),
                    ) {
                        Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("صوت / PDF", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            useUrlMode = !useUrlMode
                            if (useUrlMode) selectedUri = null
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (useUrlMode) "الوضع الحالي: رابط وسائط خارجي" else "أو اضغط لإضافة رابط فيديو / صوت مباشر",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                if (selectedUri != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "✓ تم اختيار ملف جاهز للحفظ المحلي",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                if (useUrlMode) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("رابط الملف (MP4 / MP3 / صورة) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf(MediaKind.VIDEO, MediaKind.AUDIO, MediaKind.IMAGE)) { k ->
                            FilterChip(
                                selected = selectedKind == k,
                                onClick = { selectedKind = k },
                                label = { Text(k.labelAr) },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (useUrlMode) "عنوان الوسائط *" else "اسم مخصّص (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
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
                    label = { Text("وصف مختصر") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val uri = selectedUri
                            if (uri != null) {
                                onImportLocalUri(uri, title, category, description)
                            } else if (useUrlMode && urlInput.isNotBlank() && title.isNotBlank()) {
                                onAddExternalLink(title, urlInput, selectedKind, category, description)
                            }
                        },
                        enabled = selectedUri != null || (useUrlMode && urlInput.isNotBlank() && title.isNotBlank()),
                        modifier = Modifier.testTag("confirm_import_media_button"),
                    ) {
                        Text("حفظ في المكتبة")
                    }
                }
            }
        }
    }
}
