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
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertInstanceOf
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class DefaultStreamMediaRecorderTest {

    private val mockMediaRecorder: MediaRecorder = mock()

    private val sut = DefaultStreamMediaRecorder(context = ApplicationProvider.getApplicationContext()).apply {
        buildMediaRecorder = { mockMediaRecorder }
    }

    @Test
    fun `startAudioRecording with valid file name should succeed`() {
        val filename = randomString()
        val result = sut.startAudioRecording(recordingName = filename)

        assertInstanceOf<Result.Success<File>>(result)
        assertEquals(filename, result.value.name)
    }

    @Test
    fun `startAudioRecording with invalid file name should fail`() {
        whenever(mockMediaRecorder.prepare()) doThrow RuntimeException("Prepare failed")

        val result = sut.startAudioRecording(recordingName = randomString())

        assertInstanceOf<Result.Failure>(result)
    }

    @Test
    fun `startAudioRecording with valid file should succeed`() {
        val result = sut.startAudioRecording(recordingFile = randomFile())

        assertInstanceOf<Result.Success<Unit>>(result)
    }

    @Test
    fun `startAudioRecording with invalid file should fail`() {
        whenever(mockMediaRecorder.prepare()) doThrow RuntimeException("Prepare failed")

        val result = sut.startAudioRecording(recordingFile = randomFile())

        assertInstanceOf<Result.Failure>(result)
    }

    @Test
    fun `stopRecording should release media recorder and return recorded media`() {
        val file = randomFile()
        sut.startAudioRecording(recordingFile = file)

        val result = sut.stopRecording()
        assertInstanceOf<Result.Success<RecordedMedia>>(result)

        verify(mockMediaRecorder).release()
        val recordedMedia = result.value
        assertEquals(file.name, recordedMedia.attachment.title)
    }

    @Test
    fun `stopRecording should fail when not started`() {
        val result = sut.stopRecording()

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `deleteRecording should delete file and return success`() {
        val file = randomFile()
            .also(File::createNewFile)

        val result = sut.deleteRecording(recordingFile = file)

        assertInstanceOf<Result.Success<Unit>>(result)
        assertFalse(file.exists())
    }

    @Test
    fun `deleteRecording with non-existent file should return success`() {
        val result = sut.deleteRecording(recordingFile = randomFile())

        assertTrue(result is Result.Success)
    }

    @Test
    fun `release should set state to UNINITIALIZED, release media recorder and notify listener`() {
        sut.startAudioRecording(recordingFile = randomFile())
        val mockOnRecordingStoppedListener = mock(StreamMediaRecorder.OnRecordingStopped::class.java)
        sut.setOnRecordingStoppedListener(mockOnRecordingStoppedListener)

        sut.release()

        assertEquals(MediaRecorderState.UNINITIALIZED, sut.mediaRecorderState)
        verify(mockMediaRecorder).release()
        verify(mockOnRecordingStoppedListener).onStopped()
    }

    @Test
    fun `setOnErrorListener should set error listener`() {
        val listener = mock(StreamMediaRecorder.OnErrorListener::class.java)
        sut.setOnErrorListener(listener)
        // No assertion, just ensure no exception
    }

    @Test
    fun `setOnInfoListener should set info listener`() {
        val listener = mock(StreamMediaRecorder.OnInfoListener::class.java)
        sut.setOnInfoListener(listener)
        // No assertion, just ensure no exception
    }

    @Test
    fun `setOnRecordingStartedListener should set listener`() {
        val listener = mock(StreamMediaRecorder.OnRecordingStarted::class.java)
        sut.setOnRecordingStartedListener(listener)
        // No assertion, just ensure no exception
    }

    @Test
    fun `setOnRecordingStoppedListener should set listener`() {
        val listener = mock(StreamMediaRecorder.OnRecordingStopped::class.java)
        sut.setOnRecordingStoppedListener(listener)
        // No assertion, just ensure no exception
    }

    @Test
    fun `setOnMaxAmplitudeSampledListener should set listener`() {
        val listener = mock(StreamMediaRecorder.OnMaxAmplitudeSampled::class.java)
        sut.setOnMaxAmplitudeSampledListener(listener)
        // No assertion, just ensure no exception
    }

    @Test
    fun `setOnMediaRecorderStateChangedListener should set listener`() {
        val listener = mock(StreamMediaRecorder.OnMediaRecorderStateChange::class.java)
        sut.setOnMediaRecorderStateChangedListener(listener)
        // No assertion, just ensure no exception
    }

    @Test
    fun `setOnCurrentRecordingDurationChangedListener should set listener`() {
        val listener = mock(StreamMediaRecorder.OnCurrentRecordingDurationChanged::class.java)
        sut.setOnCurrentRecordingDurationChangedListener(listener)
        // No assertion, just ensure no exception
    }
}
