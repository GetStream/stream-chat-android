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

package io.getstream.chat.android.client.internal.state.internal

import io.getstream.chat.android.client.internal.state.errorhandler.internal.DeleteReactionErrorHandlerImpl
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock

internal class DeleteReactionErrorHandlerImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `when passing null as cid, no crash should happen`() {
        // We would like to check that no exceptions happens, so there's no need to assert anything.
        DeleteReactionErrorHandlerImpl(testCoroutines.scope, mock(), mock())
            .onDeleteReactionError(
                TestCall(Result.Success(randomMessage())),
                null,
                randomString(),
            )
    }
}
