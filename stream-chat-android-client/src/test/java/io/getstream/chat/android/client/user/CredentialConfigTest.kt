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

package io.getstream.chat.android.client.user

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class CredentialConfigTest {

    @Test
    fun `isValid should return true when all fields are non-empty`() {
        val config = CredentialConfig(
            userId = "userId",
            userToken = "userToken",
            userName = "userName",
            isAnonymous = false,
        )
        config.isValid() shouldBeEqualTo true
    }

    @Test
    fun `isValid should return false when userId is empty`() {
        val config = CredentialConfig(
            userId = "",
            userToken = "userToken",
            userName = "userName",
            isAnonymous = false,
        )
        config.isValid() shouldBeEqualTo false
    }

    @Test
    fun `isValid should return false when userToken is empty`() {
        val config = CredentialConfig(
            userId = "userId",
            userToken = "",
            userName = "userName",
            isAnonymous = false,
        )
        config.isValid() shouldBeEqualTo false
    }

    @Test
    fun `isValid should return false when userName is empty`() {
        val config = CredentialConfig(
            userId = "userId",
            userToken = "userToken",
            userName = "",
            isAnonymous = false,
        )
        config.isValid() shouldBeEqualTo false
    }
}
