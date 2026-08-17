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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.parser2.testdata.UpdateChannelRequestTestData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class UpdateChannelRequestAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize UpdateChannelRequest for rejectInvite`() {
        val json = parser.toJson(UpdateChannelRequestTestData.rejectInviteRequest)
        Assertions.assertEquals(UpdateChannelRequestTestData.rejectInviteJson, json)
    }

    @Test
    fun `Serialize UpdateChannelRequest for addMembers with nested member and message`() {
        val json = parser.toJson(UpdateChannelRequestTestData.addMembersRequest)
        Assertions.assertEquals(UpdateChannelRequestTestData.addMembersJson, json)
    }

    @Test
    fun `Serialize UpdateChannelRequest for updateChannel with channel data`() {
        val json = parser.toJson(UpdateChannelRequestTestData.updateChannelRequest)
        Assertions.assertEquals(UpdateChannelRequestTestData.updateChannelJson, json)
    }

    @Test
    fun `Serialize UpdateChannelRequest for acceptInvite`() {
        val json = parser.toJson(UpdateChannelRequestTestData.acceptInviteRequest)
        Assertions.assertEquals(UpdateChannelRequestTestData.acceptInviteJson, json)
    }

    @Test
    fun `Serialize UpdateChannelRequest for removeMembers`() {
        val json = parser.toJson(UpdateChannelRequestTestData.removeMembersRequest)
        Assertions.assertEquals(UpdateChannelRequestTestData.removeMembersJson, json)
    }

    @Test
    fun `Serialize UpdateChannelRequest for inviteMembers`() {
        val json = parser.toJson(UpdateChannelRequestTestData.inviteMembersRequest)
        Assertions.assertEquals(UpdateChannelRequestTestData.inviteMembersJson, json)
    }
}
