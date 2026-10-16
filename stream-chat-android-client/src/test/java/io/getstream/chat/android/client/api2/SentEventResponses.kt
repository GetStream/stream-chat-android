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

/** What the v2 send-event endpoint returns for a typing event: the event as the v2 socket encodes it. */
internal const val SENT_TYPING_EVENT = """{
  "event": {
    "type": "typing.start", "created_at": 1791976405614650759, "cid": "messaging:general",
    "channel_type": "messaging", "channel_id": "general", "custom": {},
    "user": {
      "id": "luke", "role": "user", "language": "en", "banned": false, "online": true,
      "created_at": 1791976405614650759, "updated_at": 1791976405614650759, "custom": {}
    }
  },
  "duration": "1.20ms"
}"""

/** What the v2 send-event endpoint returns for an app's custom event: its fields nested under `custom`. */
internal const val SENT_CUSTOM_EVENT = """{
  "event": {
    "type": "friendship_request", "created_at": 1791976405614650759, "cid": "messaging:general",
    "channel_type": "messaging", "channel_id": "general", "custom": {"text": "hi"}
  },
  "duration": "1.20ms"
}"""

/**
 * What the v2 send-event endpoint returns for an AI indicator update: the event as sent, so the backend's fields are
 * still under `custom` (the socket's copy has them at the root).
 */
internal const val SENT_AI_INDICATOR_EVENT = """{
  "event": {
    "type": "ai_indicator.update", "created_at": 1791976405614650759, "cid": "messaging:general",
    "channel_type": "messaging", "channel_id": "general",
    "custom": {"ai_state": "AI_STATE_THINKING", "message_id": "m1", "probe": "kept"},
    "user": {
      "id": "luke", "role": "user", "language": "en", "banned": false, "online": true,
      "created_at": 1791976405614650759, "updated_at": 1791976405614650759, "custom": {}
    }
  },
  "duration": "1.20ms"
}"""
