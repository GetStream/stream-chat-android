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

package io.getstream.chat.android.models

/**
 * A grouped channels response returned by [ChatClient.groupedQueryChannels].
 *
 * @param family The grouped channel family configured for the current app.
 * @param buckets The grouped channel buckets returned by the backend in response order.
 */
public data class GroupedChannels(
    public val family: String,
    public val buckets: List<GroupedChannelsBucket>,
)

/**
 * A grouped channels bucket returned by [io.getstream.chat.android.client.ChatClient.groupedQueryChannels].
 *
 * @param key The backend-defined key for this bucket within the family.
 * @param channels The channels that belong to this bucket.
 * @param unreadCount The total unread message count across the bucket.
 * @param unreadChannels The total unread channel count in the bucket.
 */
public data class GroupedChannelsBucket(
    public val key: String,
    public val channels: List<Channel>,
    public val unreadCount: Int,
    public val unreadChannels: Int,
)
