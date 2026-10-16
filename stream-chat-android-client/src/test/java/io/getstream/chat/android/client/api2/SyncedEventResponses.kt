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

package io.getstream.chat.android.client.api2

/**
 * What the v2 sync endpoint returns: an app's custom event, its fields nested under `custom`, and a Feeds event the
 * SDK ignores, each as the v2 socket encodes it.
 */
internal const val SYNCED_EVENTS = """{
  "events": [
    {
      "type": "friendship_request", "created_at": 1791976405614650759, "cid": "messaging:general",
      "channel_type": "messaging", "channel_id": "general", "custom": {"text": "hi"}
    },
    {"type": "feeds.activity.added", "created_at": 1791976405614650760, "fid": "user:luke"}
  ],
  "duration": "1.20ms"
}"""

/** A v2 sync response replaying a `channel.hidden` without the channel it requires. */
internal const val SYNCED_UNREADABLE_EVENT = """{
  "events": [
    {
      "type": "channel.hidden", "created_at": 1791976405614650759, "cid": "messaging:general",
      "channel_type": "messaging", "channel_id": "general", "custom": {}
    }
  ],
  "duration": "1.20ms"
}"""
