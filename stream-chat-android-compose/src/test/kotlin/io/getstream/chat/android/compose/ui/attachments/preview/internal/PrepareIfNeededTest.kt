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

package io.getstream.chat.android.compose.ui.attachments.preview.internal

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards [prepareIfNeeded] against regressions: it must skip only a redundant prepare of the URL
 * the player already holds, and still (re)prepare whenever the URL changes or the player is fresh.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class PrepareIfNeededTest {

    private val url = "https://cdn.example.com/video.mp4"
    private val otherUrl = "https://cdn.example.com/other.mp4"

    @Test
    fun `prepares when the player has no media item`() {
        val player = mock<Player>() // currentMediaItem defaults to null

        player.prepareIfNeeded(url)

        verify(player).setMediaItem(any<MediaItem>(), eq(0L))
        verify(player).prepare()
    }

    @Test
    fun `skips prepare when already prepared with the same url`() {
        val player = mock<Player> {
            on { currentMediaItem } doReturn MediaItem.fromUri(url)
        }

        player.prepareIfNeeded(url)

        verify(player, never()).setMediaItem(any<MediaItem>(), any<Long>())
        verify(player, never()).prepare()
    }

    @Test
    fun `prepares when the player holds a different url`() {
        val player = mock<Player> {
            on { currentMediaItem } doReturn MediaItem.fromUri(otherUrl)
        }

        player.prepareIfNeeded(url)

        verify(player).setMediaItem(any<MediaItem>(), eq(0L))
        verify(player).prepare()
    }

    @Test
    fun `prepares from the given start position`() {
        val player = mock<Player>()

        player.prepareIfNeeded(url, startPositionMs = 5_000L)

        verify(player).setMediaItem(any<MediaItem>(), eq(5_000L))
        verify(player).prepare()
    }
}
