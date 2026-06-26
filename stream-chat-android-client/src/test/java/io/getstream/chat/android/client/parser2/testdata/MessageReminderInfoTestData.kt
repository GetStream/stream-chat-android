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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.models.MessageReminderInfo
import org.intellij.lang.annotations.Language
import java.util.Date

internal object MessageReminderInfoTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"channel_cid":"messaging:general","message_id":"msg-1","user_id":"user-1","remind_at":"2025-04-08T12:00:00.000Z","created_at":"2025-04-01T10:00:00.000Z","updated_at":"2025-04-07T15:30:00.000Z"}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{"channel_cid":"messaging:general","message_id":"msg-1","user_id":"user-1","created_at":"2025-04-01T10:00:00.000Z","updated_at":"2025-04-07T15:30:00.000Z"}"""

    @Language("JSON")
    val jsonMissingCreatedAt =
        """{"channel_cid":"messaging:general","message_id":"msg-1","user_id":"user-1","remind_at":"2025-04-08T12:00:00.000Z","updated_at":"2025-04-07T15:30:00.000Z"}"""

    @Language("JSON")
    val jsonMissingUpdatedAt =
        """{"channel_cid":"messaging:general","message_id":"msg-1","user_id":"user-1","remind_at":"2025-04-08T12:00:00.000Z","created_at":"2025-04-01T10:00:00.000Z"}"""

    val expectedAllFields = MessageReminderInfo(
        remindAt = Date(1744113600000L),
        createdAt = Date(1743501600000L),
        updatedAt = Date(1744039800000L),
    )

    val expectedOptionalFieldsMissing = MessageReminderInfo(
        remindAt = null,
        createdAt = Date(1743501600000L),
        updatedAt = Date(1744039800000L),
    )
}
