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
import android.media.MediaRecorder.OnInfoListener
import java.io.File

/**
 * A media recording interface, designed to simplify using [MediaRecorder].
 */
public interface StreamMediaRecorder {

    /**
     * Creates a [File] internally and starts recording.
     * Calling the function again after a recording has already been started will reset the recording process.
     *
     * @param context The [Context] necessary to prepare for recording.
     * @param recordingName The file name the recording will be stored under.
     * @param override Determines if the new recording file should override one with the same name, if it exists.
     *
     * @return The [File] the audio will be saved to once the recording stops, if the file was created and recording
     * started successfully. Throws an exception otherwise.
     */
    @Throws
    public fun startRecording(
        context: Context,
        recordingName: String,
        override: Boolean = true,
    ): File

    /**
     * Prepares the given [recordingFile] and starts recording.
     * Calling the function again after a recording has already been started will reset the recording process.
     *
     * @param context The [Context] necessary to prepare for recording.
     * @param recordingFile The [File] the audio will be saved to once the recording stops.
     *
     * @return Unit if the recording was started successfully, throws an exception otherwise.
     */
    @Throws
    public fun startRecording(
        context: Context,
        recordingFile: File
    )

    /**
     * Stops recording and saves the recording to the file provided by [startRecording].
     *
     * @return Unit if the recording was stopped successfully, throws an exception otherwise.
     */
    @Throws
    public fun stopRecording()

    /**
     * Deleted the recording to the file provided by [recordingFile].
     *
     * @param recordingFile The [File] to be deleted.
     *
     * @return Unit if the recording was deleted successfully, throws an exception otherwise.
     */
    @Throws
    public fun deleteRecording(recordingFile: File)

    /**
     * Sets an error listener.
     *
     * @param onError [MediaRecorder.OnErrorListener] SAM used to notify the user about any underlying [MediaRecorder]
     * errors.
     */
    public fun setonErrorListener(onError: MediaRecorder.OnErrorListener)

    /**
     * Sets an info listener.
     *
     * @param onInfoListener [MediaRecorder.OnInfoListener] SAM used to notify the user about any underlying
     * [MediaRecorder] information and warnings.
     */
    public fun setOnInfoListener(onInfoListener: OnInfoListener)
}
