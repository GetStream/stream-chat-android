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

package io.getstream.chat.android.compose.ui.components.audio

import androidx.collection.IntFloatMap
import androidx.collection.intFloatMapOf
import io.getstream.chat.android.client.audio.audioHash
import io.getstream.chat.android.client.extensions.EXTRA_DURATION
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AudioPlaybackTest {

    @Test
    fun `an attachment without a url has no source`() {
        val playback = playerState().playbackOf(audio(assetUrl = null))

        assertFalse(playback.hasSource)
        assertFalse(playback.playing)
    }

    @Test
    fun `a blank url is not a source, and does not match the idle player`() {
        // CurrentAudioState.audioUri defaults to "", so a blank url must not read as the loaded track.
        val playback = playerState().playbackOf(audio(assetUrl = ""))

        assertFalse(playback.hasSource)
        assertFalse(playback.playing)
    }

    @Test
    fun `the loaded track reports the player's progress and duration`() {
        val attachment = audio()
        val state = playerState(
            current = AudioPlayerState.CurrentAudioState(
                audioUri = ASSET_URL,
                isPlaying = true,
                playingProgress = 0.25f,
                durationInMs = 40_000,
            ),
        )

        val playback = state.playbackOf(attachment)

        assertTrue(playback.playing)
        assertEquals(0.25f, playback.progress)
        assertEquals(40_000, playback.durationInMs)
    }

    @Test
    fun `a duration of zero is not a known duration`() {
        val attachment = audio(durationInSeconds = 0f)

        val playback = playerState().playbackOf(attachment)

        assertNull(playback.durationInMs)
    }

    @Test
    fun `an attachment duration is preferred over the player's`() {
        val playback = playerState().playbackOf(audio(durationInSeconds = 12f))

        assertEquals(12_000, playback.durationInMs)
    }

    @Test
    fun `a track which is not loaded reports its stored seek position`() {
        val attachment = audio()
        val state = playerState(seekTo = intFloatMapOf(attachment.audioHash, 0.4f))

        val playback = state.playbackOf(attachment)

        assertFalse(playback.playing)
        assertEquals(0.4f, playback.progress)
    }

    @Test
    fun `a progress the player could not compute is not passed on`() {
        // The player divides by the duration it reports, so an unmeasurable track yields NaN.
        val state = playerState(
            current = AudioPlayerState.CurrentAudioState(
                audioUri = ASSET_URL,
                isPlaying = true,
                playingProgress = Float.NaN,
            ),
        )

        val playback = state.playbackOf(audio())

        assertEquals(0f, playback.progress)
    }

    @Test
    fun `an out of range progress is clamped`() {
        val state = playerState(
            current = AudioPlayerState.CurrentAudioState(
                audioUri = ASSET_URL,
                isPlaying = true,
                playingProgress = 1.5f,
            ),
        )

        val playback = state.playbackOf(audio())

        assertEquals(1f, playback.progress)
    }

    private fun playerState(
        current: AudioPlayerState.CurrentAudioState = AudioPlayerState.CurrentAudioState(),
        seekTo: IntFloatMap = intFloatMapOf(),
    ) = AudioPlayerState(
        current = current,
        seekTo = seekTo,
        getRecordingUri = Attachment::assetUrl,
    )

    private fun audio(
        assetUrl: String? = ASSET_URL,
        durationInSeconds: Float? = null,
    ) = Attachment(
        type = AttachmentType.AUDIO,
        assetUrl = assetUrl,
        extraData = durationInSeconds?.let { mapOf(EXTRA_DURATION to it) } ?: emptyMap(),
    )

    private companion object {
        const val ASSET_URL = "https://example.com/audio.mp3"
    }
}
