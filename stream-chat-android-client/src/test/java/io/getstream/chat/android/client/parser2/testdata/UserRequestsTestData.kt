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

import io.getstream.chat.android.network.models.CreateGuestRequest
import io.getstream.chat.android.network.models.UpdateUserPartialRequest
import io.getstream.chat.android.network.models.UpdateUsersPartialRequest
import io.getstream.chat.android.network.models.UserRequest
import org.intellij.lang.annotations.Language

internal object UserRequestsTestData {

    // UpdateUsersPartialRequest

    @Language("JSON")
    val updateUsersPartialRequestJson =
        """{
          "users": [
            {
              "id": "user1",
              "unset": [
                "age"
              ],
              "set": {
                "nickname": "neo"
              }
            }
          ]
        }""".withoutWhitespace()

    val updateUsersPartialRequest = UpdateUsersPartialRequest(
        users = listOf(
            UpdateUserPartialRequest(
                id = "user1",
                unset = listOf("age"),
                set = mapOf("nickname" to "neo"),
            ),
        ),
    )

    // CreateGuestRequest (custom flattened onto the nested user object)

    @Language("JSON")
    val createGuestRequestJson =
        """{
          "user": {
            "id": "user1",
            "name": "Neo",
            "customKey": "customValue"
          }
        }""".withoutWhitespace()

    val createGuestRequest = CreateGuestRequest(
        user = UserRequest(
            id = "user1",
            name = "Neo",
            custom = mapOf("customKey" to "customValue"),
        ),
    )

    @Language("JSON")
    val createGuestRequestJsonWithoutCustom =
        """{
          "user": {
            "id": "user1",
            "name": "Neo"
          }
        }""".withoutWhitespace()

    val createGuestRequestWithoutCustom = CreateGuestRequest(
        user = UserRequest(id = "user1", name = "Neo"),
    )
}
