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
import android.os.Build
import io.getstream.chat.android.ui.common.utils.StreamFileUtil
import io.getstream.result.Error
import io.getstream.result.Result
import java.io.File

public class DefaultStreamMediaRecorder : StreamMediaRecorder {

    /**
     * An instance of [MediaRecorder] used primarily for recording audio.
     * This class is single instance per single use, so it will often get cycled and reset.  [MediaRecorder.release]
     * should be called after every use, as well as whenever the activity hosting the recording class gets paused.
     */
    // TODO this should be synchronized
    private var mediaRecorder: MediaRecorder? = null
        set(value) {
            if (value != null) {
                onErrorListener?.let { value.setOnErrorListener(it) }
                onInfoListener?.let { value.setOnInfoListener(it) }
            }
            field = value
        }

    /**
     * Used for listening to the error events emitted by [mediaRecorder].
     */
    private var onErrorListener: MediaRecorder.OnErrorListener? = null

    /**
     * Used for listening to the info events emitted by [mediaRecorder].
     */
    private var onInfoListener: MediaRecorder.OnInfoListener? = null

    /**
     * Initializes the media recorder and sets it to record audio using the device's microphone.
     *
     * @param context The [Context] necessary to prepare for recording.
     * @param recordingFile The [File] the audio will be saved to once the recording stops.
     */
    @Throws
    private fun initializeMediaRecorderForAudio(
        context: Context,
        recordingFile: File,
    ) {
        mediaRecorder = if (Build.VERSION.SDK_INT < 31) {
            MediaRecorder()
        } else {
            MediaRecorder(context)
        }.apply {
            val fileUri = StreamFileUtil.getUriForFile(
                context = context,
                file = recordingFile
            )
            setAudioSource(MediaRecorder.AudioSource.MIC)
            // TODO - consult with the SDK teams to see the best
            // TODO - format for this
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(fileUri.path)
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
    override fun startAudioRecording(
        context: Context,
        recordingName: String,
        override: Boolean,
    ): Result<File> {
        return try {
            StreamFileUtil.createFileInCacheDir(context, recordingName)
                .onSuccess {
                    initializeMediaRecorderForAudio(
                        context = context,
                        recordingFile = it
                    )

                    requireNotNull(mediaRecorder)

                    mediaRecorder?.start()

                    Result.Success(it)
                }
        } catch (exception: Exception) {
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not start audio recording.",
                    cause = exception
                )
            )
        }
    }

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
    override fun startAudioRecording(
        context: Context,
        recordingFile: File,
    ): Result<Unit> {
        return try {
            initializeMediaRecorderForAudio(
                context = context,
                recordingFile = recordingFile
            )

            requireNotNull(mediaRecorder)

            mediaRecorder?.start()

            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not start audio recording.",
                    cause = exception
                )
            )
        }
    }

    /**
     * Stops recording and saves the recording to the file provided by [startAudioRecording].
     *
     * @return A Unit wrapped inside a [Result] if recording has been stopped successfully. Returns a [ChatError]
     * wrapped inside [Result] if the action had failed.
     */
    override fun stopRecording(): Result<Unit> {
        return try {
            requireNotNull(mediaRecorder)

            mediaRecorder?.stop()
            mediaRecorder?.release()

            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not Stop audio recording.",
                    cause = exception
                )
            )
        }
    }

    /**
     * Deleted the recording to the file provided by [recordingFile].
     *
     * @param recordingFile The [File] to be deleted.
     *
     * @return A Unit wrapped inside a [Result] if recording has been deleted successfully. Returns a [ChatError]
     * wrapped inside [Result] if the action had failed.
     */
    override fun deleteRecording(recordingFile: File): Result<Unit> {
        return try {
            recordingFile.delete()

            Result.Success(Unit)
        } catch (exception: Exception) {
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not delete audio recording.",
                    cause = exception
                )
            )
        }
    }

    /**
     * Sets an error listener.
     *
     * @param onErrorListener [StreamMediaRecorder.OnErrorListener] SAM used to notify the user about any underlying
     * [MediaRecorder] errors.
     */
    override fun setOnErrorListener(onErrorListener: StreamMediaRecorder.OnErrorListener) {
        mediaRecorder?.setOnErrorListener { _, what, extra ->
            onErrorListener.onError(
                streamMediaRecorder = this,
                what = what,
                extra = extra
            )
        }
    }

    /**
     * A functional interface used for listening to info events dispatched by the [MediaRecorder] internally
     * used by [StreamMediaRecorder].
     */
    override fun setOnInfoListener(onInfoListener: StreamMediaRecorder.OnInfoListener) {
        mediaRecorder?.setOnInfoListener { _, what, extra ->
            onInfoListener.onInfo(
                this,
                what = what,
                extra = extra
            )
        }
    }
}
