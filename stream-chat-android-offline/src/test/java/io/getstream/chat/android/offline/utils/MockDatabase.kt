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

package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal fun mockDb() = mock<ChatDatabase> {
    on { userDao() } doReturn mock()
    on { channelConfigDao() } doReturn mock()
    on { channelStateDao() } doReturn mock()
    on { queryChannelsDao() } doReturn mock()
    on { messageDao() } doReturn mock()
    on { reactionDao() } doReturn mock()
    on { syncStateDao() } doReturn mock()
    on { attachmentDao() } doReturn mock()
}
