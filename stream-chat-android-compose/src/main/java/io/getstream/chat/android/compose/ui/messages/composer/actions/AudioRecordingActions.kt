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
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState

/**
 * Actions that can be performed during an audio recording session.
 *
 * Each property maps a user gesture or button tap to a handler.
 * Override individual actions via [copy] to customise behaviour while keeping the rest at their defaults.
 *
 * @property onStartRecording Begins a new recording.
 *  Transitions from [RecordingState.Idle] to [RecordingState.Hold].
 *  Ignored when the current state is not [RecordingState.Idle].
 * @property onHoldRecording Updates the drag offset while the user holds the record button.
 *  The [Offset] represents the constrained drag delta from the initial press position
 *  (negative x = cancel direction, negative y = lock direction).
 *  Only meaningful in [RecordingState.Hold].
 * @property onLockRecording Locks the recording so it continues hands-free.
 *  Transitions from [RecordingState.Hold] to [RecordingState.Locked].
 * @property onCancelRecording Discards the recording via the swipe-to-cancel gesture.
 *  Invoked when the user drags past the cancel threshold during [RecordingState.Hold].
 *  Transitions to [RecordingState.Idle].
 * @property onDeleteRecording Discards the recording via the delete button.
 *  Invoked when the user taps the trash icon in [RecordingState.Locked] or [RecordingState.Overview].
 *  Transitions to [RecordingState.Idle].
 * @property onStopRecording Stops the active microphone recording.
 *  Transitions from [RecordingState.Locked] to [RecordingState.Overview],
 *  where the user can review the waveform before confirming.
 * @property onConfirmRecording Finalises the recording.
 *  Depending on configuration, this either sends the recording immediately
 *  or attaches it to the composer for manual sending.
 *  Invoked on finger release (when not locked) or when tapping the confirm button in
 *  [RecordingState.Locked] / [RecordingState.Overview].
 * @property onToggleRecordingPlayback Toggles play / pause of the recorded audio.
 *  Only meaningful in [RecordingState.Overview].
 * @property onRecordingSliderDragStart Called when the user begins dragging the playback slider.
 *  Pauses playback. The [Float] is the playback progress at the drag start point (0..1).
 * @property onRecordingSliderDragStop Called when the user releases the playback slider.
 *  Seeks playback to the given progress. The [Float] is the target progress (0..1).
 */
@Immutable
public data class AudioRecordingActions(
    val onStartRecording: () -> Unit,
    val onHoldRecording: (Offset) -> Unit,
    val onLockRecording: () -> Unit,
    val onCancelRecording: () -> Unit,
    val onDeleteRecording: () -> Unit,
    val onStopRecording: () -> Unit,
    val onConfirmRecording: () -> Unit,
    val onToggleRecordingPlayback: () -> Unit,
    val onRecordingSliderDragStart: (Float) -> Unit,
    val onRecordingSliderDragStop: (Float) -> Unit,
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
            onConfirmRecording = {},
            onToggleRecordingPlayback = {},
            onRecordingSliderDragStart = {},
            onRecordingSliderDragStop = {},
        )

        /**
         * Default implementation backed by [viewModel].
         *
         * @param viewModel The [MessageComposerViewModel] that drives recording state.
         * @param sendOnComplete When `true`, [onConfirmRecording] sends the message immediately.
         *  When `false`, it attaches the recording to the composer for manual sending.
         */
        public fun defaultActions(
            viewModel: MessageComposerViewModel,
            sendOnComplete: Boolean,
        ): AudioRecordingActions = AudioRecordingActions(
            onStartRecording = viewModel::startRecording,
            onHoldRecording = { viewModel.holdRecording(it.toRestrictedCoordinates()) },
            onLockRecording = viewModel::lockRecording,
            onCancelRecording = viewModel::cancelRecording,
            onDeleteRecording = viewModel::cancelRecording,
            onStopRecording = viewModel::stopRecording,
            onConfirmRecording = {
                if (sendOnComplete) viewModel.sendRecording() else viewModel.completeRecording()
            },
            onToggleRecordingPlayback = viewModel::toggleRecordingPlayback,
            onRecordingSliderDragStart = { viewModel.pauseRecording() },
            onRecordingSliderDragStop = viewModel::seekRecordingTo,
        )
    }
}

private fun Offset.toRestrictedCoordinates(): Pair<Float, Float> = x.coerceAtMost(0f) to y.coerceAtMost(0f)
