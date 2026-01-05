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

package io.getstream.sdk.chat.audio.recording

import android.media.MediaRecorder
import io.getstream.result.Result
import java.io.File

/**
 * A media recording interface, designed to simplify using [MediaRecorder].
 */
public interface StreamMediaRecorder {

    /**
     * Creates a [File] internally and starts recording.
     * Calling the function again after a recording has already been started will reset the recording process.
     *
     * @param recordingName The file name the recording will be stored under.
     * @param amplitudePollingInterval Dictates how often the recorder is polled for the latest max amplitude and
     * how often [OnMaxAmplitudeSampled] emits a new value.
     * @param override Determines if the new recording file should override one with the same name, if it exists.
     *
     * @return The [File] to which the recording will be stored wrapped inside a [Result] if recording has
     * started successfully. Returns a [ChatError] wrapped inside a [Result] if the action had failed.
     */
    public fun startAudioRecording(
        recordingName: String,
        amplitudePollingInterval: Long = 100L,
        override: Boolean = true,
    ): Result<File>

    /**
     * Prepares the given [recordingFile] and starts recording.
     * Calling the function again after a recording has already been started will reset the recording process.
     *
     * @param recordingFile The [File] the audio will be saved to once the recording stops.
     * @param amplitudePollingInterval Dictates how often the recorder is polled for the latest max amplitude and
     * how often [OnMaxAmplitudeSampled] emits a new value.
     *
     * @return A Unit wrapped inside a [Result] if recording has started successfully. Returns a [ChatError] wrapped
     * inside [Result] if the action had failed.
     */
    public fun startAudioRecording(
        recordingFile: File,
        amplitudePollingInterval: Long = 100L,
    ): Result<Unit>

    /**
     * Stops recording and saves the recording to the file provided by [startAudioRecording].
     *
     * @return A Unit wrapped inside a [Result] if recording has been stopped successfully. Returns a [ChatError]
     * wrapped inside [Result] if the action had failed.
     */
    public fun stopRecording(): Result<RecordedMedia>

    /**
     * Deleted the recording to the file provided by [recordingFile].
     *
     * @param recordingFile The [File] to be deleted.
     *
     * @return A Unit wrapped inside a [Result] if recording has been deleted successfully. Returns a [ChatError]
     * wrapped inside [Result] if the action had failed.
     */
    public fun deleteRecording(recordingFile: File): Result<Unit>

    /**
     * Releases the [MediaRecorder] used by [StreamMediaRecorder].
     */
    public fun release()

    // TODO add an onDestroy method, should kill the coroutine scope inside DefaultStreamMediaRecorder

    /**
     * Sets an error listener.
     *
     * @param onErrorListener [StreamMediaRecorder.OnErrorListener] SAM used to notify the user about any underlying
     * [MediaRecorder] errors.
     */
    public fun setOnErrorListener(onErrorListener: OnErrorListener)

    /**
     * Sets an info listener.
     *
     * @param onInfoListener [StreamMediaRecorder.OnInfoListener] SAM used to notify the user about any underlying
     * [MediaRecorder] information and warnings.
     */
    public fun setOnInfoListener(onInfoListener: OnInfoListener)

    /**
     * Sets a [StreamMediaRecorder.OnRecordingStarted] listener on this instance of [StreamMediaRecorder].
     *
     * @param onRecordingStarted [StreamMediaRecorder.OnRecordingStarted] SAM used for notifying after the recording
     * has started successfully.
     */
    public fun setOnRecordingStartedListener(onRecordingStarted: OnRecordingStarted)

    /**
     * Sets a [StreamMediaRecorder.OnRecordingStopped] listener on this instance of [StreamMediaRecorder].
     *
     * @param onRecordingStopped [StreamMediaRecorder.OnRecordingStarted] SAM used to notify the user after the
     * recording has stopped.
     */
    public fun setOnRecordingStoppedListener(onRecordingStopped: OnRecordingStopped)

