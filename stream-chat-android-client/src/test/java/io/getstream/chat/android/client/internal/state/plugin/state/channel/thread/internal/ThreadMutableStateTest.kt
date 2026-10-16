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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.thread.internal

import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.TestScope
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class ThreadMutableStateTest {

    private val parentId = randomString()
    private val state = ThreadMutableState(parentId, TestScope())

    @Test
    fun `Given loaded replies When the replies are set Should keep only the parent and the new replies`() {
        val parent = randomMessage(id = parentId, syncStatus = SyncStatus.COMPLETED)
        val loadedReply = randomMessage(parentId = parentId, syncStatus = SyncStatus.COMPLETED)
        val newReply = randomMessage(parentId = parentId, syncStatus = SyncStatus.COMPLETED)
        state.upsertMessages(listOf(parent, loadedReply))

        state.setReplies(listOf(newReply))

        state.rawMessage.value shouldBeEqualTo mapOf(parent.id to parent, newReply.id to newReply)
    }

    @Test
    fun `Given a reply that is not sent yet When the replies are set Should keep it`() {
        val pendingReply = randomMessage(parentId = parentId, syncStatus = SyncStatus.SYNC_NEEDED)
        val newReply = randomMessage(parentId = parentId, syncStatus = SyncStatus.COMPLETED)
        state.upsertMessages(listOf(pendingReply))

        state.setReplies(listOf(newReply))

        state.rawMessage.value shouldBeEqualTo mapOf(pendingReply.id to pendingReply, newReply.id to newReply)
    }
}
