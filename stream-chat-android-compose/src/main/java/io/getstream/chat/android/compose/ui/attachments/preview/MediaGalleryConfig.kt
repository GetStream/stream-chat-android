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

package io.getstream.chat.android.compose.ui.attachments.preview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Model holding the configuration for the media gallery options.
 * By default, all options are enabled/visible.
 *
 * @param isCloseVisible If the close button is visible.
 * @param isOptionsVisible If the options button is visible.
 * @param isShareVisible If the share button is visible.
 * @param isGalleryVisible If the gallery button is visible.
 * @param optionsConfig The configuration for the options in the media gallery.
 */
@Parcelize
public data class MediaGalleryConfig(
    val isCloseVisible: Boolean = true,
    val isOptionsVisible: Boolean = true,
    val isShareVisible: Boolean = true,
    val isGalleryVisible: Boolean = true,
    val optionsConfig: MediaGalleryOptionsConfig = MediaGalleryOptionsConfig(),
) : Parcelable

/**
 * Model holding the configuration for the media gallery options.
 * By default, all options are enabled/visible.
 *
 * @param isShowInChatVisible If the "Show in chat" option is visible.
 * @param isReplyVisible If the "Reply" option is visible.
 * @param isSaveMediaVisible If the "Save media" option is visible.
 * @param isDeleteVisible If the "Delete" option is visible.
 */
@Parcelize
public data class MediaGalleryOptionsConfig(
    val isShowInChatVisible: Boolean = true,
    val isReplyVisible: Boolean = true,
    val isSaveMediaVisible: Boolean = true,
    val isDeleteVisible: Boolean = true,
) : Parcelable
