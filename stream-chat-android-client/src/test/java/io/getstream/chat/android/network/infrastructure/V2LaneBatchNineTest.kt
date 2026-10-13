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
import io.getstream.chat.android.network.models.BanRequest
import io.getstream.chat.android.network.models.ModerationBanResponse
import io.getstream.chat.android.network.models.MuteRequest
import io.getstream.chat.android.network.models.MuteResponse
import io.getstream.chat.android.network.models.UnbanResponse
import io.getstream.chat.android.network.models.UnmuteRequest
import io.getstream.chat.android.network.models.UnmuteResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * User mutes and channel bans parse their recorded v2 moderation responses through the generated [Serializer] alone,
 * and send the v2 request bodies.
 */
internal class V2LaneBatchNineTest {

    private val mapping = DomainMapping(
        currentUserIdProvider = { "filip" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
        hidesStoredDeclaredCustom = true,
    )

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private inline fun <reified T> v2Body(request: T): String = Serializer.moshi.adapter(T::class.java).toJson(request)

    @Test
    fun `A user mute parses its users and integer dates from v2`() {
        val mute = with(mapping) { parse<MuteResponse>(MUTE).mutes!!.single().toDomain() }

        assertEquals("filip", mute.user?.id)
        assertEquals("jaewoong", mute.target?.id)
        assertEquals(mapOf("mute_probe" to "nested"), mute.target?.extraData)
        assertEquals(Date(1791877045679), mute.createdAt)
        assertEquals(Date(1791877045679), mute.updatedAt)
        assertEquals(Date(1791877345679), mute.expires)
    }

    @Test
    fun `An unmute, a ban and an unban parse from v2`() {
        assertEquals("26.13ms", parse<UnmuteResponse>(UNMUTE).duration)
        assertEquals("24.82ms", parse<ModerationBanResponse>(BAN).duration)
        assertEquals("14.76ms", parse<UnbanResponse>(UNBAN).duration)
    }

    @Test
    fun `A mute and an unmute send their target as a list on v2`() {
        assertEquals("""{"target_ids":["jaewoong"],"timeout":5}""", v2Body(MuteRequest(listOf("jaewoong"), 5)))
        assertEquals("""{"target_ids":["jaewoong"]}""", v2Body(UnmuteRequest(listOf("jaewoong"))))
    }

    @Test
    fun `A ban sends its channel as a cid on v2, which keeps it scoped to the channel`() {
        val request = BanRequest(
            targetUserId = "jaewoong",
            channelCid = "messaging:general",
            reason = "spam",
            shadow = true,
            timeout = 5,
        )

        assertEquals(
            """{"target_user_id":"jaewoong","channel_cid":"messaging:general","reason":"spam","shadow":true,""" +
                """"timeout":5}""",
            v2Body(request),
        )
    }

    private companion object {
        private fun user(id: String, custom: String) = """{"id":"$id","custom":$custom,"language":"","role":"user",
            "teams":[],"created_at":1634853490403855000,"updated_at":1791810499684936000,"banned":false,
            "online":false,"blocked_user_ids":[]}"""

        /** The recorded response also carries the current user's own fields, left out here. */
        private val MUTE = """{"mutes":[{"user":${user("filip", "{}")},
            "target":${user("jaewoong", """{"mute_probe":"nested"}""")},"expires":1791877345679855000,
            "created_at":1791877045679858000,"updated_at":1791877045679858000}],"duration":"29.73ms"}"""

        private const val UNMUTE = """{"duration":"26.13ms"}"""

        private const val BAN = """{"duration":"24.82ms"}"""

        private const val UNBAN = """{"duration":"14.76ms"}"""
    }
}
