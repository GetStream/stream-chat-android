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

package io.getstream.chat.android.client.parser2.direct

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.EVENT_USER_HIDDEN_CUSTOM_KEYS
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.User
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.UserResponseCommonFields
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date

/** The direct [UserAdapter] reads a v2 user into the same [User] as the generated v2 path. */
internal class V2UserAdapterParityTest {

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "me" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
        hidesStoredDeclaredCustom = true,
    )
    private val dateAdapter = Moshi.Builder().add(IsoDateAdapter()).build().adapter(Date::class.java)
    private val direct = UserAdapter(dateAdapter, NoOpUserTransformer, domainMapping, EVENT_USER_HIDDEN_CUSTOM_KEYS)
    private val generated = Serializer.moshi.adapter(UserResponseCommonFields::class.java)

    private fun viaGenerated(json: String): User =
        with(domainMapping) { requireNotNull(generated.fromJson(json)).toDomain() }

    @ParameterizedTest
    @MethodSource("users")
    fun `A v2 user parses to the same User on both paths`(json: String) {
        direct.fromJson(json) shouldBeEqualTo viaGenerated(json)
    }

    @ParameterizedTest
    @MethodSource("incompleteUsers")
    fun `A v2 user without a required field fails on both paths`(json: String) {
        assertThrows<JsonDataException> { viaGenerated(json) }
        assertThrows<JsonDataException> { direct.fromJson(json) }
    }

    private companion object {
        private const val REQUIRED = """"id": "leia", "role": "user", "banned": false, "online": true,
            "language": "en", "created_at": 1791976405614650759, "updated_at": 1791976405614650759"""

        @JvmStatic
        fun users() = listOf(
            "{$REQUIRED}",
            """{$REQUIRED, "name": "Leia", "image": "https://example.com/leia.png", "teams": ["blue"],
                "teams_role": {"blue": "admin"}, "blocked_user_ids": ["vader"], "avg_response_time": 42,
                "last_active": 1791976405614650759, "deactivated_at": null}""",
            """{$REQUIRED, "custom": {"flair": "gold", "score": 7, "nested": {"a": [1, 2]}, "empty": null}}""",
            // Push devices stored as custom data are dropped.
            """{$REQUIRED, "custom": {"devices": [{"id": "token", "push_provider": "firebase"}], "flair": "gold"}}""",
            // A custom value stored under a declared name is hidden, as v1 hides it.
            """{$REQUIRED, "custom": {"name": "stored", "banned": true, "flair": "gold"}}""",
            // Declared fields v1 also kept in custom data come back to extraData.
            """{$REQUIRED, "deleted_at": 1791976405614650759, "revoke_tokens_issued_before": 1791976405614650759}""",
            // An unknown root field is ignored, as the generated model ignores it.
            """{$REQUIRED, "unknown": "x"}""",
        )

        @JvmStatic
        fun incompleteUsers() = listOf("created_at", "updated_at", "language", "id").map { field ->
            "{${REQUIRED.replace(Regex(""""$field": [^,]+,?"""), "")}}".replace(", }", "}")
        }
    }
}
