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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.User
import io.getstream.chat.android.network.models.UserResponse
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.reflect.Modifier

/**
 * Parses the same user JSON through the hand-written [DownstreamUserDto] and the generated [UserResponse], and
 * requires the same [User]. The fixtures have the shape a client-side connection receives for other users
 * (channel watchers, created_by): common fields only, since the Go payload drops its client-side-only fields.
 */
internal class UserResponseParityTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "jaewoong" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    fun `The generated UserResponse maps to the same User as the hand-written DTO`(name: String, json: String) {
        val legacy = with(mapping) { parser.fromJson(json, DownstreamUserDto::class.java).toDomain() }
        val generated = with(mapping) { parser.fromJson(json, UserResponse::class.java).toDomain() }
        val differences = User::class.java.declaredFields
            .filterNot { Modifier.isStatic(it.modifiers) }
            .onEach { it.isAccessible = true }
            .mapNotNull { f ->
                val a = f.get(legacy)
                val b = f.get(generated)
                if (a == b) null else "  ${f.name}:\n    legacy    = $a\n    generated = $b"
            }
        if (differences.isNotEmpty()) fail<Unit>("$name differs:\n" + differences.joinToString("\n"))
    }

    companion object {

        private const val COMMON = """
            "id": "oleg", "role": "user", "language": "it", "banned": false, "online": true,
            "created_at": "2021-10-21T21:58:10.000Z", "updated_at": "2026-09-13T11:30:11.000Z"
        """

        @JvmStatic
        fun fixtures() = listOf(
            Arguments.of("required fields only", "{ $COMMON }"),
            Arguments.of(
                "optional fields and custom data",
                """
                {
                  $COMMON,
                  "name": "R2-D2", "image": "https://example.com/r2.png",
                  "last_active": "2026-09-24T13:30:16.000Z", "deactivated_at": "2026-09-01T00:00:00.000Z",
                  "teams": ["blue", "red"], "teams_role": {"blue": "admin"},
                  "blocked_user_ids": ["luke"], "avg_response_time": 1207290,
                  "deleted_at": "2026-09-02T00:00:00.000Z",
                  "revoke_tokens_issued_before": "2026-09-03T00:00:00.000Z",
                  "flair": "gold", "score": 7, "profile": {"bio": "beep"}
                }
                """.trimIndent(),
            ),
        )
    }
}
