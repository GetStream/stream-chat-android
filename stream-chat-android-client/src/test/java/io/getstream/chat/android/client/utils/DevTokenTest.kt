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

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.helpers.QueryChannelsPostponeHelper
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.test.TestCoroutineExtension
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class DevTokenTest(private val userId: String, private val expectedToken: String) {

    private val socketStateService = SocketStateService()
    private val userStateService: UserStateService = UserStateService()
    private val queryChannelsPostponeHelper = QueryChannelsPostponeHelper(socketStateService, testCoroutines.scope)
    private val client = ChatClient(
        config = mock(),
        api = mock(),
        socket = mock(),
        notifications = mock(),
        tokenManager = FakeTokenManager(""),
        socketStateService = socketStateService,
        queryChannelsPostponeHelper = queryChannelsPostponeHelper,
        userCredentialStorage = mock(),
        userStateService = userStateService,
        scope = testCoroutines.scope,
        retryPolicy = NoRetryPolicy(),
        appSettingsManager = mock(),
    )

    @Test
    fun `Should return valid dev token`() {
        client.devToken(userId) `should be equal to` expectedToken
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{index}: {0} => {1}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(
                "jc",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
            ),
            arrayOf(
                "vishal",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidmlzaGFsIn0=.devtoken"
            ),
            arrayOf(
                "amin",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYW1pbiJ9.devtoken"
            )
        )
    }
}
