package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.VoiceAssistantEngine
import com.example.ui.viewmodel.AssistantChatMessage

@Composable
fun AssistantScreen(
    messages: List<AssistantChatMessage>,
    isThinking: Boolean,
    latestSpokenReply: AssistantChatMessage?,
    onSendPrompt: (String) -> Unit,
    onOpenSuggestedCourse: (Long) -> Unit,
    onShowNotice: (String) -> Unit,
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var autoSpeakEnabled by remember { mutableStateOf(true) }
    var rmsLevel by remember { mutableFloatStateOf(0f) }

    val listState = rememberLazyListState()

    val voiceEngine = remember(context) {
        VoiceAssistantEngine(
            context = context,
            onListeningChanged = { isListening = it },
            onPartialResult = { partial -> inputText = partial },
            onFinalResult = { spoken ->
                inputText = ""
                onSendPrompt(spoken)
            },
            onRmsChanged = { rmsLevel = it },
            onSpeakingChanged = { isSpeaking = it },
            onError = { err -> onShowNotice(err) },
        )
    }

    DisposableEffect(voiceEngine) {
        onDispose { voiceEngine.release() }
    }

    LaunchedEffect(latestSpokenReply?.id) {
        if (autoSpeakEnabled && latestSpokenReply != null && !latestSpokenReply.isFromUser) {
            voiceEngine.speak(latestSpokenReply.text)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                voiceEngine.startListening()
            } else {
                onShowNotice("تم رفض إذن الميكروفون؛ يمكنك استخدام الكتابة أو تفعيل الإذن من الإعدادات.")
            }
        },
    )

    fun toggleVoiceInput() {
        if (isListening) {
            voiceEngine.stopListening()
        } else {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                voiceEngine.startListening()
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val orbScale by animateFloatAsState(
        targetValue = if (isListening) 1.08f + (rmsLevel * 0.28f) else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "voice_orb_scale",
    )

    val quickPrompts = listOf(
        "ما تقدمي الحالي؟",
        "اقترح لي درساً الآن",
        "لخص ملاحظاتي",
        "الذكاء الاصطناعي",
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 1. Header + Auto-TTS Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                    Column {
                        Text("المساعد الشخصي والصوتي", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = if (isListening) "أستمع إليك الآن..." else if (isSpeaking) "يتحدّث المساعد..." else "تحدّث أو اكتب سؤالك",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                IconButton(
                    onClick = {
                        autoSpeakEnabled = !autoSpeakEnabled
                        if (!autoSpeakEnabled) voiceEngine.stopSpeaking()
                    },
                    modifier = Modifier.testTag("assistant_tts_toggle"),
                ) {
                    Icon(
                        imageVector = if (autoSpeakEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = if (autoSpeakEnabled) "إيقاف القراءة الصوتية التلقائية" else "تفعيل القراءة الصوتية",
                        tint = if (autoSpeakEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 2. Transparent Firebase AI Notice Banner
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "المساعد يعمل بالردود المحلية التجريبية؛ تتطلب قدرات Gemini السحابية تفعيل Firebase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            // 3. Animated Voice Waveform Strip when Listening or Speaking
            AnimatedVisibility(visible = isListening || isSpeaking) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AudioWaveformVisualizer(
                            isPlaying = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                        )
                        OutlinedButton(
                            onClick = {
                                if (isListening) voiceEngine.stopListening()
                                if (isSpeaking) voiceEngine.stopSpeaking()
                            },
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "إيقاف", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("إيقاف")
                        }
                    }
                }
            }

            // 4. Quick Prompt Suggestions
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickPrompts) { prompt ->
                    AssistChip(
                        onClick = { onSendPrompt(prompt) },
                        label = { Text(prompt) },
                    )
                }
            }

            // 5. Chat Feed
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    AssistantMessageBubble(
                        message = msg,
                        onSpeak = { voiceEngine.speak(msg.text) },
                        onOpenSuggestedCourse = onOpenSuggestedCourse,
                    )
                }
                if (isThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("جاري تحليل مساراتك...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // 6. Voice Orb + Text Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledIconButton(
                    onClick = { toggleVoiceInput() },
                    modifier = Modifier
                        .size(52.dp)
                        .scale(orbScale)
                        .testTag("assistant_voice_mic_button"),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        contentColor = if (isListening) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondary,
                    ),
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "إيقاف الاستماع" else "تحدث الآن",
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("اسأل المساعد الشخصي...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("assistant_text_input"),
                )

                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendPrompt(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("assistant_send_button"),
                    shape = CircleShape,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال")
                }
            }
        }
    }
}

@Composable
private fun AssistantMessageBubble(
    message: AssistantChatMessage,
    onSpeak: () -> Unit,
    onOpenSuggestedCourse: (Long) -> Unit,
) {
    val isUser = message.isFromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp,
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = if (isUser) 2.dp else 4.dp,
            modifier = Modifier.widthIn(max = 520.dp),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                )
                if (!isUser) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "استماع للرد",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        if (message.suggestedCourseId != null) {
                            OutlinedButton(
                                onClick = { onOpenSuggestedCourse(message.suggestedCourseId) },
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("افتح المسار المقترح")
                            }
                        }
                    }
                }
            }
        }
    }
}
