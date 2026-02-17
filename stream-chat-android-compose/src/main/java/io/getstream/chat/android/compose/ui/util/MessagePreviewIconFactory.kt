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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * An interface that allows the creation of message preview icons for message types.
 */
public interface MessagePreviewIconFactory {

    /**
     * Creates [InlineTextContent] for all the supported message types.
     */
    public fun createPreviewIcons(): Map<String, InlineTextContent>

    public companion object {
        /**
         * Builds the default message preview icon factory that creates preview icons from
         * drawable resources.
         *
         * @return The default implementation of [MessagePreviewIconFactory].
         */
        public fun defaultFactory(): MessagePreviewIconFactory = DefaultMessagePreviewIconFactory()
    }
}

/**
 * The default implementation of [MessagePreviewIconFactory] that uses drawable resources
 */
internal class DefaultMessagePreviewIconFactory : MessagePreviewIconFactory {

    companion object {
        /**
         * The key for the voice message preview icon.
         */
        internal const val VOICE_MESSAGE = "voice_message"
    }

    override fun createPreviewIcons(): Map<String, InlineTextContent> {
        return mapOf(
            VOICE_MESSAGE to InlineTextContent(
                placeholder = Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                ),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                    contentDescription = null,
                    tint = ChatTheme.colors.textLowEmphasis,
                )
            },
        )
    }
}
