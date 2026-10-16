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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.Json

/**
 * Sync request
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class SyncRequest(
    // Patched: the generator types this as java.util.Date, which keeps milliseconds, so the event at the cursor is
    // replayed on every sync. chat#18838 types it as ExactDate, as here; regen once it is merged.
    @Json(name = "last_sync_at")
    internal val lastSyncAt: io.getstream.chat.android.network.infrastructure.ExactDate,

    @Json(name = "channel_cids")
    internal val channelCids: List<String> = emptyList(),
)
