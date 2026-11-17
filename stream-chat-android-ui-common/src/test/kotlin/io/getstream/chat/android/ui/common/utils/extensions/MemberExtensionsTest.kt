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

import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.ExpandableList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MemberExtensionsTest {

    private val currentUserOnline = member(id = "me", online = true)
    private val otherOnline = member(id = "a", online = true)
    private val otherOffline = member(id = "b", online = false)
    private val members = listOf(currentUserOnline, otherOnline, otherOffline)

    @Test
    fun `countOnline counts every online member by default`() {
        assertEquals(2, members.countOnline(currentUserId = "me"))
    }

    @Test
    fun `countOnline skips the current user when asked to`() {
        assertEquals(1, members.countOnline(currentUserId = "me", countCurrentUser = false))
    }

    @Test
    fun `countOnline skips other users when asked to`() {
        assertEquals(1, members.countOnline(currentUserId = "me", countOthers = false))
    }

    @Test
    fun `onlineMemberCount includes collapsed members`() {
        val list = ExpandableList(
            items = listOf(otherOffline, otherOnline, currentUserOnline),
            minimumVisibleItems = 1,
            isCollapsed = true,
        )

        assertEquals(listOf(otherOffline), list)
        assertEquals(2, list.onlineMemberCount())
    }

    private fun member(id: String, online: Boolean) = Member(user = User(id = id, online = online))
}
