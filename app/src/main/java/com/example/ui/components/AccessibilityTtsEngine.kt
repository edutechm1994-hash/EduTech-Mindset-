package com.example.ui.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CourseEntity
import com.example.data.local.ModuleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TtsPlaybackState(
    val isReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val activeItemId: String? = null,
    val speechRate: Float = 1.0f,
    val errorMessage: String? = null,
)

object CourseAudioFormatter {
    fun formatCourseNarration(course: CourseEntity): String {
        return buildString {
            append(course.title)
            if (course.subtitle.isNotBlank()) {
                append(". ")
                append(course.subtitle)
            }
            if (course.description.isNotBlank()) {
                append(". ")
                append(course.description)
            }
            append(". المدة التقديرية ")
            append(course.durationMinutes)
            append(" دقيقة.")
        }
    }

    fun formatModuleSummaryNarration(module: ModuleEntity): String {
        return buildString {
            append(module.title)
            if (module.summary.isNotBlank()) {
                append(". ")
                append(module.summary)
            }
        }
    }

    fun formatModuleFullNarration(module: ModuleEntity): String {
        return buildString {
            append(module.title)
            if (module.summary.isNotBlank()) {
                append(". الملخص: ")
                append(module.summary)
            }
            if (module.contentBody.isNotBlank()) {
                append(". المحتوى التعليمي: ")
                append(module.contentBody)
            }
        }
    }
}

class AccessibilityTtsEngine(context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val _state = MutableStateFlow(TtsPlaybackState())
    val state: StateFlow<TtsPlaybackState> = _state.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private var pendingUtterance: Pair<String, String>? = null

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            mainHandler.post {
                if (status == TextToSpeech.SUCCESS) {
                    val engine = textToSpeech
                    if (engine != null) {
                        val arabicLocale = Locale.forLanguageTag("ar")
                        val langResult = engine.setLanguage(arabicLocale)
                        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            engine.language = Locale.getDefault()
                        }
                        engine.setSpeechRate(_state.value.speechRate)
                        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                mainHandler.post {
                                    _state.value = _state.value.copy(
                                        isSpeaking = true,
                                        activeItemId = utteranceId,
                                        errorMessage = null,
                                    )
                                }
                            }

                            override fun onDone(utteranceId: String?) {
                                mainHandler.post {
                                    if (_state.value.activeItemId == utteranceId) {
                                        _state.value = _state.value.copy(
                                            isSpeaking = false,
                                            activeItemId = null,
                                        )
                                    }
                                }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                mainHandler.post {
                                    _state.value = _state.value.copy(
                                        isSpeaking = false,
                                        activeItemId = null,
                                        errorMessage = "تعذّر تشغيل القارئ الصوتي",
                                    )
                                }
                            }
                        })
                        _state.value = _state.value.copy(isReady = true, errorMessage = null)
                        val queued = pendingUtterance
                        if (queued != null) {
                            pendingUtterance = null
                            speak(queued.first, queued.second)
                        }
                    }
                } else {
                    _state.value = _state.value.copy(
                        isReady = false,
                        isSpeaking = false,
                        errorMessage = "محرّك القراءة الصوتية غير متاح على هذا الجهاز",
                    )
                }
            }
        }
    }

    fun speak(itemId: String, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        if (!_state.value.isReady) {
            pendingUtterance = itemId to clean
            return
        }
        textToSpeech?.setSpeechRate(_state.value.speechRate)
        _state.value = _state.value.copy(
            isSpeaking = true,
            activeItemId = itemId,
            errorMessage = null,
        )
        textToSpeech?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, itemId)
    }

    fun toggleSpeak(itemId: String, text: String) {
        val current = _state.value
        if (current.isSpeaking && current.activeItemId == itemId) {
            stop()
        } else {
            speak(itemId, text)
        }
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.75f, 1.5f)
        _state.value = _state.value.copy(speechRate = clamped)
        textToSpeech?.setSpeechRate(clamped)
    }

    fun stop() {
        pendingUtterance = null
        textToSpeech?.stop()
        _state.value = _state.value.copy(isSpeaking = false, activeItemId = null)
    }

    fun release() {
        pendingUtterance = null
        mainHandler.removeCallbacksAndMessages(null)
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _state.value = TtsPlaybackState()
    }
}