    /**
     * Sets a [StreamMediaRecorder.setOnMaxAmplitudeSampledListener] listener on this instance of [StreamMediaRecorder].
     *
     * @param onMaxAmplitudeSampled [StreamMediaRecorder.setOnMaxAmplitudeSampledListener] SAM used to notify when a new
     * maximum amplitude value has been sampled.
     */
    public fun setOnMaxAmplitudeSampledListener(onMaxAmplitudeSampled: OnMaxAmplitudeSampled)

    /**
     * Sets a [StreamMediaRecorder.OnMediaRecorderStateChange] listener on this instance of [StreamMediaRecorder].
     *
     * @param onMediaRecorderStateChange [StreamMediaRecorder.OnMediaRecorderStateChange] SAM used to notify when the
     * media recorder state has changed.
     */
    public fun setOnMediaRecorderStateChangedListener(onMediaRecorderStateChange: OnMediaRecorderStateChange)

    /**
     * Sets a [StreamMediaRecorder.OnCurrentRecordingDurationChanged] listener on this instance of
     * [StreamMediaRecorder].
     *
     * @param onCurrentRecordingDurationChanged [StreamMediaRecorder.OnCurrentRecordingDurationChanged] SAM updated
     * when the duration of the currently active recording has changed.
     */
    public fun setOnCurrentRecordingDurationChangedListener(
        onCurrentRecordingDurationChanged: OnCurrentRecordingDurationChanged,
    )

    /**
     * A functional interface used for listening to info events dispatched by the [MediaRecorder] internally
     * used by [StreamMediaRecorder].
     */
    public fun interface OnInfoListener {

        /**
         * Called when the [MediaRecorder] used internally by [StreamMediaRecorder] emits an info event.
         *
         * @param streamMediaRecorder The [StreamMediaRecorder] instance this event is tied to.
         * @param what Error or info type.
         * @param extra An extra code, specific to the error or info type.
         */
        public fun onInfo(
            streamMediaRecorder: StreamMediaRecorder,
            what: Int,
            extra: Int,
        )
    }

    /**
     * A functional interface used for listening to error events dispatched by the [MediaRecorder] internally
     * used by [StreamMediaRecorder].
     */
    public fun interface OnErrorListener {

        /**
         * Called when the [MediaRecorder] used internally by [StreamMediaRecorder] emits an error event.
         *
         * @param streamMediaRecorder The [StreamMediaRecorder] instance this event is tied to.
         * @param what Error or info type.
         * @param extra An extra code, specific to the error or info type.
         */
        public fun onError(
            streamMediaRecorder: StreamMediaRecorder,
            what: Int,
            extra: Int,
        )
    }

    /**
     * A functional interface used for notifying after the recording has started successfully.
     */
    public fun interface OnRecordingStarted {

        /**
         * Called after the recording has started successfully.
         */
        public fun onStarted()
    }

    /**
     * A functional interface used for notifying after the recording has stopped.
     */
    public fun interface OnRecordingStopped {

        /**
         * Called after the recording has stopped.
         */
        public fun onStopped()
    }

    /**
     * A functional interface used for emitting max amplitude readings during recording.
     */
    public fun interface OnMaxAmplitudeSampled {

        /**
         * Called after the recording has stopped.
         *
         * @param maxAmplitude The maximum amplitude value sampled since the previous sample.
         * @see [MediaRecorder.getMaxAmplitude]
         */
        public fun onSampled(maxAmplitude: Int)
    }

    /**
     * A functional interface used for listening to [StreamMediaRecorder] state changes.
     */
    public fun interface OnMediaRecorderStateChange {

        /**
         * Called after the recording has stopped.
         *
         * @param recorderState The current state of the media recorder
         */
        public fun onStateChanged(recorderState: MediaRecorderState)
    }

    /**
     * A functional interface used for listening to updated in the duration of the currently active recording.
     */
    public fun interface OnCurrentRecordingDurationChanged {

        /**
         * Called after the recording has stopped.
         *
         * @param durationMs The duration of the currently active recording expressed in milliseconds.
         */
        public fun onDurationChanged(durationMs: Long)
    }
}
