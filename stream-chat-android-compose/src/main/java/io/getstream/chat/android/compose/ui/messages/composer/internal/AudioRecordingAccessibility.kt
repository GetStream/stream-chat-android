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

package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState

/**
 * Sets the localized content description (which includes the press-and-hold gesture hint) and
 * tags the node as a button so TalkBack announces "Button".
 *
 * The actual recording is started by the press-and-hold gesture on `pointerInput`; lifecycle
 * announcements ("Recording started" / "Recording cancelled") are emitted by
 * [AnnounceRecordingTransitions], not by this modifier.
 */
@Composable
internal fun Modifier.micButtonSemantics(): Modifier {
    val buttonDescription = stringResource(R.string.stream_compose_audio_recording_start)
    return this.semantics {
        contentDescription = buttonDescription
        role = Role.Button
    }
}

/**
 * Observes [recordingState] transitions and emits one-shot TalkBack announcements:
 * - `Idle → Hold` announces "Recording started"
 * - `non-Idle → Idle` announces "Recording cancelled" only when [cancelRequested] is `true`
 *
 * The caller flips [cancelRequested] on when the user invokes a cancel or delete action; this
 * distinguishes a real cancellation from the confirm / send paths, which also transition back
 * to `Idle` but emit through a brief `Complete` state that Compose batches away.
 *
 * @param recordingState Current recording state to observe.
 * @param cancelRequested `true` when the user invoked a cancel or delete action since the last
 *  transition was processed.
 * @param onTransitionConsumed Invoked after each processed transition so the caller can reset
 *  [cancelRequested] to `false`.
 */
@Composable
internal fun AnnounceRecordingTransitions(
    recordingState: RecordingState,
    cancelRequested: Boolean,
    onTransitionConsumed: () -> Unit,
) {
    val view = LocalView.current
    val startedAnnouncement = stringResource(R.string.stream_compose_audio_recording_state_started)
    val cancelledAnnouncement = stringResource(R.string.stream_compose_audio_recording_state_cancelled)
    var previousWasIdle by remember { mutableStateOf(true) }
    val currentIsIdle = recordingState is RecordingState.Idle
    val currentIsHold = recordingState is RecordingState.Hold

    LaunchedEffect(currentIsIdle, currentIsHold) {
        val wasIdle = previousWasIdle
        previousWasIdle = currentIsIdle
        when (recordingState) {
            is RecordingState.Hold -> if (wasIdle) {
                view.announceForAccessibility(startedAnnouncement)
            }
            is RecordingState.Idle -> if (!wasIdle && cancelRequested) {
                view.announceForAccessibility(cancelledAnnouncement)
            }
            is RecordingState.Locked,
            is RecordingState.Overview,
            is RecordingState.Complete,
            -> Unit
        }
        onTransitionConsumed()
    }
}