@Composable
fun rememberAccessibilityTtsEngine(): AccessibilityTtsEngine {
    val context = LocalContext.current
    val engine = remember(context) { AccessibilityTtsEngine(context) }
    DisposableEffect(engine) {
        onDispose { engine.release() }
    }
    return engine
}

@Composable
fun TtsIconToggleButton(
    itemId: String,
    textToRead: String,
    ttsEngine: AccessibilityTtsEngine,
    contentDescriptionListen: String,
    contentDescriptionStop: String = stringResource(R.string.tts_stop_reading),
    modifier: Modifier = Modifier,
) {
    val state by ttsEngine.state.collectAsState()
    val isThisPlaying = state.isSpeaking && state.activeItemId == itemId

    IconButton(
        onClick = { ttsEngine.toggleSpeak(itemId, textToRead) },
        modifier = modifier.minimumInteractiveComponentSize(),
    ) {
        Icon(
            imageVector = if (isThisPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = if (isThisPlaying) contentDescriptionStop else contentDescriptionListen,
            tint = if (isThisPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun AudioAccessibilityCard(
    title: String,
    primaryItemId: String,
    primaryText: String,
    primaryButtonLabel: String,
    ttsEngine: AccessibilityTtsEngine,
    secondaryItemId: String? = null,
    secondaryText: String? = null,
    secondaryButtonLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    val state by ttsEngine.state.collectAsState()
    val isPrimaryActive = state.isSpeaking && state.activeItemId == primaryItemId
    val isSecondaryActive = secondaryItemId != null && state.isSpeaking && state.activeItemId == secondaryItemId
    val isAnyActive = isPrimaryActive || isSecondaryActive

    val speeds = listOf(
        0.85f to stringResource(R.string.tts_speed_slow),
        1.0f to stringResource(R.string.tts_speed_normal),
        1.25f to stringResource(R.string.tts_speed_fast),
    )

    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (isAnyActive) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
        },
        border = BorderStroke(
            1.dp,
            if (isAnyActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = if (isAnyActive) Icons.Default.GraphicEq else Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = if (isAnyActive) stringResource(R.string.tts_reading_now) else title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (isAnyActive) {
                    OutlinedButton(
                        onClick = { ttsEngine.stop() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tts_stop_all_button"),
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.tts_stop_reading))
                    }
                }
            }

            AnimatedVisibility(visible = isAnyActive) {
                AudioWaveformVisualizer(
                    isPlaying = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { ttsEngine.toggleSpeak(primaryItemId, primaryText) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPrimaryActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = if (isPrimaryActive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("tts_primary_action_$primaryItemId"),
                ) {
                    Icon(
                        imageVector = if (isPrimaryActive) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isPrimaryActive) stringResource(R.string.tts_stop_reading) else primaryButtonLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (secondaryItemId != null && !secondaryText.isNullOrBlank() && !secondaryButtonLabel.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { ttsEngine.toggleSpeak(secondaryItemId, secondaryText) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("tts_secondary_action_$secondaryItemId"),
                    ) {
                        Icon(
                            imageVector = if (isSecondaryActive) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isSecondaryActive) stringResource(R.string.tts_stop_reading) else secondaryButtonLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "سرعة القراءة:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                speeds.forEach { (rate, label) ->
                    FilterChip(
                        selected = kotlin.math.abs(state.speechRate - rate) < 0.05f,
                        onClick = { ttsEngine.setSpeechRate(rate) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("tts_speed_$label"),
                    )
                }
            }

            if (!state.errorMessage.isNullOrBlank()) {
                Text(
                    text = state.errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
