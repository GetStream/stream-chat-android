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

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import io.getstream.chat.android.client.extensions.EXTRA_DURATION
import io.getstream.chat.android.client.extensions.EXTRA_WAVEFORM_DATA
import io.getstream.chat.android.client.internal.file.StreamFileManager
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.log10

/**
 * The default implementation of [StreamMediaRecorder], used as a wrapper around [MediaRecorder] simplifying
 * working with it.
 *
 * @param context The [Context] necessary to prepare for recording.
 * @param fileManager The file manager for creating audio recording files .
 */
public class DefaultStreamMediaRecorder(
    private val context: Context,
    private val audioSource: Int = MediaRecorder.AudioSource.MIC,
    private val outputFormat: Int = MediaRecorder.OutputFormat.AAC_ADTS,
    private val audioEncoder: Int = MediaRecorder.AudioEncoder.AAC,
    private val audioSamplingRate: Int = SAMPLING_RATE_16KHZ,
    private val audioEncodingBitRate: Int = ENCODING_BIT_RATE_32KBPS,
    private val audioChannels: Int = CHANNELS,
    private val fileManager: StreamFileManager = StreamFileManager(),
) : StreamMediaRecorder {

    /**
     * Holds the current state of the [MediaRecorder] instance.
     */
    @VisibleForTesting
    internal var mediaRecorderState: MediaRecorderState = MediaRecorderState.UNINITIALIZED
        private set(value) {
            field = value
            onStreamMediaRecorderStateChangedListener?.onStateChanged(field)

            when (field) {
                MediaRecorderState.RECORDING -> {
                    activeRecordingStartedAt = System.currentTimeMillis()
                    logger.d { "[onMediaRecorderState] #1; activeRecordingStartedAt: $activeRecordingStartedAt" }
                    trackMaxDuration()
                }
                else -> {
                    activeRecordingStartedAt = 0L
                    logger.d { "[onMediaRecorderState] #2; activeRecordingStartedAt: $activeRecordingStartedAt" }
                }
            }
        }

    /**
     * Coroutine Scope used for performing various jobs.
     */
    private val coroutineScope: CoroutineScope = CoroutineScope(DispatcherProvider.IO)

    /**
     * The job used to poll for max amplitude.
     * @see pollMaxAmplitude
     */
    private var pollingJob: Job? = null

    /**
     * The job used to keep track of the current recording duration.
     * @see currentRecordingDurationJob
     */
    private var currentRecordingDurationJob: Job? = null

    /**
     * Used for logging errors, warnings and various information.
     */
    private val logger by taggedLogger("Chat:DefaultStreamMediaRecorder")

    /**
     * An instance of [MediaRecorder] used primarily for recording audio.
     * This class is single instance per single use, so it will often get cycled and reset.  [MediaRecorder.release]
     * should be called after every use, as well as whenever the activity hosting the recording class gets paused.
     */
    private var mediaRecorder: MediaRecorder? = null
        set(value) {
            if (value != null) {
                onErrorListener?.let { value.setOnErrorListener(it) }
                onInfoListener?.let { value.setOnInfoListener(it) }
            }
            field = value
        }

    /**
     * The file used to store the recording.
     */
    private var recordingFile: File? = null

    /**
     * The time at which the active recording has started.
     * Reset to null when a recording has been stopped.
     */
    private var activeRecordingStartedAt: Long? = null

    private var sampleData = arrayListOf<Float>()

    /**
     * Used for listening to the error events emitted by [mediaRecorder].
     */
    private var onErrorListener: MediaRecorder.OnErrorListener? = null

    /**
     * Used for listening to the info events emitted by [mediaRecorder].
     */
    private var onInfoListener: MediaRecorder.OnInfoListener? = null

    /**
     * Updated when the media recorder starts recording.
     */
    private var onStartRecordingListener: StreamMediaRecorder.OnRecordingStarted? = null

    /**
     * Updated when the media recorder stops recording.
     */
    private var onStopRecordingListener: StreamMediaRecorder.OnRecordingStopped? = null

    /**
     * Updated when a new max amplitude value is emitted.
     */
    private var onMaxAmplitudeSampledListener: StreamMediaRecorder.OnMaxAmplitudeSampled? = null

    /**
     * Updated when the media recorder state changes.
     */
    private var onStreamMediaRecorderStateChangedListener: StreamMediaRecorder.OnMediaRecorderStateChange? = null

    /**
     * Updated when the duration of the currently active recording changes.
     */
    private var onCurrentRecordingDurationChangedListener: StreamMediaRecorder.OnCurrentRecordingDurationChanged? = null

    @VisibleForTesting
    internal var buildMediaRecorder: () -> MediaRecorder = {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            MediaRecorder()
        } else {
            MediaRecorder(context)
        }
    }

    /**
     * Initializes the media recorder and sets it to record audio using the device's microphone.
     *
     * @param recordingFile The [File] the audio will be saved to once the recording stops.
     */
    @Throws
    private fun initializeMediaRecorderForAudio(recordingFile: File) {
        release()

        mediaRecorder = buildMediaRecorder().apply {
            setAudioSource(audioSource)
            setOutputFormat(outputFormat)
            setAudioEncoder(audioEncoder)
            setAudioEncodingBitRate(audioEncodingBitRate)
            setAudioSamplingRate(audioSamplingRate)
            setAudioChannels(audioChannels)
            setOutputFile(recordingFile.path)
            prepare()
            mediaRecorderState = MediaRecorderState.PREPARED
        }
    }

    /**
     * Creates a [File] internally and starts recording.
     * Calling the function again after a recording has already been started will reset the recording process.
     *
     * @param recordingName The file name the recording will be stored under.
     * @param amplitudePollingInterval Dictates how often the recorder is polled for the latest max amplitude and
     * how often [onMaxAmplitudeSampledListener] emits a new value.
     * @param override Determines if the new recording file should override one with the same name, if it exists.
     *
     * @return The [File] to which the recording will be stored wrapped inside a [Result] if recording has
     * started successfully. Returns a [Error] wrapped inside a [Result] if the action had failed.
     */
    override fun startAudioRecording(
        recordingName: String,
        amplitudePollingInterval: Long,
        override: Boolean,
    ): Result<File> {
        return try {
            fileManager.createFileInCache(context, recordingName)
                .onSuccess {
                    recordingFile = it
                    initializeMediaRecorderForAudio(recordingFile = it)
                    requireNotNull(mediaRecorder)
                    mediaRecorder?.start()
                    onStartRecordingListener?.onStarted()

                    mediaRecorderState = MediaRecorderState.RECORDING
                    pollMaxAmplitude(amplitudePollingInterval)

                    Result.Success(it)
                }
        } catch (exception: Exception) {
            release()
            logger.e(exception) { "Could not start recording audio" }
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not start audio recording.",
                    cause = exception,
                ),
            )
        }
    }

    /**
     * Prepares the given [recordingFile] and starts recording.
     * Calling the function again after a recording has already been started will reset the recording process.
     *
     * @param recordingFile The [File] the audio will be saved to once the recording stops.
     * @param amplitudePollingInterval Dictates how often the recorder is polled for the latest max amplitude and
     * how often [onMaxAmplitudeSampledListener] emits a new value.
     *
     * @return A Unit wrapped inside a [Result] if recording has started successfully. Returns a [ChatError] wrapped
     * inside [Result] if the action had failed.
     */
    override fun startAudioRecording(
        recordingFile: File,
        amplitudePollingInterval: Long,
    ): Result<Unit> {
        return try {
            this.recordingFile = recordingFile

            initializeMediaRecorderForAudio(recordingFile = recordingFile)

            requireNotNull(mediaRecorder)

            mediaRecorder?.start()
            onStartRecordingListener?.onStarted()
            mediaRecorderState = MediaRecorderState.RECORDING
            pollMaxAmplitude(amplitudePollingInterval)
            Result.Success(Unit)
        } catch (exception: Exception) {
            release()
            logger.e(exception) { "Could not start recording audio" }
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not start audio recording.",
                    cause = exception,
                ),
            )
        }
    }

    /**
     * Stops recording and saves the recording to the file provided by [startAudioRecording].
     *
     * @return A Unit wrapped inside a [Result] if recording has been stopped successfully. Returns a [ChatError]
     * wrapped inside [Result] if the action had failed.
     */
    override fun stopRecording(): Result<RecordedMedia> {
        return try {
            requireNotNull(mediaRecorder)
            mediaRecorder?.stop()

            val calculatedDurationInMs = activeRecordingStartedAt?.let {
                System.currentTimeMillis() - it
            } ?: 0
            val parsedDurationInMs = getAudioDurationInMs(recordingFile)
            logger.d {
                "[stopRecording] startedAt: $activeRecordingStartedAt, " +
                    "calculatedDuration: $calculatedDurationInMs, parsedDuration: $parsedDurationInMs"
            }

            val durationInMs = when (parsedDurationInMs > 0) {
                true -> parsedDurationInMs
                else -> calculatedDurationInMs.toInt()
            }
            onCurrentRecordingDurationChangedListener?.onDurationChanged(durationInMs.toLong())
            release()
            onStopRecordingListener?.onStopped()

            val attachment = Attachment(
                title = recordingFile?.name ?: "Recording",
                upload = recordingFile,
                type = AttachmentType.AUDIO_RECORDING,
                mimeType = "audio/aac",
                extraData = mapOf(
                    EXTRA_DURATION to durationInMs / 1000f,
                    EXTRA_WAVEFORM_DATA to sampleData,
                ),
            )
            val recordedMedia = RecordedMedia(attachment = attachment, durationInMs = durationInMs)
            logger.v { "[stopRecording] succeed: $recordedMedia" }
            Result.Success(recordedMedia)
        } catch (exception: Exception) {
            logger.e(exception) { "[stopRecording] failed: $exception" }
            release()
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not Stop audio recording.",
                    cause = exception,
                ),
            )
        }
    }

    private fun getAudioDurationInMs(file: File?): Int {
        file ?: return 0
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.toUri().toString())
            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationString?.toInt()
            duration ?: 0
        } catch (e: Throwable) {
            logger.e(e) { "[getAudioDurationInMs] failed: $e" }
            0
        } finally {
            try {
                retriever.release()
            } catch (_: Throwable) {
            }
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
            logger.e(exception) { "Could not delete audio recording" }
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not delete audio recording.",
                    cause = exception,
                ),
            )
        }
    }

    /**
     * Releases the [MediaRecorder] used by [StreamMediaRecorder].
     */
    override fun release() {
        mediaRecorder?.release()
        mediaRecorderState = MediaRecorderState.UNINITIALIZED
        onStopRecordingListener?.onStopped()
    }

    /**
     * Polls the latest maximum amplitude value and updates [onMaxAmplitudeSampledListener] listener with the new value.
     */
    private fun pollMaxAmplitude(amplitudePollingInterval: Long) {
        sampleData.clear()
        pollingJob?.cancel()
        pollingJob = coroutineScope.launch {
            try {
                while (mediaRecorderState == MediaRecorderState.RECORDING) {
                    val maxAmplitude = mediaRecorder?.maxAmplitude

                    if (maxAmplitude != null) {
                        val db = 20 * log10(maxAmplitude.toDouble())
                        val normalized = maxAmplitude / Short.MAX_VALUE.toFloat()
                        logger.d { "[pollMaxAmplitude] maxAmplitude: $maxAmplitude, db: $db, normalized: $normalized" }
                        sampleData.add(normalized)
                        onMaxAmplitudeSampledListener?.onSampled(maxAmplitude)
                    }
                    delay(amplitudePollingInterval)
                }
            } catch (e: Exception) {
                logger.e {
                    "Could not start poll max amplitude: ${e.message ?: e.cause}"
                }
            }
        }
    }

    /**
     * Keeps track of the duration of the currently active recording and updates
     * [onCurrentRecordingDurationChangedListener] accordingly.
     */
    private fun trackMaxDuration() {
        currentRecordingDurationJob?.cancel()

        currentRecordingDurationJob = coroutineScope.launch {
            while (mediaRecorderState == MediaRecorderState.RECORDING) {
                val activeRecordingStartedAt = this@DefaultStreamMediaRecorder.activeRecordingStartedAt
                val currentDuration =
                    if (activeRecordingStartedAt != null) {
                        System.currentTimeMillis() - activeRecordingStartedAt
                    } else {
                        0L
                    }

                onCurrentRecordingDurationChangedListener?.onDurationChanged(currentDuration)

                delay(1000)
            }
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
                extra = extra,
            )
        }
    }

    /**
     * Sets an info listener.
     *
     * @param onInfoListener [StreamMediaRecorder.OnInfoListener] SAM used to notify the user about any underlying
     * [MediaRecorder] information events.
     */
    override fun setOnInfoListener(onInfoListener: StreamMediaRecorder.OnInfoListener) {
        mediaRecorder?.setOnInfoListener { _, what, extra ->
            onInfoListener.onInfo(
                this,
                what = what,
                extra = extra,
            )
        }
    }

    /**
     * Sets an [StreamMediaRecorder.OnRecordingStarted] listener on this instance of [StreamMediaRecorder].
     *
     * @param onRecordingStarted [StreamMediaRecorder.OnRecordingStarted] SAM used for notifying after the recording
     * has started successfully.
     */
    // TODO evaluate for removal due to new state updater
    override fun setOnRecordingStartedListener(onRecordingStarted: StreamMediaRecorder.OnRecordingStarted) {
        this.onStartRecordingListener = onRecordingStarted
    }

    /**
     * Sets an [StreamMediaRecorder.OnRecordingStopped] listener on this instance of [StreamMediaRecorder].
     *
     * @param onRecordingStopped [StreamMediaRecorder.OnRecordingStarted] SAM used to notify the user after the
     * recording has stopped.
     */
    // TODO evaluate for removal due to new state updater
    override fun setOnRecordingStoppedListener(onRecordingStopped: StreamMediaRecorder.OnRecordingStopped) {
        this.onStopRecordingListener = onRecordingStopped
    }

    /**
     * Sets a [StreamMediaRecorder.setOnMaxAmplitudeSampledListener] listener on this instance of [StreamMediaRecorder].
     *
     * @param onMaxAmplitudeSampled [StreamMediaRecorder.setOnMaxAmplitudeSampledListener] SAM used to notify when a new
     * maximum amplitude value has been sampled.
     */
    override fun setOnMaxAmplitudeSampledListener(onMaxAmplitudeSampled: StreamMediaRecorder.OnMaxAmplitudeSampled) {
        this.onMaxAmplitudeSampledListener = onMaxAmplitudeSampled
    }

    /**
     * Sets a [StreamMediaRecorder.OnMediaRecorderStateChange] listener on this instance of [StreamMediaRecorder].
     *
     * @param onMediaRecorderStateChange [StreamMediaRecorder.OnMediaRecorderStateChange] SAM used to notify when the
     * media recorder state has changed.
     */
    override fun setOnMediaRecorderStateChangedListener(
        onMediaRecorderStateChange: StreamMediaRecorder.OnMediaRecorderStateChange,
    ) {
        this.onStreamMediaRecorderStateChangedListener = onMediaRecorderStateChange
    }

    /**
     * Sets a [StreamMediaRecorder.OnCurrentRecordingDurationChanged] listener on this instance of
     * [StreamMediaRecorder].
     *
     * @param onCurrentRecordingDurationChanged [StreamMediaRecorder.OnCurrentRecordingDurationChanged] SAM updated
     * when the duration of the currently active recording has changed.
     */
    override fun setOnCurrentRecordingDurationChangedListener(
        onCurrentRecordingDurationChanged: StreamMediaRecorder.OnCurrentRecordingDurationChanged,
    ) {
        this.onCurrentRecordingDurationChangedListener = onCurrentRecordingDurationChanged
    }

    private companion object {
        // 1 channel - optimal for voice recording
        private const val CHANNELS = 1

        // 16 kHz - standard for voice recording, provides good clarity while keeping file size small
        private const val SAMPLING_RATE_16KHZ = 16000

        // 32 kbps - optimal for voice recording
        private const val ENCODING_BIT_RATE_32KBPS = 32000

        // 44.1 kHz - standard for music recording, provides good clarity while keeping file size small
        private const val SAMPLING_RATE_44100HZ = 44100

        // 128 kbps - standard bitrate for music recording at 44.1 kHz
        private const val ENCODING_BIT_RATE_128KBPS = 128000
    }
}
