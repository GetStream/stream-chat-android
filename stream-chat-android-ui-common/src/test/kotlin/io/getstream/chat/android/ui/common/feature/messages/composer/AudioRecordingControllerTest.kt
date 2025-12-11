/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.sdk.chat.audio.recording.RecordedMedia
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
internal class AudioRecordingControllerTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var mockAudioPlayer: AudioPlayer
    private lateinit var mockMediaRecorder: StreamMediaRecorder
    private val mockFile: File = mock()
    private val fileToUri: (File) -> String = { file -> "file://${file.absolutePath}" }
    private lateinit var controller: AudioRecordingController

    @BeforeEach
    fun setUp() {
        mockAudioPlayer = mock()
        mockMediaRecorder = mock()
        controller = AudioRecordingController(
            audioPlayer = mockAudioPlayer,
            mediaRecorder = mockMediaRecorder,
            fileToUri = fileToUri,
            scope = testCoroutineRule.scope,
        )
    }

    @Test
    fun `Given Idle state When startRecording is called Then state transitions to Hold`() = runTest {
        // Given
        whenever(mockMediaRecorder.startAudioRecording(any<String>(), any<Long>(), any<Boolean>())) doReturn Result.Success(mockFile)

        // When
        controller.startRecording()

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Hold)
        assertEquals(RecordingState.Hold.ZeroOffset, (state as RecordingState.Hold).offset)
        verify(mockMediaRecorder).startAudioRecording(any<String>(), any<Long>(), eq(true))
    }

    @Test
    fun `Given Idle state When startRecording with offset is called Then state transitions to Hold with offset`() = runTest {
        // Given
        val offset = Pair(10f, 20f)
        whenever(mockMediaRecorder.startAudioRecording(any<String>(), any<Long>(), any<Boolean>())) doReturn Result.Success(mockFile)

        // When
        controller.startRecording(offset)

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Hold)
        assertEquals(offset, (state as RecordingState.Hold).offset)
    }

    @Test
    fun `Given non-Idle state When startRecording is called Then state remains unchanged`() = runTest {
        // Given
        whenever(mockMediaRecorder.startAudioRecording(any<String>(), any<Long>(), any<Boolean>())) doReturn Result.Success(mockFile)
        controller.startRecording()
        val stateBeforeSecondCall = controller.recordingState.value

        // When
        controller.startRecording()

        // Then
        assertEquals(stateBeforeSecondCall, controller.recordingState.value)
    }

    @Test
    fun `Given Hold state When holdRecording with offset is called Then offset is updated`() {
        // Given
        controller.recordingState.value = RecordingState.Hold()
        val newOffset = Pair(15f, 25f)

        // When
        controller.holdRecording(newOffset)

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Hold)
        assertEquals(newOffset, (state as RecordingState.Hold).offset)
    }

    @Test
    fun `Given Hold state When holdRecording with null offset is called Then state remains unchanged`() {
        // Given
        val originalOffset = Pair(10f, 20f)
        controller.recordingState.value = RecordingState.Hold(offset = originalOffset)

        // When
        controller.holdRecording(null)

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Hold)
        assertEquals(originalOffset, (state as RecordingState.Hold).offset)
    }

    @Test
    fun `Given non-Hold state When holdRecording is called Then state remains unchanged`() {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        controller.holdRecording(Pair(10f, 20f))

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Hold state When lockRecording is called Then state transitions to Locked`() {
        // Given
        val duration = 5000
        val waveform = listOf(0.5f, 0.6f, 0.7f)
        controller.recordingState.value = RecordingState.Hold(durationInMs = duration, waveform = waveform)

        // When
        controller.lockRecording()

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Locked)
        assertEquals(duration, (state as RecordingState.Locked).durationInMs)
        assertEquals(waveform, state.waveform)
    }

    @Test
    fun `Given non-Hold state When lockRecording is called Then state remains unchanged`() {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        controller.lockRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Recording state When cancelRecording is called Then state transitions to Idle`() {
        // Given
        controller.recordingState.value = RecordingState.Hold()

        // When
        controller.cancelRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
        verify(mockAudioPlayer, never()).resetAudio(any())
        verify(mockMediaRecorder).release()
    }

    @Test
    fun `Given Overview state with playing audio When cancelRecording is called Then audio is reset and state transitions to Idle`() {
        // Given
        val attachment = Attachment(upload = mockFile)
        val playingId = 12345
        controller.recordingState.value = RecordingState.Overview(
            attachment = attachment,
            isPlaying = true,
            playingId = playingId,
        )

        // When
        controller.cancelRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
        verify(mockAudioPlayer).resetAudio(playingId)
        verify(mockMediaRecorder).release()
    }

    @Test
    fun `Given Idle state When cancelRecording is called Then state remains Idle`() {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        controller.cancelRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
        verify(mockMediaRecorder, never()).release()
    }

    @Test
    fun `Given Overview state When toggleRecordingPlayback is called first time Then audio starts playing`() {
        // Given
        val attachment = Attachment(upload = mockFile)
        controller.recordingState.value = RecordingState.Overview(attachment = attachment)

        // When
        controller.toggleRecordingPlayback()

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Overview)
        assertTrue((state as RecordingState.Overview).isPlaying)
        verify(mockAudioPlayer).play(any(), any())
    }

    @Test
    fun `Given Overview state with playing audio When toggleRecordingPlayback is called Then audio pauses`() {
        // Given
        val attachment = Attachment(upload = mockFile)
        val audioHash = attachment.hashCode()
        whenever(mockAudioPlayer.currentPlayingId) doReturn audioHash
        controller.recordingState.value = RecordingState.Overview(
            attachment = attachment,
            isPlaying = true,
            playingId = audioHash,
        )

        // When
        controller.toggleRecordingPlayback()

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Overview)
        assertFalse((state as RecordingState.Overview).isPlaying)
        verify(mockAudioPlayer).pause()
    }

    @Test
    fun `Given Overview state with paused audio When toggleRecordingPlayback is called Then audio resumes`() {
        // Given
        val attachment = Attachment(upload = mockFile)
        val audioHash = attachment.hashCode()
        whenever(mockAudioPlayer.currentPlayingId) doReturn audioHash
        controller.recordingState.value = RecordingState.Overview(
            attachment = attachment,
            isPlaying = false,
            playingId = audioHash,
        )

        // When
        controller.toggleRecordingPlayback()

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Overview)
        assertTrue((state as RecordingState.Overview).isPlaying)
        verify(mockAudioPlayer).resume(audioHash)
    }

    @Test
    fun `Given Overview state without audio file When toggleRecordingPlayback is called Then state remains unchanged`() {
        // Given
        val attachment = Attachment(upload = null)
        controller.recordingState.value = RecordingState.Overview(attachment = attachment)
        val stateBefore = controller.recordingState.value

        // When
        controller.toggleRecordingPlayback()

        // Then
        assertEquals(stateBefore, controller.recordingState.value)
    }

    @Test
    fun `Given non-Overview state When toggleRecordingPlayback is called Then state remains unchanged`() {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        controller.toggleRecordingPlayback()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Locked state When stopRecording succeeds Then state transitions to Overview`() = runTest {
        // Given
        val duration = 5000
        val attachment = Attachment(upload = mockFile)
        val recordedMedia = RecordedMedia(durationInMs = duration, attachment = attachment)
        whenever(mockMediaRecorder.stopRecording()) doReturn Result.Success(recordedMedia)
        controller.recordingState.value = RecordingState.Locked()

        // When
        controller.stopRecording()

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Overview)
        assertEquals(duration, (state as RecordingState.Overview).durationInMs)
        assertEquals(attachment, state.attachment)
    }

    @Test
    fun `Given Locked state When stopRecording fails Then state transitions to Idle`() = runTest {
        // Given
        whenever(mockMediaRecorder.stopRecording()) doReturn Result.Failure(Error.GenericError("Failed to stop"))
        controller.recordingState.value = RecordingState.Locked()

        // When
        controller.stopRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given non-Locked state When stopRecording is called Then state remains unchanged`() = runTest {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        controller.stopRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Overview state When seekRecordingTo is called Then playback position and progress are updated`() {
        // Given
        val duration = 10000
        val attachment = Attachment(upload = mockFile)
        val audioHash = attachment.hashCode()
        controller.recordingState.value = RecordingState.Overview(
            durationInMs = duration,
            attachment = attachment,
        )
        val progress = 0.5f

        // When
        controller.seekRecordingTo(progress)

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Overview)
        assertEquals(progress, (state as RecordingState.Overview).playingProgress)
        verify(mockAudioPlayer).seekTo((progress * duration).toInt(), audioHash)
    }

    @Test
    fun `Given Overview state without audio file When seekRecordingTo is called Then state remains unchanged`() {
        // Given
        val attachment = Attachment(upload = null)
        controller.recordingState.value = RecordingState.Overview(attachment = attachment)
        val stateBefore = controller.recordingState.value

        // When
        controller.seekRecordingTo(0.5f)

        // Then
        assertEquals(stateBefore, controller.recordingState.value)
    }

    @Test
    fun `Given non-Overview state When seekRecordingTo is called Then state remains unchanged`() {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        controller.seekRecordingTo(0.5f)

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Overview state When pauseRecording is called Then isPlaying becomes false`() {
        // Given
        val playingId = 12345
        val attachment = Attachment(upload = mockFile)
        controller.recordingState.value = RecordingState.Overview(
            attachment = attachment,
            isPlaying = true,
            playingId = playingId,
        )

        // When
        controller.pauseRecording()

        // Then
        val state = controller.recordingState.value
        assertTrue(state is RecordingState.Overview)
        assertFalse((state as RecordingState.Overview).isPlaying)
        verify(mockAudioPlayer).startSeek(playingId)
    }

    @Test
    fun `Given non-Overview state When pauseRecording is called Then state remains unchanged`() {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        controller.pauseRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Idle state When completeRecordingSync is called Then Failure result is returned`() = runTest {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        val result = controller.completeRecordingSync()

        // Then
        assertTrue(result is Result.Failure)
        assertEquals("Recording is in Idle state", (result as Result.Failure).value.message)
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Overview state When completeRecordingSync is called Then Success result with attachment is returned`() = runTest {
        // Given
        val attachment = Attachment(upload = mockFile)
        val waveform = listOf(0.5f, 0.6f, 0.7f)
        controller.recordingState.value = RecordingState.Overview(
            attachment = attachment,
            waveform = waveform,
        )

        // When
        val result = controller.completeRecordingSync()

        // Then
        assertTrue(result is Result.Success)
        val resultAttachment = (result as Result.Success).value
        assertTrue(resultAttachment.extraData.containsKey("waveform_data"))
        assertEquals(waveform, resultAttachment.extraData["waveform_data"])
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Hold state When completeRecordingSync succeeds Then Success result with attachment is returned`() = runTest {
        // Given
        val attachment = Attachment(upload = mockFile)
        val recordedMedia = RecordedMedia(durationInMs = 5000, attachment = attachment)
        whenever(mockMediaRecorder.stopRecording()) doReturn Result.Success(recordedMedia)
        controller.recordingState.value = RecordingState.Hold()

        // When
        val result = controller.completeRecordingSync()

        // Then
        assertTrue(result is Result.Success)
        val resultAttachment = (result as Result.Success).value
        assertTrue(resultAttachment.extraData.containsKey("waveform_data"))
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Hold state When completeRecordingSync fails Then Failure result is returned`() = runTest {
        // Given
        val error = Error.GenericError("Failed to complete")
        whenever(mockMediaRecorder.stopRecording()) doReturn Result.Failure(error)
        controller.recordingState.value = RecordingState.Hold()

        // When
        val result = controller.completeRecordingSync()

        // Then
        assertTrue(result is Result.Failure)
        assertEquals(error, (result as Result.Failure).value)
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Locked state When completeRecordingSync succeeds Then Success result with attachment is returned`() = runTest {
        // Given
        val attachment = Attachment(upload = mockFile)
        val recordedMedia = RecordedMedia(durationInMs = 5000, attachment = attachment)
        whenever(mockMediaRecorder.stopRecording()) doReturn Result.Success(recordedMedia)
        controller.recordingState.value = RecordingState.Locked()

        // When
        val result = controller.completeRecordingSync()

        // Then
        assertTrue(result is Result.Success)
        val resultAttachment = (result as Result.Success).value
        assertTrue(resultAttachment.extraData.containsKey("waveform_data"))
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `Given Overview state When completeRecording is called Then state transitions through Complete to Idle`() = runTest {
        // Given
        Dispatchers.setMain(testCoroutineRule.testDispatcher)
        val attachment = Attachment(upload = mockFile)
        val playingId = 12345
        controller.recordingState.value = RecordingState.Overview(
            attachment = attachment,
            playingId = playingId,
        )

        // When
        controller.completeRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
        verify(mockAudioPlayer).resetAudio(playingId)

        Dispatchers.resetMain()
    }

    @Test
    fun `Given Hold state When completeRecording succeeds Then state transitions through Complete to Idle`() = runTest {
        // Given
        Dispatchers.setMain(testCoroutineRule.testDispatcher)
        val attachment = Attachment(upload = mockFile)
        val recordedMedia = RecordedMedia(durationInMs = 5000, attachment = attachment)
        whenever(mockMediaRecorder.stopRecording()) doReturn Result.Success(recordedMedia)
        controller.recordingState.value = RecordingState.Hold()

        // When
        controller.completeRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
        verify(mockMediaRecorder).stopRecording()

        Dispatchers.resetMain()
    }

    @Test
    fun `Given Hold state When completeRecording fails Then state transitions to Idle`() = runTest {
        // Given
        whenever(mockMediaRecorder.stopRecording()) doReturn Result.Failure(Error.GenericError("Failed"))
        controller.recordingState.value = RecordingState.Hold()

        // When
        controller.completeRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
        verify(mockMediaRecorder).stopRecording()
    }

    @Test
    fun `Given Idle state When completeRecording is called Then state remains Idle`() = runTest {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        controller.completeRecording()

        // Then
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `When onCleared is called Then resources are released and state becomes Idle`() {
        // Given
        val attachment = Attachment(upload = mockFile)
        val playingId = 12345
        controller.recordingState.value = RecordingState.Overview(
            attachment = attachment,
            playingId = playingId,
        )

        // When
        controller.onCleared()

        // Then
        verify(mockMediaRecorder).release()
        verify(mockAudioPlayer).resetAudio(playingId)
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }

    @Test
    fun `When onCleared is called with Idle state Then only mediaRecorder is released`() {
        // Given
        controller.recordingState.value = RecordingState.Idle

        // When
        controller.onCleared()

        // Then
        verify(mockMediaRecorder).release()
        assertTrue(controller.recordingState.value is RecordingState.Idle)
    }
}
