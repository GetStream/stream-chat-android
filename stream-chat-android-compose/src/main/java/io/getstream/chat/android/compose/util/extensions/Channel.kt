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

package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities

/**
 * Returns if the channel has polls enabled or not for the current user based on the channel config and capabilities.
 *
 * @return True if the channel has polls enabled, false otherwise.
 */
internal fun Channel.isPollEnabled(): Boolean =
    this.config.pollsEnabled && ownCapabilities.contains(ChannelCapabilities.SEND_POLL)
