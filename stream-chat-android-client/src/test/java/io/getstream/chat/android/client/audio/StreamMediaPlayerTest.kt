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

package io.getstream.chat.android.client.audio

import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class StreamMediaPlayerTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var userScope: UserTestScope
    private lateinit var mediaPlayer: NativeMediaPlayerMock
    private lateinit var streamPlayer: StreamAudioPlayer

    @BeforeEach
    fun setUp() {
        userScope = UserTestScope(testCoroutines.scope)
        mediaPlayer = NativeMediaPlayerMock(userScope)
        streamPlayer = StreamAudioPlayer(
            mediaPlayer = mediaPlayer,
            userScope = userScope,
        )
    }

    @Test
    fun `test reset scenario`() = runTest {
        /* Given */
        val sourceUrl = randomString()
        val audioHash = randomInt()
        val audioStates = arrayListOf(streamPlayer.currentState)

        /* When */
        streamPlayer.registerOnAudioStateChange(audioHash) {
            audioStates.add(it)
        }
        delay(1000)
        streamPlayer.play(sourceUrl, audioHash)
        delay(1000)
        streamPlayer.resetAudio(audioHash)
        delay(1000)

        /* Then */
        mediaPlayer.state shouldBeEqualTo NativeMediaPlayerState.IDLE
        audioStates shouldBeEqualTo listOf(
            AudioState.UNSET,
            AudioState.LOADING,
            AudioState.IDLE,
            AudioState.PLAYING,
            AudioState.UNSET,
        )
        streamPlayer.currentState shouldBeEqualTo AudioState.UNSET
    }

    @Test
    fun `test replay scenario`() = runTest {
        /* Given */
        val sourceUrl = randomString()
        val audioHash = randomInt()
        val audioStates = arrayListOf(streamPlayer.currentState)

        /* When */
        streamPlayer.registerOnAudioStateChange(audioHash) {
            audioStates.add(it)
        }
        delay(1000)
        streamPlayer.play(sourceUrl, audioHash)
        delay(1000)
        streamPlayer.resetAudio(audioHash)
        delay(1000)
        streamPlayer.registerOnAudioStateChange(audioHash) {
            audioStates.add(it)
        }
        delay(1000)
        streamPlayer.play(sourceUrl, audioHash)
        delay(1000)
        streamPlayer.resetAudio(audioHash)
        delay(1000)

        /* Then */
        mediaPlayer.state shouldBeEqualTo NativeMediaPlayerState.IDLE
        audioStates shouldBeEqualTo listOf(
            AudioState.UNSET,
            AudioState.LOADING,
            AudioState.IDLE,
            AudioState.PLAYING,
            AudioState.UNSET,
            AudioState.LOADING,
            AudioState.IDLE,
            AudioState.PLAYING,
            AudioState.UNSET,
        )
        streamPlayer.currentState shouldBeEqualTo AudioState.UNSET
    }

    @Test
    fun `test complete scenario`() = runTest {
        /* Given */
        val sourceUrl = randomString()
        val audioHash = randomInt()
        val audioStates = arrayListOf(streamPlayer.currentState)

        /* When */
        streamPlayer.registerOnAudioStateChange(audioHash) {
            audioStates.add(it)
        }
        delay(1000)
        streamPlayer.play(sourceUrl, audioHash)
        delay(1000)
        mediaPlayer.complete()
        delay(1000)

        /* Then */
        mediaPlayer.state shouldBeEqualTo NativeMediaPlayerState.PLAYBACK_COMPLETED
        audioStates shouldBeEqualTo listOf(
            AudioState.UNSET,
            AudioState.LOADING,
            AudioState.IDLE,
            AudioState.PLAYING,
            AudioState.IDLE,
        )
        streamPlayer.currentState shouldBeEqualTo AudioState.IDLE
    }

    @Test
    fun `test changeSpeed cycles through speeds`() = runTest {
        /* Given */
        val audioHash = randomInt()

        /* When & Then */
        streamPlayer.changeSpeed(audioHash) shouldBeEqualTo 1.5f
        streamPlayer.changeSpeed(audioHash) shouldBeEqualTo 2.0f
        streamPlayer.changeSpeed(audioHash) shouldBeEqualTo 1.0f
        streamPlayer.changeSpeed(audioHash) shouldBeEqualTo 1.5f
    }

    @Test
    fun `test changeSpeed maintains independent speeds per audio`() = runTest {
        /* Given */
        val audioHash1 = randomInt()
        val audioHash2 = randomInt()

        /* When */
        streamPlayer.changeSpeed(audioHash1) // 1.5
        streamPlayer.changeSpeed(audioHash1) // 2.0
        streamPlayer.changeSpeed(audioHash2) // 1.5

        /* Then */
        streamPlayer.changeSpeed(audioHash1) shouldBeEqualTo 1.0f // cycles back
        streamPlayer.changeSpeed(audioHash2) shouldBeEqualTo 2.0f // continues from 1.5
    }

    @AfterEach
    fun tearDown() {
        userScope.cancelChildren()
    }
}
