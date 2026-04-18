package com.rudra.savingbuddy.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class VoiceInputHelper(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var listener: VoiceInputListener? = null

    interface VoiceInputListener {
        fun onResult(amount: Double?, description: String)
        fun onError(error: String)
    }

    fun startListening(listener: VoiceInputListener) {
        this.listener = listener

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError("Voice recognition not available")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(createRecognitionListener())

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            listener.onError("Failed to start voice recognition: ${e.message}")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                parseVoiceInput(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    parseVoiceInput(text)
                }
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                listener?.onError(errorMessage)
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun parseVoiceInput(text: String) {
        val lowerText = text.lowercase()
        
        var amount: Double? = null
        var description = text

        // Extract numbers from text
        val wordsToNumbers = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
            "hundred" to 100, "thousand" to 1000
        )

        // Try to find amount patterns like "500 rupees", "fifty rupees", "five hundred"
        val numberPatterns = listOf(
            Regex("""(\d+(?:\.\d+)?)\s*(?:rupees?|rs?)""", RegexOption.IGNORE_CASE),
            Regex("""(\d+)"""),
        )

        for (pattern in numberPatterns) {
            val match = pattern.find(lowerText)
            if (match != null) {
                amount = match.groupValues[1].toDoubleOrNull()
                break
            }
        }

        // Try word numbers if no digit found
        if (amount == null) {
            var total = 0.0
            for ((word, value) in wordsToNumbers) {
                if (lowerText.contains(word)) {
                    total += value
                }
            }
            if (total > 0) amount = total
        }

        // Clean up description
        description = lowerText
            .replace(Regex("""\d+(?:\.\d+)?"""), "")
            .replace(Regex("""rupees?|rs?\d*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+"""), " ")
            .trim()

        listener?.onResult(amount, description)
    }
}