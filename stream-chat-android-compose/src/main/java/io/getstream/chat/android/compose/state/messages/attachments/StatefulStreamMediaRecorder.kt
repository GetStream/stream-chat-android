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

import android.content.Context
import android.media.MediaRecorder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.getstream.sdk.chat.audio.recording.MediaRecorderState
import com.getstream.sdk.chat.audio.recording.RecordedMedia
import com.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import com.getstream.sdk.chat.audio.recording.StreamMediaRecorderState
import io.getstream.chat.android.compose.util.KeyValuePair
import io.getstream.chat.android.models.Attachment
import io.getstream.log.StreamLog
import io.getstream.log.TaggedLogger
import io.getstream.result.Result
import java.io.File

/**
 * A wrapper class that wraps around [StreamMediaRecorder] manages and tracks the internal state of the [MediaRecorder]
 * used by [StreamMediaRecorder].
 *
 * For instance, this class helps automatically recover from [MediaRecorder.MEDIA_ERROR_SERVER_DIED] by releasing the
 * current instance of [streamMediaRecorder].
 *
 * @param streamMediaRecorder The media recorder whose state this class is tracking.
 */
public class StatefulStreamMediaRecorder(
    private val streamMediaRecorder: StreamMediaRecorder,
) {

    /**
     * The ordinal number of a max amplitude sample.
     * Used to update composition when the same value is emitted twice.
     */
    private var maxAmplitudeSampleKey by mutableStateOf(0)

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
    public val onErrorState: State<StreamMediaRecorderState?> = _onErrorState

    /**
     * Emits the latest [MediaRecorder] max amplitude reading.
     */
    private val _latestMaxAmplitude: MutableState<KeyValuePair<Int, Int>> = mutableStateOf(KeyValuePair(0, 0))

    /**
     * Emits the latest [MediaRecorder] max amplitude reading.
     */
    public val latestMaxAmplitude: State<KeyValuePair<Int, Int>> = _latestMaxAmplitude

    /**
     * Represents the current state of the [MediaRecorder].
     */
    private val _mediaRecorderState: MutableState<MediaRecorderState> =
        mutableStateOf(MediaRecorderState.UNINITIALIZED)

    /**
     * Represents the current state of the [MediaRecorder].
     */
    public val mediaRecorderState: State<MediaRecorderState> = _mediaRecorderState

    /**
     * Represents the duration of the currently active recording.
     */
    private val _activeRecordingDuration: MutableState<Long> =
        mutableStateOf(0L)

    /**
     * Represents the duration of the currently active recording.
     */
    public val activeRecordingDuration: State<Long> = _activeRecordingDuration

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

            _latestMaxAmplitude.value = KeyValuePair(maxAmplitudeSampleKey, it)
            maxAmplitudeSampleKey += 1
        }

        streamMediaRecorder.setOnMediaRecorderStateChangedListener {
            logger.v { "[setOnMediaRecorderStateChangedListener] -> ${it.name}" }

            maxAmplitudeSampleKey = 0
            _mediaRecorderState.value = it
        }

        streamMediaRecorder.setOnCurrentRecordingDurationChangedListener {
            logger.v { "[setOnCurrentRecordingDurationChangedListener] -> $it" }

            _activeRecordingDuration.value = it
        }
    }

    /**
     * Creates a [File] internally and starts recording.
     * Calling the function again after a recording has already been started will reset the recording process.
     *
     * @param context The [Context] necessary to prepare for recording.
     * @param recordingName The file name the recording will be stored under.
     * @param override Determines if the new recording file should override one with the same name, if it exists.
     *
     * @return The [File] to which the recording will be stored wrapped inside a [Result] if recording has
     * started successfully. Returns a [ChatError] wrapped inside a [Result] if the action had failed.
     */
    public fun startAudioRecording(
        context: Context,
        recordingName: String,
        override: Boolean = true,
    ): Result<File> =
        streamMediaRecorder.startAudioRecording(
            recordingName = recordingName,
            override = override
        )

    /**
     * Prepares the given [recordingFile] and starts recording.
     * Calling the function again after a recording has already been started will reset the recording process.
     *
     * @param context The [Context] necessary to prepare for recording.
     * @param recordingFile The [File] the audio will be saved to once the recording stops.
     *
     * @return A Unit wrapped inside a [Result] if recording has started successfully. Returns a [ChatError] wrapped
     * inside [Result] if the action had failed.
     */
    public fun startAudioRecording(
        context: Context,
        recordingFile: File,
    ): Result<Unit> = streamMediaRecorder.startAudioRecording(
        recordingFile = recordingFile
    )

    /**
     * Stops recording and saves the recording to the file provided by [startAudioRecording].
     *
     * @return A Unit wrapped inside a [Result] if recording has been stopped successfully. Returns a [ChatError]
     * wrapped inside [Result] if the action had failed.
     */
    public fun stopRecording(): Result<RecordedMedia> =
        streamMediaRecorder.stopRecording()

    /**
     * Deleted the recording to the file provided by [recordingFile].
     *
     * @param recordingFile The [File] to be deleted.
     *
     * @return A Unit wrapped inside a [Result] if recording has been deleted successfully. Returns a [ChatError]
     * wrapped inside [Result] if the action had failed.
     */
    public fun deleteRecording(recordingFile: File): Result<Unit> =
        streamMediaRecorder.deleteRecording(recordingFile)

    /**
     * Releases the [MediaRecorder] used by [StreamMediaRecorder].
     */
    public fun release() {
        streamMediaRecorder.release()
    }
}
