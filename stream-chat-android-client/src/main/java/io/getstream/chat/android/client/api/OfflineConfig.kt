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

package io.getstream.chat.android.client.api

/**
 * Configuration class for offline capabilities in the Stream Chat Android client.
 *
 * @param enabled Flag to enable or disable offline support. Default is `true`.
 * @param ignoredChannelTypes Set of channel types to be ignored by offline support. Default is
 *   an empty set.
 */
public data class OfflineConfig(
    public val enabled: Boolean = true,
    public val ignoredChannelTypes: Set<String> = emptySet(),
)
