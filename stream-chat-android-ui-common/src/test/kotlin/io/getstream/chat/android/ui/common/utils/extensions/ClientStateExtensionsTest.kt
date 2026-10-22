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

package io.getstream.chat.android.ui.common.utils.extensions

import app.cash.turbine.test
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ClientStateExtensionsTest {

    private val user = MutableStateFlow<User?>(null)
    private val clientState: ClientState = mock { on { it.user } doReturn user }

    @Test
    fun `userSessionEnds emits on disconnect and on a switch to a different user`() = runTest {
        clientState.userSessionEnds().test {
            user.value = User(id = "a")
            user.value = User(id = "a", name = "renamed")
            expectNoEvents()

            user.value = null
            awaitItem()

            user.value = User(id = "a")
            expectNoEvents()

            user.value = User(id = "b")
            awaitItem()
        }
    }
}
