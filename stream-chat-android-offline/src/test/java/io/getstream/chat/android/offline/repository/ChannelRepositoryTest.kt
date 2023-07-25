/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelRepositoryTest : BaseDomainTest2() {
    private val helper by lazy { repos }

    @Test
    fun `inserting a channel and reading it should be equal`(): Unit = runTest {
        helper.upsertChannels(listOf(data.channel1))
        val channel = helper.selectChannelWithoutMessages(data.channel1.cid)!!

        channel shouldBeEqualTo data.channel1
    }

    @Test
    fun `deleting a channel should work`(): Unit = runTest {
        helper.upsertChannels(listOf(data.channel1))
        helper.deleteChannel(data.channel1.cid)
        val entity = helper.selectChannelWithoutMessages(data.channel1.cid)

        entity.shouldBeNull()
    }

    @Test
    fun `updating a channel should work as intended`(): Unit = runTest {
        helper.upsertChannels(listOf(data.channel1, data.channel1Updated))
        val channel = helper.selectChannelWithoutMessages(data.channel1.cid)!!

        // // ignore these 4 fields
        // channel.config = data.channel1.config
        // channel.createdBy = data.channel1.createdBy
        // channel.watchers = data.channel1Updated.watchers
        // channel.watcherCount = data.channel1Updated.watcherCount
        channel shouldBeEqualTo data.channel1Updated
    }
}
