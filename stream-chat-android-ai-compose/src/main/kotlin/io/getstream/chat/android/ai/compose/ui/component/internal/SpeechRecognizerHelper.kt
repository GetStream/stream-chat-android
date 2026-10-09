/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.ai.compose.util.internal.simpleLogger
import java.util.Locale

/**
 * Interface for speech recognition helper.
 * Manages speech recognition using Android's SpeechRecognizer API.
 */
internal interface SpeechRecognizerHelper {
    val isListening: Boolean get() = false
    val rmsdB: Float get() = 0f

    fun startListening(): Boolean = false

    fun stopListening() {
        // No-op
    }

    fun cancel() {
        // No-op
    }

    fun release() {
        // No-op
    }
}

/**
 * Default implementation of SpeechRecognizerHelper, extending from ViewModel so that it can survive
 * configuration changes.
 */
@SuppressLint("StaticFieldLeak") // We are using the application context
private class SpeechRecognizerHelperViewModel(
    private val context: Context,
) : SpeechRecognizerHelper, ViewModel() {
    private val logger = simpleLogger("SpeechRecognizerHelper")

    private var onPartialResult: ((String) -> Unit)? = null
    private var onFinalResult: ((String) -> Unit)? = null

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = mutableStateOf(false)
    override val isListening by _isListening

    private val _rmsdB = mutableFloatStateOf(0f)
    override val rmsdB by _rmsdB

    private var speechResults = mutableListOf<String>()

    fun setCallbacks(
        onPartialResult: ((String) -> Unit)?,
        onFinalResult: ((String) -> Unit)?,
    ) {
        this.onPartialResult = onPartialResult
        this.onFinalResult = onFinalResult
    }

    @Suppress("TooGenericExceptionCaught")
    override fun startListening(): Boolean {
        if (_isListening.value) {
            return false
        }

        speechRecognizer = speechRecognizer ?: createRecognizer() ?: run {
            logger.w { "Speech recognition is not available on this device" }
            return false
        }

        return try {
            resetState()
            speechRecognizer?.startListening(createRecognitionIntent())
            _isListening.value = true
            true
        } catch (e: Exception) {
            logger.e(e) { "Failed to start listening: ${e.message}" }
            false
        }
    }

    override fun stopListening() {
        if (!_isListening.value) {
            return
        }
        speechRecognizer?.stopListening()
        // Don't reset state here - onResults() will be called asynchronously
        // and will handle the reset after processing the final result
        _isListening.value = false
    }

    override fun cancel() {
        speechRecognizer?.cancel()
        resetState()
    }

    override fun release() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        resetState()
    }

    override fun onCleared() {
        release()
    }

    private fun createRecognizer(): SpeechRecognizer? =
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        } else {
            null
        }

    private fun createRecognitionListener(): RecognitionListener = object : RecognitionListener {
        // 1. Called first - recognizer is ready to listen
        override fun onReadyForSpeech(params: Bundle?) {
            logger.d { "onReadyForSpeech: params=${params?.getData()}" }
        }

        // 2. Called when user starts speaking
        override fun onBeginningOfSpeech() {
            logger.d { "onBeginningOfSpeech: User started speaking" }
        }

        // 3. Called repeatedly during speech - provides audio level updates
        override fun onRmsChanged(rmsdB: Float) {
            _rmsdB.floatValue = rmsdB
        }

        // 4. Optional - may not be called, receives audio buffer
        override fun onBufferReceived(buffer: ByteArray?) {
            logger.d { "onBufferReceived: buffer size=${buffer?.size ?: 0}" }
        }

        // 5. Optional - called multiple times if EXTRA_PARTIAL_RESULTS is enabled
        override fun onPartialResults(partialResults: Bundle?) {
            logger.d { "onPartialResults: ${partialResults?.getData()}" }

            extractResult(partialResults)
                ?.takeIf(String::isNotBlank)
                ?.let { result ->
                    onPartialResult?.invoke(speechResults.joinToString() + result)

                    if (partialResults.isFinalResult()) {
                        speechResults += result
                    }
                }
        }

        // 6. Called when user stops speaking
        override fun onEndOfSpeech() {
            logger.d { "onEndOfSpeech: User stopped speaking" }
        }

        // 7. Called last (in normal flow) - provides final recognition results
        override fun onResults(results: Bundle?) {
            logger.d { "onResults: ${results?.getData()}" }

            onFinalResult?.invoke(speechResults.joinToString())

            resetState()
        }

        // 8. Can be called at any time if an error occurs
        override fun onError(error: Int) {
            val errorMessage = getErrorMessage(error)
            logger.e { "onError: $errorMessage" }

            resetState()
        }

        // 9. Optional - handles additional events
        override fun onEvent(eventType: Int, params: Bundle?) {
            logger.d { "onEvent: eventType=$eventType, params=${params?.getData()}" }
        }
    }

    private fun resetState() {
        _isListening.value = false
        _rmsdB.floatValue = 0f
        speechResults.clear()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SpeechRecognizerHelperViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return SpeechRecognizerHelperViewModel(context = context.applicationContext) as T
        }
    }
}

@Suppress("DEPRECATION")
private fun Bundle.getData(): String =
    keySet().joinToString { key -> "$key=${get(key)}" }

@Composable
internal fun rememberSpeechRecognizerHelper(
    onPartialResult: ((String) -> Unit)? = null,
    onFinalResult: (String) -> Unit,
): SpeechRecognizerHelper {
    val isPreview = LocalInspectionMode.current
    val activity = LocalActivity.current
    val context = LocalContext.current

    val helper = if (isPreview) {
        remember { object : SpeechRecognizerHelper {} }
    } else {
        viewModel(
            modelClass = SpeechRecognizerHelperViewModel::class.java,
            // Prioritize activity scope which supports configuration changes
            viewModelStoreOwner = activity?.getViewModelStoreOwner()
                ?: checkNotNull(LocalViewModelStoreOwner.current) { // Fallback to the default store
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                },
            factory = SpeechRecognizerHelperViewModel.Factory(context),
        ).apply {
            setCallbacks(
                onPartialResult = onPartialResult,
                onFinalResult = onFinalResult,
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Keep the recognizer across configuration changes; release on navigation/real disposal
            if (activity?.isChangingConfigurations != true) {
                helper.release()
            }
        }
    }

    return helper
}

private fun Activity.getViewModelStoreOwner(): ViewModelStoreOwner? =
    (this as? ComponentActivity)?.let {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = it.viewModelStore
        }
    }

private const val SilenceTimeoutMs = 3000

private fun createRecognitionIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, SilenceTimeoutMs)
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, SilenceTimeoutMs)
}

private fun extractResult(bundle: Bundle?): String? =
    bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

private fun Bundle?.isFinalResult(): Boolean =
    this?.getBoolean("final_result", false) ?: false

private fun getErrorMessage(error: Int): String = when (error) {
    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
    SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT -> "The service does not allow to check for support."
    SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS ->
        "The service does not support listening to model downloads events."
    SpeechRecognizer.ERROR_CLIENT -> "Other client side errors."
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ->
        "Requested language is not available to be used with the current recognizer."
    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ->
        "Requested language is supported, but not available currently (e.g. not downloaded yet)."
    SpeechRecognizer.ERROR_NETWORK -> "Other network related errors."
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network operation timed out."
    SpeechRecognizer.ERROR_NO_MATCH -> "No recognition result matched."
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy."
    SpeechRecognizer.ERROR_SERVER -> "Server sends error status."
    SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "Server has been disconnected, e.g. because the app has crashed."
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
    SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> "Too many requests from the same client."
    else -> "Unknown error: $error"
}
