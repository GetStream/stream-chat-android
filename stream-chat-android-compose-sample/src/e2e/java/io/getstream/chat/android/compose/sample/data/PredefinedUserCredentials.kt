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

package io.getstream.chat.android.compose.sample.data

import io.getstream.chat.android.models.User

/**
 * Contains hardcoded [UserCredentials] for the demo environment.
 */
object PredefinedUserCredentials {

    const val API_KEY: String = "8br4watad788"

    val availableUsers: List<UserCredentials> = listOf(
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                "luke_skywalker",
                name = "Luke Skywalker",
                image = "https://vignette.wikia.nocookie.net/starwars/images/2/20/LukeTLJ.jpg",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibHVrZV9za3l3YWxrZXIifQ.kFSLHRB5X62t0Zlc7nwczWUfsQMwfkpylC6jCUZ6Mc0",
        ),
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                "leia_organa",
                name = "Leia Organa",
                image = "https://vignette.wikia.nocookie.net/starwars/images/f/fc/Leia_Organa_TLJ.png",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibGVpYV9vcmdhbmEifQ.IzwBuaYwX5dRvnDDnJN2AyW3wwfYwgQm3w-1RD4BLPU",
        ),
    )
}
