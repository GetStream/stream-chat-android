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

package io.getstream.chat.android.ai.compose.ui.component

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.ui.component.internal.SpeechRecognizerHelper
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberSpeechRecognizerHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * A composable button that provides speech-to-text functionality.
 *
 * When not recording, displays a microphone icon button.
 * When recording, transforms into a circle with animated bars that respond to voice input.
 *
 * The component automatically handles:
 * - Audio permission requests
 * - Starting and stopping speech recognition
 * - Streaming recognized text chunks to the callback as they're detected
 * - Animated visualization during recording
 *
 * @param state The state holder for tracking recording status. Must be created with [rememberSpeechToTextButtonState].
 * @param modifier Modifier to be applied to the root container
 * @param onPermissionDenied Callback invoked when microphone permission is denied.
 * @param idleContent The composable content to display when not recording. Receives an onClick callback
 *   that toggles recording.
 * @param recordingContent The composable content to display when recording. Receives an onClick callback
 *   that stops recording and the current audio level (rmsdB) for visualization.
 *
 * @see SpeechToTextButtonState
 * @see rememberSpeechToTextButtonState
 */
@Composable
public fun SpeechToTextButton(
    state: SpeechToTextButtonState,
    modifier: Modifier = Modifier,
    onPermissionDenied: () -> Unit = { },
    idleContent: @Composable (onClick: () -> Unit) -> Unit = { onClick ->
        with(LocalChatAiComponentFactory.current) {
            SpeechToTextButtonIdleContent(SpeechToTextButtonIdleContentParams(onClick = onClick))
        }
    },
    recordingContent: @Composable (
        onClick: () -> Unit,
        rmsdB: Float,
    ) -> Unit = { onClick, rmsdB ->
        with(LocalChatAiComponentFactory.current) {
            SpeechToTextButtonRecordingContent(
                SpeechToTextButtonRecordingContentParams(onClick = onClick, rmsdB = rmsdB),
            )
        }
    },
) {
    val context = LocalContext.current
    val speechRecognizerHelper = state.helper

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            speechRecognizerHelper.startListening()
        } else {
            onPermissionDenied()
        }
    }

    val hasPermission = remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
    }

    val onToggleRecording: () -> Unit = {
        when {
            state.isRecording() -> speechRecognizerHelper.stopListening()

            hasPermission -> speechRecognizerHelper.startListening()

            else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    AnimatedContent(
        targetState = state.isRecording(),
        modifier = modifier,
    ) { isRecording ->
        if (isRecording) {
            recordingContent(onToggleRecording, state.rmsdB)
        } else {
            idleContent(onToggleRecording)
        }
    }
}

@Composable
internal fun DefaultIdleContent(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.stream_ai_compose_ic_mic),
            contentDescription = stringResource(R.string.stream_ai_compose_speech_to_text_idle_button),
        )
    }
}

@Composable
internal fun DefaultRecordingContent(
    onClick: () -> Unit,
    rmsdB: Float,
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        VoiceRecordingBars(rmsdB = rmsdB)
    }
}

/**
 * State holder for [SpeechToTextButton] that tracks the recording status.
 *
 * Use [rememberSpeechToTextButtonState] to create an instance of this state.
 *
 * @see rememberSpeechToTextButtonState
 * @see SpeechToTextButton
 */
public class SpeechToTextButtonState internal constructor(
    internal val helper: SpeechRecognizerHelper,
) {
    /**
     * Returns the current audio level in decibels.
     *
     * @return The RMS audio level in decibels (typically 0-10)
     */
    internal val rmsdB: Float get() = helper.rmsdB

    /**
     * Returns whether the speech-to-text button is currently recording.
     *
     * @return `true` if recording is in progress, `false` otherwise
     */
    public fun isRecording(): Boolean = helper.isListening
}

/**
 * Creates and remembers a [SpeechToTextButtonState] instance.
 *
 * The state is remembered across recompositions and can be used to track the recording status
 * of a [SpeechToTextButton].
 *
 * @param onPartialResult Callback invoked when text chunks are recognized.
 * Called with each partial result as speech is detected, enabling real-time text streaming.
 * @param onFinalResult Callback invoked when the final result is available.
 * @return A remembered instance of [SpeechToTextButtonState]
 *
 * @see SpeechToTextButtonState
 * @see SpeechToTextButton
 */
@Composable
public fun rememberSpeechToTextButtonState(
    onPartialResult: ((String) -> Unit)? = null,
    onFinalResult: (String) -> Unit,
): SpeechToTextButtonState {
    val helper = rememberSpeechRecognizerHelper(
        onPartialResult = onPartialResult,
        onFinalResult = onFinalResult,
    )
    return remember(helper) {
        SpeechToTextButtonState(helper)
    }
}

/**
 * Draws animated vertical bars that simulate voice input visualization.
 * The bars animate randomly with heights influenced by the audio level.
 */
@Suppress("MagicNumber")
@Composable
private fun VoiceRecordingBars(
    rmsdB: Float,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.onPrimary
    val barCount = 5
    val barAnimatables = remember {
        List(barCount) { Animatable(0.3f) }
    }

    // Normalize rmsdB to a scale factor (0.5 to 1.5)
    // Typical rmsdB ranges from 0 to 10
    val audioScale = 0.5f + (rmsdB.coerceIn(0f, 10f) / 10f)

    LaunchedEffect(barAnimatables) {
        barAnimatables.forEachIndexed { index, animatable ->
            launch {
                // Stagger the start times
                delay(index * 100L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 300 + (Random.nextInt(200)),
                            easing = LinearEasing,
                        ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                )
            }
        }
    }

    val contentDescription = stringResource(R.string.stream_ai_compose_speech_to_text_recording_button)

    Canvas(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .size(24.dp),
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / (barCount * 2 - 1)
        val spacing = barWidth
        val maxBarHeight = canvasHeight * 0.7f
        val minBarHeight = canvasHeight * 0.2f

        barAnimatables.forEachIndexed { index, animatable ->
            val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * animatable.value * audioScale
            val x = index * (barWidth + spacing)
            val y = (canvasHeight - barHeight) / 2

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
            )
        }
    }
}

@Composable
internal fun SpeechToTextButtonIdle() {
    val state = rememberSpeechToTextButtonState { }
    SpeechToTextButton(state = state)
}

@Composable
internal fun SpeechToTextButtonRecording() {
    val state = remember {
        SpeechToTextButtonState(
            helper = object : SpeechRecognizerHelper {
                override val isListening = true
                override val rmsdB = 5f
            },
        )
    }
    SpeechToTextButton(state = state)
}

@Preview(showBackground = true)
@Composable
private fun SpeechToTextButtonIdlePreview() {
    SpeechToTextButtonIdle()
}

@Preview(showBackground = true)
@Composable
private fun SpeechToTextButtonRecordingPreview() {
    SpeechToTextButtonRecording()
}
