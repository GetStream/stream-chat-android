package io.getstream.chat.android.ui.common.feature.messages.composer

import com.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.state.messages.composer.copy
import io.getstream.log.StreamLog
import io.getstream.log.TaggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import kotlin.math.abs
import kotlin.math.log10

public class AudioRecordingController(
    private val channelId: String,
    private val chatClient: ChatClient,
    private val mediaRecorder: StreamMediaRecorder,
    private val scope: CoroutineScope,
) {

    /**
     * The logger used to print to errors, warnings, information
     * and other things to log.
     */
    private val logger: TaggedLogger = StreamLog.getLogger("Chat:RecordController")

    /**
     * Represents the current recording state.
     */
    public val recordingState: MutableStateFlow<RecordingState> = MutableStateFlow(RecordingState.Idle)

    init {
        setupMediaRecorder()
    }

    private val mutex = Mutex()

    private val waveform = arrayListOf<Float>()

    private fun setupMediaRecorder() {
        mediaRecorder.setOnRecordingStartedListener {
            logger.i { "[onRecorderStarted] no args" }
        }
        mediaRecorder.setOnRecordingStoppedListener {
            logger.i { "[onRecorderStopped] no args" }
        }
        mediaRecorder.setOnMediaRecorderStateChangedListener { state ->
            logger.i { "[onRecorderStateChanged] state: $state" }
        }
        mediaRecorder.setOnErrorListener { recorder, what, extra ->
            logger.e { "[onRecorderError] what: $what, extra: $extra" }
        }
        mediaRecorder.setOnInfoListener { recorder, what, extra ->
            logger.i { "[onRecorderInfo] what: $what, extra: $extra" }
        }
        mediaRecorder.setOnCurrentRecordingDurationChangedListener { durationMs ->
            logger.v { "[onRecorderDurationChanged] duration: $durationMs" }
            scope.launch {
                mutex.withLock {
                    val state = recordingState.value
                    if (state is RecordingState.Recording) {
                        recordingState.value = state.copy(duration = durationMs)
                    }
                }
            }
        }
        mediaRecorder.setOnMaxAmplitudeSampledListener { maxAmplitude ->

            //val normalized = maxOf(maxAmplitude.toFloat() / 32767f, 0.2f)
            val normalized = maxAmplitude.toFloat() / Short.MAX_VALUE

            val MAX_AMPLITUDE = 32767.0
            val MAX_DB = 90.0 // Maximum decibel level for normalization

            val decibels = 20 * log10(maxAmplitude / MAX_AMPLITUDE)
            //val normalizedValue = maxOf(0.0, minOf(decibels / MAX_DB, 1.0))
            val normalizedValue = abs((50 + decibels) / 50)
            // logger.v { "[onRecorderMaxAmplitudeSampled] maxAmplitude: $maxAmplitude, decibels: $decibels, " +
            //     "normalizedValue: $normalizedValue ($normalized)" }

            scope.launch {
                mutex.withLock {
                    waveform.add(normalizedValue.toFloat())
                    val state = recordingState.value
                    if (state is RecordingState.Recording) {
                        logger.v { "[onRecorderMaxAmplitudeSampled] waveform: ${waveform.size}" }
                        recordingState.value = state.copy(waveform = ArrayList(waveform))
                    }
                }
            }
        }

    }

    public fun startRecording() {
        val state = this.recordingState.value
        if (state !is RecordingState.Idle) {
            logger.w { "[startRecording] rejected (state is not Idle): $state" }
            return
        }
        logger.i { "[startRecording] state: $state" }
        mediaRecorder.startAudioRecording(recordingName = "audio_recording_${Date()}")
        this.recordingState.value = RecordingState.Hold()
    }

    public fun lockRecording() {
        val state = this.recordingState.value
        if (state !is RecordingState.Hold) {
            logger.w { "[lockRecording] rejected (state is not Hold): $state" }
            return
        }
        logger.i { "[lockRecording] state: $state" }
        this.recordingState.value = RecordingState.Locked(state.duration, state.waveform)
    }

    public fun cancelRecording() {
        val state = this.recordingState.value
        if (state is RecordingState.Idle) {
            logger.w { "[cancelRecording] rejected (state is not Idle)" }
            return
        }
        logger.i { "[cancelRecording] state: $state" }
        mediaRecorder.release()
        this.recordingState.value = RecordingState.Idle
    }

    public fun deleteRecording() {
        val state = this.recordingState.value
        if (state is RecordingState.Idle) {
            logger.w { "[deleteRecording] rejected (state is not Idle)" }
            return
        }
        logger.i { "[deleteRecording] state: $state" }
        mediaRecorder.release()
        this.recordingState.value = RecordingState.Idle
    }

    public fun toggleRecording() {
        // TODO
    }

    public fun stopRecording() {
        val state = this.recordingState.value
        if (state !is RecordingState.Locked) {
            logger.w { "[stopRecording] rejected (state is not Locked): $state" }
            return
        }
        logger.i { "[stopRecording] no args: $state" }
        mediaRecorder.stopRecording()
        this.recordingState.value = RecordingState.Overview(state.duration, state.waveform)
    }

    public fun completeRecording() {
        val state = this.recordingState.value
        if (state is RecordingState.Idle) {
            logger.w { "[completeRecording] rejected (state is not Idle)" }
            return
        }
        logger.i { "[completeRecording] state: $state" }
        this.recordingState.value = RecordingState.Idle
    }

    public fun onCleared() {
        mediaRecorder.release()
    }

}