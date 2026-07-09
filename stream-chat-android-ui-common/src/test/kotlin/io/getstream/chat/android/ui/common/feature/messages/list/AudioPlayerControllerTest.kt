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

package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.client.audio.AudioPlayer
import io.getstream.chat.android.client.audio.AudioState
import io.getstream.chat.android.client.audio.ProgressData
import io.getstream.chat.android.client.audio.audioHash
import io.getstream.chat.android.client.extensions.EXTRA_DURATION
import io.getstream.chat.android.client.extensions.EXTRA_WAVEFORM_DATA
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.randomAttachment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AudioPlayerControllerTest {

    private lateinit var audioPlayer: AudioPlayer
    private lateinit var controller: AudioPlayerController

    @BeforeEach
    fun setUp() {
        audioPlayer = mock {
            on { currentState } doReturn AudioState.IDLE
            on { currentPlayingId } doReturn NO_ID
        }
        controller = AudioPlayerController(audioPlayer) { RECORDING_URI }
    }

    @Test
    fun `play rejects attachment which is not an audio recording`() {
        controller.play(randomAttachment(type = AttachmentType.IMAGE))

        verify(audioPlayer, never()).play(any(), any())
        assertEquals(NO_ID, controller.state.value.current.playingId)
    }

    @Test
    fun `play rejects attachment without recording uri`() {
        val controller = AudioPlayerController(audioPlayer) { null }

        controller.play(audioRecordingAttachment())

        verify(audioPlayer, never()).play(any(), any())
        assertEquals(NO_ID, controller.state.value.current.playingId)
    }

    @Test
    fun `play starts playback and exposes track metadata`() {
        val attachment = audioRecordingAttachment()

        controller.play(attachment)

        verify(audioPlayer).play(RECORDING_URI, attachment.audioHash)
        val current = controller.state.value.current
        assertEquals(attachment.audioHash, current.playingId)
        assertEquals(RECORDING_URI, current.audioUri)
        assertEquals(WAVEFORM, current.waveform)
        assertEquals(DURATION_IN_MS, current.durationInMs)
        assertEquals(0, current.playbackInMs)
    }

    @Test
    fun `play reflects the loading state of the audio player`() {
        whenever(audioPlayer.currentState) doReturn AudioState.LOADING

        controller.play(audioRecordingAttachment())

        assertTrue(controller.state.value.current.isLoading)
        assertFalse(controller.state.value.current.isPlaying)
    }

    @Test
    fun `play resumes from the stored seek position`() {
        val attachment = audioRecordingAttachment()
        controller.seekTo(attachment, progress = 0.5f)

        controller.play(attachment)

        verify(audioPlayer, times(2)).seekTo(HALF_DURATION_IN_MS, attachment.audioHash)
        val current = controller.state.value.current
        assertEquals(HALF_DURATION_IN_MS, current.playbackInMs)
        assertEquals(0.5f, current.playingProgress)
    }

    @Test
    fun `audio state changes from the player update the playing flags`() {
        val listeners = playAndCaptureListeners(audioRecordingAttachment())

        listeners.onAudioState(AudioState.PLAYING)
        assertTrue(controller.state.value.current.isPlaying)

        listeners.onAudioState(AudioState.PAUSE)
        assertFalse(controller.state.value.current.isPlaying)
    }

    @Test
    fun `progress updates from the player update the playback position`() {
        val listeners = playAndCaptureListeners(audioRecordingAttachment())

        listeners.onProgress(
            ProgressData(currentPosition = 2500, progress = 0.25f, duration = DURATION_IN_MS),
        )

        val current = controller.state.value.current
        assertTrue(current.isPlaying)
        assertEquals(0.25f, current.playingProgress)
        assertEquals(2500, current.playbackInMs)
        assertEquals(DURATION_IN_MS, current.durationInMs)
    }

    @Test
    fun `progress updates from the player clear the stored seek position`() {
        val attachment = audioRecordingAttachment()
        controller.seekTo(attachment, progress = 0.5f)
        val listeners = playAndCaptureListeners(attachment)

        listeners.onProgress(
            ProgressData(currentPosition = 6000, progress = 0.6f, duration = DURATION_IN_MS),
        )

        assertFalse(attachment.audioHash in controller.state.value.seekTo)
    }

    @Test
    fun `speed changes from the player update the playing speed`() {
        val attachment = audioRecordingAttachment()
        val listeners = playAndCaptureListeners(attachment)

        listeners.onSpeed(1.5f)

        assertEquals(1.5f, controller.state.value.current.playingSpeed)
        assertEquals(1.5f, controller.state.value.speeds.getOrDefault(attachment.audioHash, 0f))
    }

    @Test
    fun `player callbacks are ignored after reset`() {
        val listeners = playAndCaptureListeners(audioRecordingAttachment())
        controller.reset()

        listeners.onAudioState(AudioState.PLAYING)
        listeners.onProgress(
            ProgressData(currentPosition = 2500, progress = 0.25f, duration = DURATION_IN_MS),
        )
        listeners.onSpeed(1.5f)

        val current = controller.state.value.current
        assertEquals(NO_ID, current.playingId)
        assertFalse(current.isPlaying)
        assertEquals(1.0f, current.playingSpeed)
    }

    @Test
    fun `togglePlayback rejects attachment which is not an audio recording`() {
        controller.togglePlayback(randomAttachment(type = AttachmentType.IMAGE))

        verify(audioPlayer, never()).play(any(), any())
        verify(audioPlayer, never()).pause()
    }

    @Test
    fun `togglePlayback starts playback when there is no prior progress`() {
        val attachment = audioRecordingAttachment()

        controller.togglePlayback(attachment)

        verify(audioPlayer).play(RECORDING_URI, attachment.audioHash)
    }

    @Test
    fun `togglePlayback pauses the current playing track`() {
        val attachment = audioRecordingAttachment()
        val listeners = playAndCaptureListeners(attachment)
        listeners.onProgress(
            ProgressData(currentPosition = HALF_DURATION_IN_MS, progress = 0.5f, duration = DURATION_IN_MS),
        )
        whenever(audioPlayer.currentPlayingId) doReturn attachment.audioHash

        controller.togglePlayback(attachment)

        verify(audioPlayer).pause()
    }

    @Test
    fun `togglePlayback resumes the current paused track`() {
        val attachment = audioRecordingAttachment()
        val listeners = playAndCaptureListeners(attachment)
        listeners.onProgress(
            ProgressData(currentPosition = HALF_DURATION_IN_MS, progress = 0.5f, duration = DURATION_IN_MS),
        )
        listeners.onAudioState(AudioState.PAUSE)
        whenever(audioPlayer.currentPlayingId) doReturn attachment.audioHash
        whenever(audioPlayer.currentState) doReturn AudioState.PAUSE

        controller.togglePlayback(attachment)

        verify(audioPlayer).resume(attachment.audioHash)
    }

    @Test
    fun `togglePlayback restarts a partially played track when another audio is loaded`() {
        val attachment = audioRecordingAttachment()
        val listeners = playAndCaptureListeners(attachment)
        listeners.onProgress(
            ProgressData(currentPosition = HALF_DURATION_IN_MS, progress = 0.5f, duration = DURATION_IN_MS),
        )
        whenever(audioPlayer.currentPlayingId) doReturn attachment.audioHash + 1

        controller.togglePlayback(attachment)

        verify(audioPlayer, times(2)).play(RECORDING_URI, attachment.audioHash)
        verify(audioPlayer).seekTo(HALF_DURATION_IN_MS, attachment.audioHash)
        assertEquals(HALF_DURATION_IN_MS, controller.state.value.current.playbackInMs)
    }

    @Test
    fun `changeSpeed rejects attachment which is not an audio recording`() {
        controller.changeSpeed(randomAttachment(type = AttachmentType.IMAGE))

        verify(audioPlayer, never()).changeSpeed(any())
    }

    @Test
    fun `changeSpeed updates the speed of the current track`() {
        val attachment = audioRecordingAttachment()
        whenever(audioPlayer.changeSpeed(attachment.audioHash)) doReturn 1.5f
        controller.play(attachment)

        controller.changeSpeed(attachment)

        assertEquals(1.5f, controller.state.value.current.playingSpeed)
        assertEquals(1.5f, controller.state.value.speeds.getOrDefault(attachment.audioHash, 0f))
    }

    @Test
    fun `changeSpeed stores the speed of a track which is not playing`() {
        val attachment = audioRecordingAttachment()
        whenever(audioPlayer.changeSpeed(attachment.audioHash)) doReturn 2f

        controller.changeSpeed(attachment)

        assertEquals(1.0f, controller.state.value.current.playingSpeed)
        assertEquals(2f, controller.state.value.speeds.getOrDefault(attachment.audioHash, 0f))
    }

    @Test
    fun `startSeek is ignored for a track which is not playing`() {
        controller.startSeek(audioRecordingAttachment())

        verify(audioPlayer, never()).startSeek(any())
    }

    @Test
    fun `startSeek marks the current track as seeking`() {
        val attachment = audioRecordingAttachment()
        controller.play(attachment)

        controller.startSeek(attachment)

        verify(audioPlayer).startSeek(attachment.audioHash)
        assertTrue(controller.state.value.current.isSeeking)
    }

    @Test
    fun `seekTo moves the playback position of the current track`() {
        val attachment = audioRecordingAttachment()
        controller.play(attachment)

        controller.seekTo(attachment, progress = 0.5f)

        verify(audioPlayer).seekTo(HALF_DURATION_IN_MS, attachment.audioHash)
        val current = controller.state.value.current
        assertFalse(current.isSeeking)
        assertEquals(0.5f, current.playingProgress)
        assertEquals(HALF_DURATION_IN_MS, current.playbackInMs)
        assertEquals(0.5f, controller.state.value.seekTo.getOrDefault(attachment.audioHash, 0f))
    }

    @Test
    fun `seekTo stores the position of a track which is not playing`() {
        val attachment = audioRecordingAttachment()

        controller.seekTo(attachment, progress = 0.25f)

        verify(audioPlayer).seekTo(2500, attachment.audioHash)
        assertEquals(NO_ID, controller.state.value.current.playingId)
        assertEquals(0.25f, controller.state.value.seekTo.getOrDefault(attachment.audioHash, 0f))
    }

    @Test
    fun `resetAudio is ignored for a track which is not playing`() {
        controller.resetAudio(audioRecordingAttachment())

        verify(audioPlayer, never()).resetAudio(any())
    }

    @Test
    fun `resetAudio resets the current track`() {
        val attachment = audioRecordingAttachment()
        controller.play(attachment)

        controller.resetAudio(attachment)

        verify(audioPlayer).resetAudio(attachment.audioHash)
    }

    @Test
    fun `pause is ignored when no track is loaded`() {
        controller.pause()

        verify(audioPlayer, never()).pause()
    }

    @Test
    fun `pause is ignored when the current track is not playing`() {
        controller.play(audioRecordingAttachment())

        controller.pause()

        verify(audioPlayer, never()).pause()
    }

    @Test
    fun `pause pauses the current playing track`() {
        val listeners = playAndCaptureListeners(audioRecordingAttachment())
        listeners.onAudioState(AudioState.PLAYING)

        controller.pause()

        verify(audioPlayer).pause()
    }

    @Test
    fun `resume is ignored when no track is loaded`() {
        controller.resume()

        verify(audioPlayer, never()).resume(any())
    }

    @Test
    fun `resume is ignored when the current track is already playing`() {
        val listeners = playAndCaptureListeners(audioRecordingAttachment())
        listeners.onAudioState(AudioState.PLAYING)

        controller.resume()

        verify(audioPlayer, never()).resume(any())
    }

    @Test
    fun `resume is ignored when the player is not idle or paused`() {
        controller.play(audioRecordingAttachment())
        whenever(audioPlayer.currentState) doReturn AudioState.LOADING

        controller.resume()

        verify(audioPlayer, never()).resume(any())
    }

    @Test
    fun `resume resumes the current paused track`() {
        val attachment = audioRecordingAttachment()
        val listeners = playAndCaptureListeners(attachment)
        listeners.onAudioState(AudioState.PAUSE)
        whenever(audioPlayer.currentState) doReturn AudioState.PAUSE

        controller.resume()

        verify(audioPlayer).resume(attachment.audioHash)
    }

    @Test
    fun `reset clears the player and the state`() {
        val attachment = audioRecordingAttachment()
        val listeners = playAndCaptureListeners(attachment)
        listeners.onProgress(
            ProgressData(currentPosition = HALF_DURATION_IN_MS, progress = 0.5f, duration = DURATION_IN_MS),
        )
        controller.changeSpeed(attachment)

        controller.reset()

        verify(audioPlayer).reset()
        val state = controller.state.value
        assertEquals(NO_ID, state.current.playingId)
        assertEquals(0, state.seekTo.size)
        assertEquals(0, state.speeds.size)
    }

    private fun playAndCaptureListeners(attachment: Attachment): PlayerListeners {
        controller.play(attachment)
        val stateCaptor = argumentCaptor<(AudioState) -> Unit>()
        val progressCaptor = argumentCaptor<(ProgressData) -> Unit>()
        val speedCaptor = argumentCaptor<(Float) -> Unit>()
        verify(audioPlayer).registerOnAudioStateChange(eq(attachment.audioHash), stateCaptor.capture())
        verify(audioPlayer).registerOnProgressStateChange(eq(attachment.audioHash), progressCaptor.capture())
        verify(audioPlayer).registerOnSpeedChange(eq(attachment.audioHash), speedCaptor.capture())
        return PlayerListeners(
            onAudioState = stateCaptor.firstValue,
            onProgress = progressCaptor.firstValue,
            onSpeed = speedCaptor.firstValue,
        )
    }

    private class PlayerListeners(
        val onAudioState: (AudioState) -> Unit,
        val onProgress: (ProgressData) -> Unit,
        val onSpeed: (Float) -> Unit,
    )

    private fun audioRecordingAttachment(): Attachment = randomAttachment(
        type = AttachmentType.AUDIO_RECORDING,
        extraData = mapOf(
            EXTRA_DURATION to DURATION_IN_SECONDS,
            EXTRA_WAVEFORM_DATA to WAVEFORM,
        ),
    )

    private companion object {
        private const val NO_ID = -1
        private const val RECORDING_URI = "recording-uri"
        private const val DURATION_IN_SECONDS = 10f
        private const val DURATION_IN_MS = 10_000
        private const val HALF_DURATION_IN_MS = 5_000
        private val WAVEFORM = listOf(0.1f, 0.5f, 0.9f)
    }
}
