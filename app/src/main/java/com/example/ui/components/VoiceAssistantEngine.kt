package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceAssistantEngine(
    private val context: Context,
    private val onListeningChanged: (Boolean) -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onSpeakingChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            mainHandler.post {
                if (status == TextToSpeech.SUCCESS) {
                    val arResult = textToSpeech?.setLanguage(Locale.forLanguageTag("ar"))
                    if (arResult == TextToSpeech.LANG_MISSING_DATA || arResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech?.language = Locale.getDefault()
                    }
                    textToSpeech?.setSpeechRate(1.0f)
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            mainHandler.post { onSpeakingChanged(true) }
                        }

                        override fun onDone(utteranceId: String?) {
                            mainHandler.post { onSpeakingChanged(false) }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            mainHandler.post { onSpeakingChanged(false) }
                        }
                    })
                    isTtsReady = true
                }
            }
        }
    }

    fun isSpeechRecognitionAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("التعرّف الصوتي غير متاح على هذا الجهاز حالياً. يمكنك الكتابة مباشرة.")
            return
        }
        stopSpeaking()
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = onListeningChanged(true)
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    onRmsChanged(normalized)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    onListeningChanged(false)
                    onRmsChanged(0f)
                }

                override fun onError(error: Int) {
                    onListeningChanged(false)
                    onRmsChanged(0f)
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "لم أتمكن من سماع صوت واضح، حاول مجدداً."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "انتهى وقت الاستماع دون كلام."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "يرجى منح إذن الميكروفون للتحدث."
                        else -> "تعذّر بدء الاستماع الصوتي (رمز $error)."
                    }
                    onError(msg)
                }

                override fun onResults(results: Bundle?) {
                    onListeningChanged(false)
                    onRmsChanged(0f)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull().orEmpty()
                    if (spoken.isNotBlank()) {
                        onFinalResult(spoken)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    if (partial.isNotBlank()) {
                        onPartialResult(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-JO")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        onListeningChanged(false)
        onRmsChanged(0f)
    }

    fun speak(text: String) {
        if (!isTtsReady || text.isBlank()) return
        val cleanText = text.replace("[رد تجريبي محلي]", "").trim()
        textToSpeech?.speak(
            cleanText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "edutech_tts_${System.currentTimeMillis()}",
        )
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        onSpeakingChanged(false)
    }

    fun release() {
        mainHandler.removeCallbacksAndMessages(null)
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
