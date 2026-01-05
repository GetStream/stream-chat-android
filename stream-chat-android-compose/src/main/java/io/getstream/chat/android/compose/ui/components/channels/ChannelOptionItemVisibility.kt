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

package io.getstream.chat.android.compose.ui.components.channels

/**
 * Controls option visibility in the channel options menu.
 *
 * @param isViewInfoVisible Visibility of the view channel info option.
 * @param isLeaveChannelVisible Visibility of the leave channel option.
 * @param isMuteChannelVisible Visibility of the mute channel option.
 * @param isArchiveChannelVisible Visibility of the archive channel option.
 * @param isPinChannelVisible Visibility of the pin channel option.
 * @param isDeleteChannelVisible Visibility of the delete channel option.
 *
 * @see [ChannelOptions]
 */
public data class ChannelOptionItemVisibility(
    val isViewInfoVisible: Boolean = true,
    val isLeaveChannelVisible: Boolean = true,
    val isMuteChannelVisible: Boolean = true,
    val isArchiveChannelVisible: Boolean = false,
    val isPinChannelVisible: Boolean = false,
    val isDeleteChannelVisible: Boolean = true,
)
