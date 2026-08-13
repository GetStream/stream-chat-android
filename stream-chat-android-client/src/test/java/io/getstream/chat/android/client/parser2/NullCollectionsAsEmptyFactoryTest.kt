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

import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.network.models.ListUserGroupsResponse
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class NullCollectionsAsEmptyFactoryTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Explicit null is read as an empty collection`() {
        val response = parser.fromJson(
            """{"duration": "1ms", "user_groups": null}""",
            ListUserGroupsResponse::class.java,
        )

        response.userGroups.shouldBeEmpty()
    }

    @Test
    fun `Absent collection still falls back to the model default`() {
        val response = parser.fromJson("""{"duration": "1ms"}""", ListUserGroupsResponse::class.java)

        response.userGroups.shouldBeEmpty()
    }

    @Test
    fun `Populated collection is read unchanged`() {
        val response = parser.fromJson(
            """
            {
                "duration": "1ms",
                "user_groups": [
                    {
                        "id": "group-1",
                        "name": "Group One",
                        "created_at": "2020-06-10T11:04:31.000Z",
                        "updated_at": "2020-06-10T11:04:31.000Z"
                    }
                ]
            }
            """.trimIndent(),
            ListUserGroupsResponse::class.java,
        )

        response.userGroups shouldHaveSize 1
        response.userGroups.first().id shouldBeEqualTo "group-1"
    }

    @Test
    fun `Hand-written DTOs are left alone`() {
        invoking {
            parser.fromJson(
                """
                {
                    "id": "jc",
                    "role": "user",
                    "created_at": "2020-06-10T11:04:31.000Z",
                    "updated_at": "2020-06-10T11:04:31.000Z",
                    "banned": false,
                    "online": true,
                    "teams": null
                }
                """.trimIndent(),
                DownstreamUserDto::class.java,
            )
        }.shouldThrow(Exception::class)
    }
}
