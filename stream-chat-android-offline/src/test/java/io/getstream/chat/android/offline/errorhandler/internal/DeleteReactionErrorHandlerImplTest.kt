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

package io.getstream.chat.android.offline.errorhandler.internal

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class DeleteReactionErrorHandlerImplTest {

    @Test
    fun `when passing null as cid, no crash should happen`() {
        // We would like to check that no exceptions happens, so there's no need to assert anything.
        DeleteReactionErrorHandlerImpl(mock(), mock(), mock())
            .onDeleteReactionError(
                TestCall(Result(randomMessage())),
                null,
                randomString()
            )
    }
}
