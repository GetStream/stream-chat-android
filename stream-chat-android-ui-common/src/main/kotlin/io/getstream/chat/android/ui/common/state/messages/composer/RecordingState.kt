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

package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.client.extensions.duration
import io.getstream.chat.android.models.Attachment

public sealed class RecordingState {

    public object Idle : RecordingState() {
        override fun toString(): String = "Recording.Idle"
    }

    public sealed class Recording : RecordingState() {
        public abstract val durationInMs: Int
        public abstract val waveform: List<Float>
    }

    public data class Hold(
        override val durationInMs: Int = 0,
        override val waveform: List<Float> = emptyList(),
        public val offset: Pair<Float, Float> = ZeroOffset,
    ) : Recording() {

        public val offsetX: Float get() = offset.first

        public val offsetY: Float get() = offset.second

        public companion object {
            public val ZeroOffset: Pair<Float, Float> = Pair(0f, 0f)
        }
        override fun toString(): String = "Recording.Hold(" +
            "waveform=${waveform.size}, " +
            "duration=${durationInMs}ms, " +
            "offset=[$offsetX:$offsetY]" +
            ")"
    }

    public data class Locked(
        override val durationInMs: Int = 0,
        override val waveform: List<Float> = emptyList(),
    ) : Recording() {
        override fun toString(): String = "Recording.Locked(waveform=${waveform.size}, duration=${durationInMs}ms)"
    }

    public data class Overview(
        val durationInMs: Int = 0,
        val waveform: List<Float> = emptyList(),
        val attachment: Attachment,
        val isPlaying: Boolean = false,
        val playingProgress: Float = 0f,
        val playingId: Int = -1,
    ) : RecordingState() {

        val hasPlayingId: Boolean get() = playingId != -1

        override fun toString(): String = "Recording.Overview(" +
            "playingId=$playingId, " +
            "waveform=${waveform.size}, " +
            "duration=${durationInMs}ms, " +
            "isPlaying=$isPlaying, " +
            "playingProgress=$playingProgress, " +
            "attachment=${attachment.upload?.hashCode()}" +
            ")"
    }

    public data class Complete(
        val attachment: Attachment,
    ) : RecordingState() {
        override fun toString(): String = "Recording.Complete(" +
            "duration=${attachment.duration}, " +
            "attachment=${attachment.upload}" +
            ")"
    }
}

public fun RecordingState.Recording.copy(
    duration: Int = this.durationInMs,
    waveform: List<Float> = this.waveform,
): RecordingState.Recording = when (this) {
    is RecordingState.Hold -> RecordingState.Hold(duration, waveform, this.offset)
    is RecordingState.Locked -> RecordingState.Locked(duration, waveform)
}
