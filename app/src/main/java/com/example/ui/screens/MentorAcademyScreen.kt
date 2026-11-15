package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.viewmodel.EduTechUiState

@Composable
fun MentorAcademyScreen(
    state: EduTechUiState,
    firebaseUserName: String?,
    firebaseUserEmail: String?,
    cloudSyncStatus: com.example.ui.viewmodel.CloudSyncStatus,
    onSignOut: () -> Unit,
    onUpdateLearnerName: (String) -> Unit,
    onUpdateFounderAvatar: (Uri) -> Unit,
    onSetThemeMode: (com.example.data.local.ThemeMode) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onToggleHighMotion: (Boolean) -> Unit,
    onReplayWelcome: () -> Unit,
    onOpenWebsite: () -> Unit,
    onOpenToolkit: () -> Unit,
) {
    val context = LocalContext.current
    var nameInput by remember(state.stats.learnerName) { mutableStateOf(state.stats.learnerName) }
    var showFullPortrait by remember { mutableStateOf(false) }
    val founderPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) onUpdateFounderAvatar(uri)
        },
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Mentor Spotlight Card — الأستاذ باسل الفقعاوي
            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        ),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(104.dp),
                        ) {
                            AsyncImage(
                                model = R.drawable.edutech_hero_banner_1794771056331,
                                contentDescription = "هوية EduTech Mindset",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                            )
                                        )
                                    )
                            )
                        }
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            AsyncImage(
                                model = state.preferences.founderAvatarUri.ifBlank { R.drawable.img_instructor_avatar_1794764865940 },
                                contentDescription = "الأستاذ باسل أشرف الفقعاوي",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(78.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                                    .clickable { showFullPortrait = true },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = "الأستاذ باسل أشرف الفقعاوي",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "موثّق",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Text(
                                    text = "مؤسس ومدير منصة EduTech Mindset",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                                Text(
                                    text = "عقلية التعليم والتكنولوجيا",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFDFEFE),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.size(56.dp),
                            ) {
                                AsyncImage(
                                    model = R.drawable.img_edutech_official_logo,
                                    contentDescription = "شعار EduTech Mindset",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                )
                            }
                        }

                        Text(
                            text = "رؤيتنا هي تمكين المعلّمين والمتعلّمين من بناء عقلية رقمية متطورة تجمع بين المنهجية التربوية الأصيلة وأحدث تقنيات التعليم والوسائط المتعددة.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        OutlinedButton(
                            onClick = {
                                founderPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mentor_pick_founder_photo"),
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("تعيين أو تحديث صورة المؤسس من جهازك")
                        }
                    }
                    }
                }
            }

            // 1b. Official Website & Teacher/Student Toolkit Connection Card
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("الربط الرسمي بموقع EduTech Mindset", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(EDUTECH_WEBSITE_URL, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Button(
                            onClick = onOpenWebsite,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mentor_open_website_in_app"),
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.open_website_in_app))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { openUrlExternally(context, EDUTECH_WEBSITE_URL) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mentor_open_website_external"),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.open_website_external))
                            }
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "منصة EduTech Mindset بإدارة الأستاذ باسل الفقعاوي: $EDUTECH_WEBSITE_URL")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة المنصة"))
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("مشاركة")
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenToolkit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mentor_open_toolkit"),
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("فتح مختبر أدوات المعلّم والطالب")
                        }
                    }
                }
            }

            // 2. Learner Stats & Editable Local Name
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text("ملف المتعلّم المحلي", style = MaterialTheme.typography.titleLarge)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            StatPill(
                                icon = Icons.Default.AutoAwesome,
                                label = "نقاط الخبرة",
                                value = "${state.stats.xpPoints} XP",
                                modifier = Modifier.weight(1f),
                            )
                            StatPill(
                                icon = Icons.Default.LocalFireDepartment,
                                label = "الاستمرارية",
                                value = "${state.stats.streakDays} يوم",
                                modifier = Modifier.weight(1f),
                            )
                            StatPill(
                                icon = Icons.Default.School,
                                label = "الدروس المنجزة",
                                value = "${state.stats.completedLessons}",
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("اسمك في المنصة") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                onClick = { onUpdateLearnerName(nameInput) },
                                enabled = nameInput.isNotBlank() && nameInput != state.stats.learnerName,
                                modifier = Modifier.testTag("save_learner_name_button"),
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "حفظ الاسم")
                            }
                        }
                    }
                }
            }

            // 3. Appearance, Material 3 Dynamic Color & Motion Graphics
            item {
                val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                val effectiveDark = state.preferences.resolveDarkTheme(isSystemInDarkTheme())
                val modeOptions = listOf(
                    Triple(com.example.data.local.ThemeMode.LIGHT, "نهاري", Icons.Default.LightMode),
                    Triple(com.example.data.local.ThemeMode.DARK, "ليلي", Icons.Default.DarkMode),
                    Triple(com.example.data.local.ThemeMode.SYSTEM, "النظام", Icons.Default.BrightnessAuto),
                )

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("المظهر الديناميكي والموشن جرافك", style = MaterialTheme.typography.titleLarge)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = if (effectiveDark) "داكن" else "فاتح",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }

                        // 3-Way Material 3 Theme Mode Switcher
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            modeOptions.forEachIndexed { index, (mode, label, icon) ->
                                val tag = when (mode) {
                                    com.example.data.local.ThemeMode.LIGHT -> "theme_mode_light"
                                    com.example.data.local.ThemeMode.DARK -> "theme_mode_dark"
                                    com.example.data.local.ThemeMode.SYSTEM -> "theme_mode_system"
                                }
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = modeOptions.size,
                                    ),
                                    onClick = { onSetThemeMode(mode) },
                                    selected = state.preferences.themeMode == mode,
                                    icon = {
                                        SegmentedButtonDefaults.Icon(active = state.preferences.themeMode == mode) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag(tag),
                                ) {
                                    Text(label, maxLines = 1)
                                }
                            }
                        }

                        // Material 3 Dynamic Color (Material You) Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("ألوان Material 3 الديناميكية", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = if (supportsDynamicColor) {
                                            "توليد الألوان تلقائياً من خلفية جهازك (Android 12+)"
                                        } else {
                                            "تُفعّل تلقائياً على Android 12+ مع لوحة EduTech الافتراضية"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Switch(
                                checked = state.preferences.dynamicColorEnabled,
                                onCheckedChange = onToggleDynamicColor,
                                modifier = Modifier.testTag("dynamic_color_switch"),
                            )
                        }

                        // Live Active ColorScheme Swatch Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (state.preferences.dynamicColorEnabled && supportsDynamicColor) {
                                    "لوحة Material You النشطة"
                                } else {
                                    "لوحة EduTech Mindset النشطة"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    MaterialTheme.colorScheme.primary to "اللون الأساسي",
                                    MaterialTheme.colorScheme.secondary to "اللون الثانوي",
                                    MaterialTheme.colorScheme.tertiary to "اللون الثالثي",
                                    MaterialTheme.colorScheme.primaryContainer to "حاوية اللون الأساسي",
                                ).forEach { (swatchColor, desc) ->
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape),
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(Icons.Default.MotionPhotosOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Column {
                                    Text("رسوميات الحركة والموشن جرافك", style = MaterialTheme.typography.titleSmall)
                                    Text("خلفيات تفاعلية وحلقات متحركة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = state.preferences.isHighMotionEnabled,
                                onCheckedChange = onToggleHighMotion,
                            )
                        }

                        OutlinedButton(
                            onClick = onReplayWelcome,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mentor_replay_welcome_button"),
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("إعادة عرض شاشة الترحيب والموشن جرافك")
                        }
                    }
                }
            }

            // 4. Cloud Firestore Sync, Google Account & Local Storage Status
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = firebaseUserName?.takeIf { it.isNotBlank() } ?: "حساب Google الموثّق",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (!firebaseUserEmail.isNullOrBlank()) {
                                    Text(
                                        text = firebaseUserEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    text = when (cloudSyncStatus) {
                                        is com.example.ui.viewmodel.CloudSyncStatus.Syncing -> "جاري الاتصال بـ Cloud Firestore..."
                                        is com.example.ui.viewmodel.CloudSyncStatus.Synced ->
                                            "مزامنة Firestore نشطة ✓ (${cloudSyncStatus.cloudNotesCount} ملاحظة • ${cloudSyncStatus.cloudProgressCount} تقدم مسار)"
                                        is com.example.ui.viewmodel.CloudSyncStatus.Error ->
                                            "تنبيه المزامنة: ${cloudSyncStatus.message}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (cloudSyncStatus is com.example.ui.viewmodel.CloudSyncStatus.Error) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Column {
                                Text("التخزين المحلي على الهاتف: نشط ✓", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${state.courses.size} مسار • ${state.mediaAssets.size} ملف وسائط • ${state.notes.size} ملاحظة • ${formatBytesCompact(state.internalStorageBytes)} وسائط مستوردة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_out_button"),
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("تسجيل الخروج من الحساب")
                        }
                    }
                }
            }
        }

        if (showFullPortrait) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showFullPortrait = false }) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AsyncImage(
                            model = state.preferences.founderAvatarUri.ifBlank { R.drawable.img_basel_founder_portrait },
                            contentDescription = "الأستاذ باسل أشرف الفقعاوي",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .clip(MaterialTheme.shapes.large),
                        )
                        Text("الأستاذ باسل أشرف الفقعاوي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("مؤسس ومدير منصة EduTech Mindset", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        OutlinedButton(onClick = { showFullPortrait = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("إغلاق")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
