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

package io.getstream.chat.android.network.infrastructure

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.UpdateMemberPartialRequest
import io.getstream.chat.android.network.models.UpdateMemberPartialResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * The member partial update parses its recorded v2 response through the generated [Serializer] alone, and sends the
 * same request body as on v1: root keys of `set` are the member's custom data on both.
 */
internal class V2LaneBatchElevenTest {

    private val mapping = DomainMapping(
        currentUserIdProvider = { "filip" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
        hidesStoredDeclaredCustom = true,
    )

    @Test
    fun `A member update parses the member's custom data and integer dates from v2`() {
        val response = Serializer.moshi.adapter(UpdateMemberPartialResponse::class.java).fromJson(RESPONSE)!!

        val member = with(mapping) { response.channelMember!!.toDomain() }

        assertEquals("filip", member.getUserId())
        assertEquals("r", member.extraData["pum_root"])
        assertEquals(Date(1791970290271), member.pinnedAt)
    }

    @Test
    fun `A member update keeps its v1 body`() {
        val request = UpdateMemberPartialRequest(set = mapOf("pum_root" to "r", "pinned" to true), unset = listOf("q"))

        val v2Body = Serializer.moshi.adapter(UpdateMemberPartialRequest::class.java).toJson(request)

        assertEquals("""{"unset":["q"],"set":{"pum_root":"r","pinned":true}}""", v2Body)
    }

    private companion object {
        /** The recorded response, with the user's optional fields left out. */
        private const val RESPONSE = """{"channel_member":{"user_id":"filip","user":{"id":"filip","name":"Chewbacca",
            "custom":{},"language":"","role":"user","teams":[],"created_at":1634853490403855000,
            "updated_at":1791791284528654000,"banned":false,"online":true,"blocked_user_ids":[]},
            "custom":{"pum_root":"r"},"status":"member","created_at":1791970280920927000,
            "updated_at":1791970290271474000,"banned":false,"shadow_banned":false,"pinned_at":1791970290271473000,
            "channel_role":"channel_member","notifications_muted":false},"duration":"10.76ms"}"""
    }
}
