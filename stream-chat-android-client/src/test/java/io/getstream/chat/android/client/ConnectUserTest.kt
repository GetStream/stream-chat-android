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
 
package io.getstream.chat.android.client

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.models.User
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
internal class ConnectUserTest {

    val client = ChatClient(
        config = mock(),
        api = mock(),
        socket = mock(),
        notifications = mock(),
        tokenManager = mock(),
        socketStateService = mock(),
        queryChannelsPostponeHelper = mock(),
        userCredentialStorage = mock(),
        scope = mock(),
        retryPolicy = mock(),
        appSettingsManager = mock(),
    )

    @Test
    fun `Connect an user with a different userId than the one into the JWT should return an error`() {
        val user = User(id = "asdf")
        val jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"

        val result = client.connectUser(user, jwt).execute()

        result.isError `should be equal to` true
        result.error().message `should be equal to` "The user_id provided on the JWT token doesn't match with the current user you try to connect"
    }
}
