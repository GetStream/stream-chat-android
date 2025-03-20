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

package io.getstream.chat.android.ui.common.feature.messages.composer

import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.audio.ProgressData
import io.getstream.chat.android.client.audio.audioHash
import io.getstream.chat.android.client.extensions.EXTRA_WAVEFORM_DATA
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.state.messages.composer.copy
import io.getstream.log.StreamLog
import io.getstream.log.TaggedLogger
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Controller responsible for recording audio messages.
 *
 * @param channelId The ID of the channel we're chatting in.
 * @param audioPlayer The audio player used to play audio messages.
 * @param mediaRecorder The media recorder used to record audio messages.
 * @param fileToUri Coverts [File] into Uri like string.
 * @param scope Coverts [File] into Uri like string.
 * @param scope MessageComposerController's scope.
 */
internal class AudioRecordingController(
    private val channelId: String,
    private val audioPlayer: AudioPlayer,
    private val mediaRecorder: StreamMediaRecorder,
    private val fileToUri: (File) -> String,
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

    private val drawPollingInterval = 100 // 100ms
    private val realPollingInterval = 10 // 10ms

    private val samples = arrayListOf<Int>()
    private val samplesTarget = 100 // 100 samples represent entire recording
    private val samplesLimit = samplesTarget * realPollingInterval // max samples representing entire recording
    private val samplesBuffer = arrayListOf<Int>()
    private var samplesBufferLimit = 1

    private val waveform = arrayListOf<Float>()
    private val waveformBuffer = IntArray(drawPollingInterval / realPollingInterval)
    private var waveformBufferCount = 0

    init {
        if (drawPollingInterval < realPollingInterval) {
            throw IllegalArgumentException("drawPollingInterval must be greater than realPollingInterval")
        }
        setupMediaRecorder()
    }

    private fun setupMediaRecorder() {
        mediaRecorder.setOnRecordingStartedListener {
            logger.i { "[onRecorderStarted] no args" }
        }
        mediaRecorder.setOnRecordingStoppedListener {
            logger.i { "[onRecorderStopped] recordingState: ${recordingState.value}" }
        }
        mediaRecorder.setOnMediaRecorderStateChangedListener { state ->
            logger.i { "[onRecorderStateChanged] state: $state; recordingState: ${recordingState.value}" }
        }
        mediaRecorder.setOnErrorListener { _, what, extra ->
            logger.e { "[onRecorderError] what: $what, extra: $extra" }
        }
        mediaRecorder.setOnInfoListener { _, what, extra ->
            logger.i { "[onRecorderInfo] what: $what, extra: $extra" }
        }
        mediaRecorder.setOnCurrentRecordingDurationChangedListener { durationMs ->
            scope.launch(DispatcherProvider.Main) {
                val state = recordingState.value
                logger.v { "[onRecorderDurationChanged] duration: $durationMs, state: $state" }
                if (state is RecordingState.Recording) {
                    // TODO make duration Int
                    setState(state.copy(duration = durationMs.toInt()))
                }
            }
        }
        mediaRecorder.setOnMaxAmplitudeSampledListener { maxAmplitude ->
            scope.launch(DispatcherProvider.Main) {
                saveSamples(maxAmplitude)
                processWave(maxAmplitude)
            }
        }
    }

    // called each 10ms
    private fun saveSamples(maxAmplitude: Int) {
        samplesBuffer.add(maxAmplitude)
        if (samplesBuffer.size < samplesBufferLimit) {
            return
        }
        val amplitude = samplesBuffer.max()
        samplesBuffer.clear()
        samples.add(amplitude)
        if (samples.size > samplesLimit) {
            val newSamples = samples.downsampleMax(samplesTarget)
            samples.clear()
            samples.addAll(newSamples)
            samplesBufferLimit *= samplesLimit / samplesTarget
            logger.v { "[saveSamples] reached samples limit; samplesBufferLimit: $samplesBufferLimit" }
        }
    }

    private fun processWave(maxAmplitude: Int) {
        waveformBuffer[waveformBufferCount++] = maxAmplitude
        if (waveformBufferCount < waveformBuffer.size) {
            // logger.v { "[processWave] skip ($waveformBufferCount): $maxAmplitude" }
            return
        }
        val amplitude = waveformBuffer.downsampleMax()
        val normalized = normalize(amplitude)
        // logger.w { "[processWave] amplitudeOf($waveformBufferCount): $amplitude ($normalized)" }
        waveformBufferCount = 0

        waveform.add(normalized)
        val state = recordingState.value
        if (state is RecordingState.Recording) {
            logger.v { "[processWave] waveform.size($normalized): ${waveform.size}" }
            setState(state.copy(waveform = ArrayList(waveform)))
        }
    }

    public fun startRecording(offset: Pair<Float, Float>? = null) {
        val state = this.recordingState.value
        if (state !is RecordingState.Idle) {
            logger.w { "[startRecording] rejected (state is not Idle): $state" }
            return
        }
        logger.i { "[startRecording] state: $state" }
        val recordingName = "audio_recording_${Date()}"
        mediaRecorder.startAudioRecording(recordingName, realPollingInterval.toLong())
        setState(RecordingState.Hold(offset = offset ?: RecordingState.Hold.ZeroOffset))
    }

    public fun holdRecording(offset: Pair<Float, Float>? = null) {
        val state = this.recordingState.value
        if (state !is RecordingState.Hold) {
            logger.w { "[holdRecording] rejected (state is not Hold): $state" }
            return
        }
        if (offset == null) {
            logger.v { "[holdRecording] rejected (offset is null)" }
            return
        }
        logger.v { "[holdRecording] offset: Offset(${offset.first}:${offset.second})" }
        setState(state.copy(offset = offset))
    }

    public fun lockRecording() {
        val state = this.recordingState.value
        if (state !is RecordingState.Hold) {
            logger.w { "[lockRecording] rejected (state is not Hold): $state" }
            return
        }
        logger.i { "[lockRecording] state: $state" }
        setState(RecordingState.Locked(state.durationInMs, state.waveform))
    }

    public fun cancelRecording() {
        val state = this.recordingState.value
        if (state is RecordingState.Idle) {
            logger.w { "[cancelRecording] rejected (state is not Idle)" }
            return
        }
        logger.i { "[cancelRecording] state: $state" }
        mediaRecorder.release()
        if (state is RecordingState.Overview && state.isPlaying) {
            audioPlayer.resetAudio(state.playingId)
        }
        clearData()
        setState(RecordingState.Idle)
    }

    public fun toggleRecordingPlayback() {
        val state = this.recordingState.value
        if (state !is RecordingState.Overview) {
            logger.v { "[toggleRecordingPlayback] rejected (state is not Locked): $state" }
            return
        }
        logger.i { "[toggleRecordingPlayback] state: $state, playerState: ${audioPlayer.currentState}" }
        val audioFile = state.attachment.upload ?: run {
            logger.v { "[toggleRecordingPlayback] rejected (audioFile is null)" }
            return
        }
        if (state.hasPlayingId && state.playingId == audioPlayer.currentPlayingId) {
            if (state.isPlaying) {
                logger.d { "[toggleRecordingPlayback] pause playback" }
                audioPlayer.pause()
                setState(state.copy(isPlaying = false))
            } else {
                logger.d { "[toggleRecordingPlayback] resume playback" }
                audioPlayer.resume(state.playingId)
                setState(state.copy(isPlaying = true))
            }
            return
        }
        val audioHash = state.attachment.audioHash
        logger.d { "[toggleRecordingPlayback] start playback: $audioHash" }
        audioPlayer.registerOnProgressStateChange(audioHash, ::onAudioPlayingProgress)
        audioPlayer.registerOnAudioStateChange(audioHash, ::onAudioStateChanged)
        audioPlayer.play(fileToUri(audioFile), audioHash)
        setState(
            state.copy(
                isPlaying = true,
                playingId = audioHash,
            ),
        )
    }

    private fun onAudioStateChanged(playbackState: AudioState) {
        val state = this.recordingState.value
        if (state !is RecordingState.Overview) {
            logger.d { "[onAudioStateChanged] rejected (state is not Overview): $state" }
            return
        }
        logger.d { "[onAudioStateChanged] playbackState: $playbackState" }
        setState(
            state.copy(
                isPlaying = playbackState == AudioState.PLAYING,
                playingProgress = when (playbackState) {
                    AudioState.PLAYING,
                    AudioState.PAUSE,
                    -> state.playingProgress
                    else -> 0f
                },
            ),
        )
    }

    private fun onAudioPlayingProgress(progressState: ProgressData) {
        val curState = this.recordingState.value
        if (curState is RecordingState.Overview) {
            setState(
                curState.copy(
                    isPlaying = true,
                    playingProgress = progressState.progress,
                    durationInMs = progressState.duration.takeIf { it > 0 } ?: curState.durationInMs,
                ),
            )
        }
    }

    public fun stopRecording() {
        val state = this.recordingState.value
        if (state !is RecordingState.Locked) {
            logger.w { "[stopRecording] rejected (state is not Locked): $state" }
            return
        }
        logger.i { "[stopRecording] no args: $state" }
        val result = mediaRecorder.stopRecording()
        if (result.isFailure) {
            logger.e { "[stopRecording] failed: ${result.errorOrNull()}" }
            clearData()
            setState(RecordingState.Idle)
            return
        }
        val adjusted = samples.downsampleMax(samplesTarget)
        val normalized = adjusted.normalize()
        clearData()
        val recorded = result.getOrThrow()
        logger.v { "[stopRecording] recorded: $recorded" }
        setState(RecordingState.Overview(recorded.durationInMs, normalized, recorded.attachment))
    }

    public fun seekRecordingTo(progress: Float) {
        val state = this.recordingState.value
        if (state !is RecordingState.Overview) {
            logger.w { "[seekRecordingTo] rejected (state is not Overview)" }
            return
        }
        state.attachment.upload ?: run {
            logger.w { "[seekRecordingTo] rejected (audioFile is null)" }
            return
        }
        val positionInMs = (progress * state.durationInMs).toInt()
        logger.i { "[seekRecordingTo] progress: $progress (${positionInMs}ms), state: $state" }
        val audioHash = state.attachment.audioHash
        audioPlayer.seekTo(positionInMs, audioHash)
        setState(state.copy(playingProgress = progress))
    }

    public fun pauseRecording() {
        val state = this.recordingState.value
        if (state !is RecordingState.Overview) {
            logger.w { "[pauseRecording] rejected (state is not Overview)" }
            return
        }
        logger.i { "[pauseRecording] state: $state" }
        audioPlayer.startSeek(state.playingId)
        setState(state.copy(isPlaying = false))
    }

    public fun completeRecording() {
        val state = this.recordingState.value
        logger.w { "[completeRecording] state: $state" }
        if (state is RecordingState.Idle) {
            logger.w { "[completeRecording] rejected (state is Idle)" }
            return
        }
        if (state is RecordingState.Overview) {
            logger.d { "[completeRecording] completing from Overview state" }
            audioPlayer.resetAudio(state.playingId)
            clearData()
            setState(
                RecordingState.Complete(
                    state.attachment.copy(
                        extraData = state.attachment.extraData + mapOf(
                            EXTRA_WAVEFORM_DATA to state.waveform,
                        ),
                    ),
                ),
            )
            setState(RecordingState.Idle)
            return
        }
        val result = mediaRecorder.stopRecording()
        if (result.isFailure) {
            logger.e { "[completeRecording] failed: ${result.errorOrNull()}" }
            clearData()
            setState(RecordingState.Idle)
            return
        }
        val adjusted = samples.downsampleMax(samplesTarget)
        val normalized = adjusted.normalize()
        clearData()
        val recorded = result.getOrThrow().let {
            it.copy(
                attachment = it.attachment.copy(
                    extraData = it.attachment.extraData + mapOf(
                        EXTRA_WAVEFORM_DATA to normalized,
                    ),
                ),
            )
        }
        logger.d { "[completeRecording] complete from state: $state" }
        setState(RecordingState.Complete(recorded.attachment))
        setState(RecordingState.Idle)
    }

    public fun onCleared() {
        logger.i { "[onCleared] no args" }
        mediaRecorder.release()
        val state = this.recordingState.value
        if (state is RecordingState.Overview) {
            audioPlayer.resetAudio(state.playingId)
        }
        clearData()
    }

    private fun clearData() {
        logger.v { "[clearData] no args" }
        waveform.clear()
        waveformBuffer.fill(0)
        waveformBufferCount = 0
        samples.clear()
        samplesBuffer.clear()
        samplesBufferLimit = 1
        setState(RecordingState.Idle)
    }

    private fun setState(state: RecordingState) {
        recordingState.value = state
    }

    private fun List<Int>.normalize(): List<Float> {
        return map { amplitude ->
            normalize(amplitude)
        }
    }

    private fun normalize(maxAmplitude: Int): Float {
        // val normalized = maxOf(maxAmplitude.toFloat() / 32767f, 0.2f)
        val normalized = maxAmplitude.toFloat() / Short.MAX_VALUE

        val MAX_AMPLITUDE = 32767f
        val MAX_DB = 90.0 // Maximum decibel level for normalization

        val decibels = 20 * log10(maxAmplitude / MAX_AMPLITUDE)
        // val normalizedValue = maxOf(0.0, minOf(decibels / MAX_DB, 1.0))
        val normalizedValue = abs((50 + decibels) / 50)
        // logger.v { "[onRecorderMaxAmplitudeSampled] maxAmplitude: $maxAmplitude, decibels: $decibels, " +
        //     "normalizedValue: $normalizedValue ($normalized)" }
        if (maxAmplitude > 20_000) {
            logger.w { "[normalize] normalizedValue: $normalizedValue, maxAmplitude: $maxAmplitude" }
        }
        if (normalizedValue == Float.NEGATIVE_INFINITY || normalizedValue == Float.POSITIVE_INFINITY) {
            return 0f
        }
        return normalizedValue
    }

    private fun List<Float>.downsample(targetSamples: Int): List<Float> {
        val sourceSamples = size
        val sourceStep = sourceSamples / targetSamples
        val target = ArrayList<Float>(targetSamples)
        for (targetIndex in 0 until targetSamples) {
            var sum = 0f
            for (sourceIndex in 0 until sourceStep) {
                val sourceSample = this[targetIndex * sourceStep + sourceIndex]
                sum += sourceSample.pow(2)
            }
            target.add(sqrt(sum / sourceStep))
        }
        return target
    }

    private fun List<Int>.downsampleMax(targetSamples: Int): List<Int> {
        val sourceSamples = size
        val sourceStep = sourceSamples / targetSamples
        val target = ArrayList<Int>(targetSamples)
        for (targetIndex in 0 until targetSamples) {
            var max = 0
            for (sourceIndex in 0 until sourceStep) {
                val sourceSample = this[targetIndex * sourceStep + sourceIndex]
                max = maxOf(max, sourceSample)
            }
            target.add(max)
        }
        return target
    }

    private fun FloatArray.downsampleRms(): Float {
        var sumOfSquaredSamples = 0f
        for (sample in this) {
            sumOfSquaredSamples += sample.pow(n = 2)
        }
        return sqrt(sumOfSquaredSamples / size)
    }

    private fun FloatArray.downsampleAverage(): Float {
        return sum() / size
    }

    private fun FloatArray.downsample(): Float {
        return last()
    }

    private fun IntArray.downsampleRms(): Int {
        var sumOfSquaredSamples = 0
        for (sample in this) {
            sumOfSquaredSamples += sample * sample
        }
        if (sumOfSquaredSamples == 0) {
            return 0
        }
        return sqrt(sumOfSquaredSamples / size.toDouble()).roundToInt()
    }

    private fun IntArray.downsampleMax(): Int {
        return max()
    }

    private fun List<Int>.downsampleToSingleMax(): Int {
        1 to 1
        return max()
    }
}
