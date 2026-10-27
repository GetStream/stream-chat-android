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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.network.models.UserResponse
import org.intellij.lang.annotations.Language
import java.util.Date

internal object UserDtoTestData {

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
            "id": "",
            "role": "",
            "name": "username",
            "image": "image",
            "invisible": false,
            "language": "language",
            "banned": false,
            "devices": [],
            "online": false,
            "created_at": null,
            "deactivated_at": null,
            "updated_at": null,
            "last_active": null,
            "total_unread_count": 0,
            "unread_channels": 0,
            "unread_count": 0,
            "mutes": [],
            "teams": [],
            "channel_mutes": []
         }"""

    @Language("JSON")
    val downstreamJson =
        """{
            "id": "userId",
            "role": "owner",
            "invisible": false,
            "privacy_settings": {
              "typing_indicators": {
                "enabled": false
              },
              "read_receipts": {
                "enabled": false
              },
              "delivery_receipts": {
                "enabled": false
              }
            },
            "language": "language",
            "banned": false,
            "devices": [
             {
              "id": "deviceId",
              "push_provider": "provider",
              "push_provider_name": "provider_name",
              "created_at": "2020-06-10T11:04:31.000Z",
              "user_id": "userId"
             }
            ],
            "online": true,
            "created_at": "2020-06-10T11:04:31.000Z",
            "updated_at": "2020-06-10T11:04:31.588Z",
            "deactivated_at": "2020-06-10T11:04:31.588Z",
            "last_active": "2020-06-10T11:04:31.588Z",
            "total_unread_count": 1,
            "unread_channels": 2,
            "unread_count": 3,
            "mutes": [
            {
             "user": $userResponseJson,
             "target": $userResponseJson,
             "created_at": "2020-06-10T11:04:31.000Z",
             "updated_at": "2020-06-10T11:04:31.588Z"
            }
            ],
            "teams": [ "team1", "team2" ],
            "teams_role": {
                "team1": "owner",
                "team2": "member"
            },
            "channel_mutes": [],
            "name": "username",
            "image": "image",
            "avg_response_time": 1000,
            "push_preferences": {
                "chat_level": "default",
                "disabled_until": "2020-06-10T11:04:31.588Z",
                "chat_preferences": {
                  "direct_mentions": "all",
                  "role_mentions": "none",
                  "group_mentions": "all",
                  "here_mentions": "none",
                  "channel_mentions": "all",
                  "thread_replies": "none",
                  "default_preference": "all"
                }
            }
         }"""
    const val userResponseJson =
        """{
            "id": "userId",
            "role": "owner",
            "language": "language",
            "banned": false,
            "online": true,
            "created_at": "2020-06-10T11:04:31.588Z",
            "updated_at": "2020-06-10T11:04:31.588Z",
            "name": "username",
            "image": "image"
        }"""

    val userResponse =
        UserResponse(
            id = "userId",
            role = "owner",
            language = "language",
            banned = false,
            online = true,
            createdAt = Date(1591787071588),
            updatedAt = Date(1591787071588),
            name = "username",
            image = "image",
        )
}
