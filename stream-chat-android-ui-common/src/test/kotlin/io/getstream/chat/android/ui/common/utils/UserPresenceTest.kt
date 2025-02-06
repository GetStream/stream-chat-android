/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.utils

import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.model.UserPresence
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowOnlineIndicator
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class UserPresenceTest {

    @ParameterizedTest
    @MethodSource("showOnlineIndicatorTestCases")
    fun shouldShowOnlineIndicator(testCase: TestCase) {
        val user = User(id = testCase.userId)
        val currentUser = testCase.currentUserId?.let(::User)

        val actual = user.shouldShowOnlineIndicator(
            userPresence = testCase.userPresence,
            currentUser = currentUser,
        )

        assertEquals(expected = testCase.expected, actual = actual)
    }

    @Suppress("LongMethod", "UnusedPrivateMember")
    private fun showOnlineIndicatorTestCases() = listOf(
        TestCase(
            userPresence = UserPresence(
                currentUser = UserPresence.DisplayOptions(showOnlineIndicator = true),
            ),
            currentUserId = "A",
            userId = "A",
            expected = true,
        ),
        TestCase(
            userPresence = UserPresence(
                currentUser = UserPresence.DisplayOptions(showOnlineIndicator = false),
            ),
            currentUserId = "A",
            userId = "A",
            expected = false,
        ),
        TestCase(
            userPresence = UserPresence(
                otherUsers = UserPresence.DisplayOptions(showOnlineIndicator = true),
            ),
            currentUserId = "A",
            userId = "B",
            expected = true,
        ),
        TestCase(
            userPresence = UserPresence(
                otherUsers = UserPresence.DisplayOptions(showOnlineIndicator = false),
            ),
            currentUserId = "A",
            userId = "B",
            expected = false,
        ),
        TestCase(
            userPresence = UserPresence(
                currentUser = UserPresence.DisplayOptions(showOnlineIndicator = true),
            ),
            currentUserId = null,
            userId = "B",
            expected = false,
        ),
        TestCase(
            userPresence = UserPresence(
                currentUser = UserPresence.DisplayOptions(showOnlineIndicator = true),
            ),
            currentUserId = null,
            userId = "B",
            expected = false,
        ),
        TestCase(
            userPresence = UserPresence(
                otherUsers = UserPresence.DisplayOptions(showOnlineIndicator = true),
            ),
            currentUserId = null,
            userId = "B",
            expected = true,
        ),
        TestCase(
            userPresence = UserPresence(
                otherUsers = UserPresence.DisplayOptions(showOnlineIndicator = false),
            ),
            currentUserId = null,
            userId = "B",
            expected = false,
        ),
    )

    data class TestCase(
        val userId: String,
        val currentUserId: String?,
        val userPresence: UserPresence,
        val expected: Boolean,
    )
}
