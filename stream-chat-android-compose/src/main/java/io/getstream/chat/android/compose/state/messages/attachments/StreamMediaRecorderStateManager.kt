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
import com.getstream.sdk.chat.audio.recording.MediaRecorderState
import com.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import com.getstream.sdk.chat.audio.recording.StreamMediaRecorderState
import io.getstream.log.StreamLog
import io.getstream.log.TaggedLogger

/**
 * A wrapper class that wraps around [StreamMediaRecorder] manages and tracks the internal state of the [MediaRecorder]
 * used by [StreamMediaRecorder].
 *
 * For instance, this class helps automatically recover from [MediaRecorder.MEDIA_ERROR_SERVER_DIED] by releasing the
 * current instance of [streamMediaRecorder].
 *
 * @param streamMediaRecorder The media recorder whose state this class is tracking.
 */
public class StreamMediaRecorderStateManager(
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
     * Emits the latest [MediaRecorder] max amplitude reading.
     */
    private val _latestMaxAmplitude: MutableState<Int> = mutableStateOf(0)

    /**
     * Emits the latest [MediaRecorder] max amplitude reading.
     */
    public val latestMaxAmplitude: State<Int> = _latestMaxAmplitude

    /**
     * Represents the current state of the [MediaRecorder].
     */
    private val _mediaRecorderState: MutableState<MediaRecorderState> =
        mutableStateOf(MediaRecorderState.UNINITIALIZED)

    /**
     * Represents the current state of the [MediaRecorder].
     */
    public val mediaRecorderState: State<MediaRecorderState> = _mediaRecorderState

    init {
        streamMediaRecorder.setOnInfoListener { streamMediaRecorder, what, extra ->
            logger.v { "[setOnInfoListener] -> what: $what , extra: $extra" }

            _onInfoState.value = StreamMediaRecorderState(
                streamMediaRecorder = streamMediaRecorder,
                what = what,
                extra = extra
            )
        }

        streamMediaRecorder.setOnErrorListener { streamMediaRecorder, what, extra ->
            logger.v { "[setOnErrorListener] -> what: $what , extra: $extra" }

            if (what == MediaRecorder.MEDIA_ERROR_SERVER_DIED) {
                streamMediaRecorder.release()
            }

            _onErrorState.value = StreamMediaRecorderState(
                streamMediaRecorder = streamMediaRecorder,
                what = what,
                extra = extra
            )
        }

        streamMediaRecorder.setOnMaxAmplitudeSampledListener {
            logger.v { "[setOnMaxAmplitudeSampledListener] -> $it" }

            _latestMaxAmplitude.value = it
        }

        streamMediaRecorder.setOnMediaRecorderStateChangedListener {
            logger.v { "[setOnMediaRecorderStateChangedListener] -> ${it.name}" }

            _mediaRecorderState.value = it
        }
    }
}
