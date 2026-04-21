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

package io.getstream.chat.android.compose.ui.messages.composer.internal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.Reply
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class CommandSelectionRoutingTest {

    private val giphy = Command("giphy", "Search GIFs", "[text]", "fun_set")
    private val mute = Command("mute", "Mute user", "[@username]", "moderation_set")

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `Available command invokes onAvailable`() {
        var received: Command? = null

        routeCommandSelection(
            command = giphy,
            action = null,
            context = context,
            onAvailable = { received = it },
        )

        assertEquals(giphy, received)
    }

    @Test
    fun `Unavailable command in reply mode does not invoke onAvailable`() {
        var received: Command? = null

        routeCommandSelection(
            command = mute,
            action = Reply(randomMessage()),
            context = context,
            onAvailable = { received = it },
        )

        assertNull(received)
    }

    @Test
    fun `Unavailable command in edit mode does not invoke onAvailable`() {
        var received: Command? = null

        routeCommandSelection(
            command = giphy,
            action = Edit(randomMessage()),
            context = context,
            onAvailable = { received = it },
        )

        assertNull(received)
    }

    @Test
    fun `Non-moderation command in reply mode invokes onAvailable`() {
        var received: Command? = null

        routeCommandSelection(
            command = giphy,
            action = Reply(randomMessage()),
            context = context,
            onAvailable = { received = it },
        )

        assertEquals(giphy, received)
    }
}
