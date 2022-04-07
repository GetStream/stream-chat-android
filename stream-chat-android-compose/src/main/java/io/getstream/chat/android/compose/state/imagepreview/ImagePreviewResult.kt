/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.state.imagepreview

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the Image Preview screen result that we propagate to the Messages screen.
 *
 * @param messageId The ID of the message that we've selected.
 * @param resultType The action that will be executed on the message list screen.
 */
@Parcelize
public class ImagePreviewResult(
    public val messageId: String,
    public val resultType: ImagePreviewResultType,
) : Parcelable

/**
 * Represents the types of actions that result in different behavior in the message list.
 */
public enum class ImagePreviewResultType {
    /**
     * The action when the user wants to scroll to and focus a given image.
     */
    SHOW_IN_CHAT,

    /**
     * The action when the user wants to quote and reply to a message.
     */
    QUOTE,
}
