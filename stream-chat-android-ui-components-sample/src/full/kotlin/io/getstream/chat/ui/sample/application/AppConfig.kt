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

package io.getstream.chat.ui.sample.application

import io.getstream.chat.ui.sample.data.user.SampleUser

object AppConfig {
    const val apiKey: String = "qk4nn7rpcn75"
    const val apiUrl: String = "chat-us-east-staging.stream-io-api.com"
    const val apiTimeout: Int = 6000
    const val cndTimeout: Int = 30000

    private val user1 = SampleUser(
        apiKey = apiKey,
        id = "80c26629-bc25-4ee5-a8ae-4824f8097b53",
        name = "paul",
        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiODBjMjY2MjktYmMyNS00ZWU1LWE4YWUtNDgyNGY4MDk3YjUzIn0.ca55fO4jAbexSSZKqBMT0OaxCZaPtYmo1HWgvjBnOY4",
        image = "https://getstream.io/random_png?id=80c26629-bc25-4ee5-a8ae-4824f8097b53&name=paul&size=200",
    )
    private val user2 = SampleUser(
        apiKey = apiKey,
        id = "a97a21dd-d993-42e0-8e52-31d6b3cea82c",
        name = "chani",
        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYTk3YTIxZGQtZDk5My00MmUwLThlNTItMzFkNmIzY2VhODJjIn0.7Hv38I8xq328_nyKdMA8m1ehz3jrNdnaH1GzSj6yuzk",
        image = "https://getstream.io/random_png?id=a97a21dd-d993-42e0-8e52-31d6b3cea82c&name=chani&size=200",
    )
    private val user3 = SampleUser(
        apiKey = apiKey,
        id = "d39f2878-2e97-49bd-9148-6e3fd85bbce5",
        name = "duncan",
        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZDM5ZjI4NzgtMmU5Ny00OWJkLTkxNDgtNmUzZmQ4NWJiY2U1In0.Do_LACAzy5pxApqJx0kpztMm_vXNRLkAVE79LEafXZg",
        image = "https://getstream.io/random_png?id=d39f2878-2e97-49bd-9148-6e3fd85bbce5&name=duncan&size=200",
    )
    private val user4 = SampleUser(
        apiKey = apiKey,
        id = "3761c83e-2a2e-4d6f-94ff-69c452386f8a",
        name = "leto",
        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMzc2MWM4M2UtMmEyZS00ZDZmLTk0ZmYtNjljNDUyMzg2ZjhhIn0.SrnLIH0bthY0yc-QwbsImI6mXTmQTz9-a7_95MPFwsA",
        image = "https://getstream.io/random_png?id=3761c83e-2a2e-4d6f-94ff-69c452386f8a&name=leto&size=200",
    )
    val availableUsers: List<SampleUser> = listOf(user1, user2, user3, user4)
}
