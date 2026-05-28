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

import io.getstream.chat.android.models.Location
import org.intellij.lang.annotations.Language
import java.util.Date

internal object LocationTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"channel_cid":"messaging:123","message_id":"msg-1","user_id":"user-1","latitude":37.7749,"longitude":-122.4194,"created_by_device_id":"device-1","end_at":"2025-04-08T12:00:00.000Z"}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{"channel_cid":"messaging:123","message_id":"msg-1","user_id":"user-1","latitude":37.7749,"longitude":-122.4194,"created_by_device_id":"device-1"}"""

    @Language("JSON")
    val jsonMissingChannelCid =
        """{"message_id":"msg-1","user_id":"user-1","latitude":37.7749,"longitude":-122.4194,"created_by_device_id":"device-1","end_at":"2025-04-08T12:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingMessageId =
        """{"channel_cid":"messaging:123","user_id":"user-1","latitude":37.7749,"longitude":-122.4194,"created_by_device_id":"device-1","end_at":"2025-04-08T12:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingUserId =
        """{"channel_cid":"messaging:123","message_id":"msg-1","latitude":37.7749,"longitude":-122.4194,"created_by_device_id":"device-1","end_at":"2025-04-08T12:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingLatitude =
        """{"channel_cid":"messaging:123","message_id":"msg-1","user_id":"user-1","longitude":-122.4194,"created_by_device_id":"device-1","end_at":"2025-04-08T12:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingLongitude =
        """{"channel_cid":"messaging:123","message_id":"msg-1","user_id":"user-1","latitude":37.7749,"created_by_device_id":"device-1","end_at":"2025-04-08T12:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingCreatedByDeviceId =
        """{"channel_cid":"messaging:123","message_id":"msg-1","user_id":"user-1","latitude":37.7749,"longitude":-122.4194,"end_at":"2025-04-08T12:00:00.000Z"}"""

    val expectedAllFields = Location(
        cid = "messaging:123",
        messageId = "msg-1",
        userId = "user-1",
        latitude = 37.7749,
        longitude = -122.4194,
        deviceId = "device-1",
        endAt = Date(1744113600000L),
    )

    val expectedOptionalFieldsMissing = Location(
        cid = "messaging:123",
        messageId = "msg-1",
        userId = "user-1",
        latitude = 37.7749,
        longitude = -122.4194,
        deviceId = "device-1",
        endAt = null,
    )
}
