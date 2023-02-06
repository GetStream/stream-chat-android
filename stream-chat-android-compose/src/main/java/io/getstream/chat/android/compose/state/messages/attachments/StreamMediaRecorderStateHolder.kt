/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.state.messages.attachments

import android.media.MediaRecorder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import com.getstream.sdk.chat.audio.recording.StreamMediaRecorderState
import io.getstream.log.StreamLog
import io.getstream.log.TaggedLogger
import kotlinx.coroutines.flow.StateFlow

/**
 * A wrapper class that wraps around [StreamMediaRecorder] and tracks the internal [MediaRecorder] state using
 * [StateFlow]s.
 *
 * @param streamMediaRecorder The media recorder whose state this class is tracking.
 */
public class StreamMediaRecorderStateHolder(
    private val streamMediaRecorder: StreamMediaRecorder,
) {

    /**
     * The logger used to print information, warnings, errors, etc. to log.
     */
    private val logger: TaggedLogger = StreamLog.getLogger("StreamMediaRecorderStateHolder")

    /**
     * Represents the latest info state of the [MediaRecorder] used by [StreamMediaRecorder].
     */
    private val _onInfoState: MutableState<StreamMediaRecorderState?> = mutableStateOf(null)

    /**
     * Represents the latest info state of the [MediaRecorder] used by [StreamMediaRecorder].
     */
    public val onInfoState: State<StreamMediaRecorderState?> = _onInfoState

    /**
     * Represents the latest error state of the [MediaRecorder] used by [StreamMediaRecorder].
     */
    private val _onErrorState: MutableState<StreamMediaRecorderState?> = mutableStateOf(null)

    /**
     * Represents the latest error state of the [MediaRecorder] used by [StreamMediaRecorder].
     */
    private val onErrorState: State<StreamMediaRecorderState?> = mutableStateOf(null)

    /**
     * Indicated if the [MediaRecorder] is currently recording.
     */
    //TODO possibly refactor this using an enum or a sealed class representing multiple states
    //TODO such as prepared, idle, recording, released, etc.
    private val _isRecording: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Indicated if the [MediaRecorder] is currently recording.
     */
    public val isRecording: State<Boolean> = _isRecording

    init {
        streamMediaRecorder.setOnInfoListener { streamMediaRecorder, what, extra ->
            logger.v { "[onInfo] -> what: $what , extra: $extra" }

            _onInfoState.value = StreamMediaRecorderState(
                streamMediaRecorder = streamMediaRecorder,
                what = what,
                extra = extra
            )
        }

        streamMediaRecorder.setOnErrorListener { streamMediaRecorder, what, extra ->
            logger.v { "[onError] -> what: $what , extra: $extra" }

            _onErrorState.value = StreamMediaRecorderState(
                streamMediaRecorder = streamMediaRecorder,
                what = what,
                extra = extra
            )
        }

        streamMediaRecorder.setOnRecordingStartedListener {
            logger.v { "[onStarted] -> started" }

            _isRecording.value = true
        }

        streamMediaRecorder.setOnRecordingStoppedListener {
            logger.v { "[onStopped] -> stopped" }

            _isRecording.value = false
        }
    }
}
