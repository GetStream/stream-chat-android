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

package io.getstream.chat.android.compose.ui.messages.composer.actions

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel

/**
 * Represents the actions that can be performed on an audio recording.
 *
 * @property onStartRecording Handler when the user starts recording an audio message.
 * @property onHoldRecording Handler when the user holds the recording button.
 * @property onLockRecording Handler when the user locks the recording.
 * @property onCancelRecording Handler when the user cancels the recording.
 * @property onDeleteRecording Handler when the user deletes the recording.
 * @property onStopRecording Handler when the user stops the recording.
 * @property onCompleteRecording Handler when the user completes the recording.
 * @property onToggleRecordingPlayback Handler when the user toggles the recording playback.
 * @property onRecordingSliderDragStart Handler when the user starts dragging the recording slider.
 * @property onRecordingSliderDragStop Handler when the user stops dragging the recording slider.
 * @property onSendRecording Handler when the user sends the recording.
 */
@Immutable
public data class AudioRecordingActions(
    val onStartRecording: (Offset) -> Unit,
    val onHoldRecording: (Offset) -> Unit,
    val onLockRecording: () -> Unit,
    val onCancelRecording: () -> Unit,
    val onDeleteRecording: () -> Unit,
    val onStopRecording: () -> Unit,
    val onCompleteRecording: (Boolean) -> Unit,
    val onToggleRecordingPlayback: () -> Unit,
    val onRecordingSliderDragStart: (Float) -> Unit,
    val onRecordingSliderDragStop: (Float) -> Unit,
    val onSendRecording: () -> Unit,
) {
    public companion object {

        /**
         * No-op implementation of [AudioRecordingActions].
         */
        public val None: AudioRecordingActions = AudioRecordingActions(
            onStartRecording = {},
            onHoldRecording = {},
            onLockRecording = {},
            onCancelRecording = {},
            onDeleteRecording = {},
            onStopRecording = {},
            onCompleteRecording = {},
            onToggleRecordingPlayback = {},
            onRecordingSliderDragStart = {},
            onRecordingSliderDragStop = {},
            onSendRecording = {},
        )

        /**
         * Default implementation of [AudioRecordingActions].
         */
        public fun defaultActions(
            viewModel: MessageComposerViewModel,
        ): AudioRecordingActions = AudioRecordingActions(
            onStartRecording = { viewModel.startRecording(it.toRestrictedCoordinates()) },
            onHoldRecording = { viewModel.holdRecording(it.toRestrictedCoordinates()) },
            onLockRecording = { viewModel.lockRecording() },
            onCancelRecording = { viewModel.cancelRecording() },
            onDeleteRecording = { viewModel.cancelRecording() },
            onStopRecording = { viewModel.stopRecording() },
            onCompleteRecording = { sendOnComplete ->
                if (sendOnComplete) viewModel.sendRecording() else viewModel.completeRecording()
            },
            onToggleRecordingPlayback = { viewModel.toggleRecordingPlayback() },
            onRecordingSliderDragStart = { viewModel.pauseRecording() },
            onRecordingSliderDragStop = { viewModel.seekRecordingTo(it) },
            onSendRecording = { viewModel.sendRecording() },
        )
    }
}

private fun Offset.toRestrictedCoordinates(): Pair<Float, Float> = x.coerceAtMost(0f) to y.coerceAtMost(0f)
