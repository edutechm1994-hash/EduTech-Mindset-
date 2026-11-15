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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.MediaAssetEntity
import com.example.data.local.MediaKind
import com.example.data.local.StudyNoteEntity

@Composable
fun NotesScreen(
    notes: List<StudyNoteEntity>,
    onSaveNote: (existingId: Long?, title: String, content: String, tag: String, accent: String, attachedUri: Uri?, existingMediaUri: String, existingMediaType: String) -> Unit,
    onTogglePin: (StudyNoteEntity) -> Unit,
    onDeleteNote: (StudyNoteEntity) -> Unit,
    onOpenAttachedMedia: (MediaAssetEntity) -> Unit,
) {
    var search by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<StudyNoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    val filtered = remember(notes, search) {
        if (search.isBlank()) notes
        else notes.filter {
            it.title.contains(search, ignoreCase = true) ||
                it.content.contains(search, ignoreCase = true) ||
                it.tag.contains(search, ignoreCase = true)
        }
    }

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
                    Text("دفتر الملاحظات والأفكار", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("ابحث في ملاحظاتك المحلية...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_search_input"),
                    )
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
                            Text("لا توجد ملاحظات بعد", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "دوّن أفكارك وملخصاتك التعليمية مع إمكانية إرفاق صور أو فيديو محلي.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = {
                            editingNote = note
                            showEditor = true
                        },
                        onTogglePin = { onTogglePin(note) },
                        onDelete = { onDeleteNote(note) },
                        onOpenMedia = {
                            if (note.attachedMediaUri.isNotBlank()) {
                                onOpenAttachedMedia(
                                    MediaAssetEntity(
                                        id = -1000L - note.id,
                                        title = note.title,
                                        category = note.tag,
                                        mediaType = note.attachedMediaType.ifBlank { MediaKind.IMAGE.name },
                                        uriString = note.attachedMediaUri,
                                        description = note.content,
                                    )
                                )
                            }
                        },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingNote = null
                showEditor = true
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_note_fab"),
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة ملاحظة")
        }
    }

    if (showEditor) {
        NoteEditorDialog(
            existing = editingNote,
            onDismiss = { showEditor = false },
            onSave = { id, title, content, tag, accent, uri, prevUri, prevType ->
                onSaveNote(id, title, content, tag, accent, uri, prevUri, prevType)
                showEditor = false
            },
        )
    }
}

@Composable
private fun NoteCard(
    note: StudyNoteEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onOpenMedia: () -> Unit,
) {
    val accentColor = when (note.accentKey) {
        "gold" -> MaterialTheme.colorScheme.secondary
        "emerald" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentColor.copy(alpha = 0.16f),
                ) {
                    Text(
                        text = note.tag,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                Row {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "تثبيت الملاحظة",
                            tint = if (note.isPinned) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف الملاحظة",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Text(note.title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )

            if (note.attachedMediaUri.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenMedia),
                ) {
                    if (note.attachedMediaType == MediaKind.IMAGE.name) {
                        AsyncImage(
                            model = note.attachedMediaUri,
                            contentDescription = "مرفق الملاحظة",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("ملف وسائط مرفق — اضغط للعرض", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    existing: StudyNoteEntity?,
    onDismiss: () -> Unit,
    onSave: (Long?, String, String, String, String, Uri?, String, String) -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var tag by remember { mutableStateOf(existing?.tag ?: "ملخص درس") }
    var accent by remember { mutableStateOf(existing?.accentKey ?: "cyan") }
    var newUri by remember { mutableStateOf<Uri?>(null) }

    val tags = listOf("ملخص درس", "فكرة ملهمة", "خطة تطبيق", "سؤال للبحث")
    val accents = listOf("cyan" to "أزرق سماوي", "gold" to "ذهبي", "emerald" to "أخضر")

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) newUri = uri },
    )

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
                Text(
                    text = if (existing == null) "تدوين ملاحظة جديدة" else "تعديل الملاحظة",
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("العنوان *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("المحتوى والأفكار *") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tags) { t ->
                        FilterChip(
                            selected = tag == t,
                            onClick = { tag = t },
                            label = { Text(t) },
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(accents) { (key, label) ->
                        FilterChip(
                            selected = accent == key,
                            onClick = { accent = key },
                            label = { Text(label) },
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        mediaPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when {
                            newUri != null -> "تم اختيار مرفق جديد ✓"
                            !existing?.attachedMediaUri.isNullOrBlank() -> "استبدال المرفق الحالي"
                            else -> "إرفاق صورة أو فيديو من الهاتف"
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                existing?.id,
                                title,
                                content,
                                tag,
                                accent,
                                newUri,
                                existing?.attachedMediaUri ?: "",
                                existing?.attachedMediaType ?: "",
                            )
                        },
                        enabled = title.isNotBlank() || content.isNotBlank(),
                        modifier = Modifier.testTag("save_note_button"),
                    ) {
                        Text("حفظ محلياً")
                    }
                }
            }
        }
    }
}
