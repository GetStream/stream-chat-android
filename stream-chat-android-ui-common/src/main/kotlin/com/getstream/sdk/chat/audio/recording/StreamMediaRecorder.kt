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

package com.getstream.sdk.chat.audio.recording

import android.content.Context
import android.media.MediaRecorder
import io.getstream.result.Result
import java.io.File

/**
 * A media recording interface, designed to simplify using [MediaRecorder].
 */
// TODO update kdocs with result class returns
public interface StreamMediaRecorder {

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
    @Throws
    public fun startAudioRecording(
        context: Context,
        recordingName: String,
        override: Boolean = true,
    ): Result<File>

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
    @Throws
    public fun startAudioRecording(
        context: Context,
        recordingFile: File,
    ): Result<Unit>

    /**
     * Stops recording and saves the recording to the file provided by [startAudioRecording].
     *
     * @return A Unit wrapped inside a [Result] if recording has been stopped successfully. Returns a [ChatError]
     * wrapped inside [Result] if the action had failed.
     */
    @Throws
    public fun stopRecording(): Result<Unit>

    /**
     * Deleted the recording to the file provided by [recordingFile].
     *
     * @param recordingFile The [File] to be deleted.
     *
     * @return A Unit wrapped inside a [Result] if recording has been deleted successfully. Returns a [ChatError]
     * wrapped inside [Result] if the action had failed.
     */
    @Throws
    public fun deleteRecording(recordingFile: File): Result<Unit>

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
}
