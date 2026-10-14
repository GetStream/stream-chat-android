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

import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.api2.model.requests.UpdateUsersRequest
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.User
import io.kotest.assertions.json.shouldEqualJson
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class UpstreamUserDtoAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize JSON user with custom fields`() {
        val jsonString = parser.toJson(UserDtoTestData.upstreamUser)
        jsonString.shouldEqualJson(UserDtoTestData.upstreamJson)
    }

    @Test
    fun `Serialize JSON user without custom fields`() {
        val jsonString = parser.toJson(UserDtoTestData.upstreamUserWithoutExtraData)
        jsonString.shouldEqualJson(UserDtoTestData.upstreamJsonWithoutExtraData)
    }

    @Test
    fun `A user upsert leaves out the user's devices`() {
        val user = User(
            id = "userId",
            role = "user",
            name = "username",
            image = "image",
            language = "en",
            teams = listOf("team1"),
            devices = listOf(Device(token = "token", pushProvider = PushProvider.FIREBASE, providerName = "firebase")),
            extraData = mapOf("flair" to "gold", "devices" to listOf("stored")),
        )
        val mapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer)

        val json = parser.toJson(UpdateUsersRequest(mapOf(user.id to with(mapping) { user.toDto() })))

        json.shouldEqualJson(
            """{"users":{"userId":{"banned":false,"id":"userId","name":"username","image":"image","invisible":false,
            "language":"en","role":"user","teams":["team1"],"teams_role":{},"flair":"gold"}}}""",
        )
    }

    @Test
    fun `Can't parse upstream user`() {
        invoking {
            parser.fromJson(UserDtoTestData.upstreamJson, UpstreamUserDto::class.java)
        }.shouldThrow(RuntimeException::class)
    }
}
