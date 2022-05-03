/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.uitests.util

import io.getstream.chat.android.client.models.User

/**
 * Provides sample users for UI tests.
 */
object TestUsers {

    val user1: User = User().apply {
        id = "jc1"
        name = "Jc Miñarro"
        image = FakeImageLoader.AVATAR_JC
        online = true
    }

    val user2: User = User().apply {
        id = "amit"
        name = "Amit Kumar"
        image = FakeImageLoader.AVATAR_AMIT
    }

    val user3: User = User().apply {
        id = "filip"
        name = "Filip Babić"
        image = FakeImageLoader.AVATAR_FILIP
    }
}
