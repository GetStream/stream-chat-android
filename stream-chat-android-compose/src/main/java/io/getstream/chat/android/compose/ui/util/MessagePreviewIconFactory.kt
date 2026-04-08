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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
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
        internal const val VOICE_MESSAGE = "voice_message"
        internal const val PHOTO = "photo"
        internal const val VIDEO = "video"
        internal const val FILE = "file"
        internal const val LINK = "link"
        internal const val LOCATION = "location"
        internal const val POLL = "poll"
        internal const val DELETED = "deleted"
        internal const val GIPHY = "giphy"
    }

    @Suppress("LongMethod")
    override fun createPreviewIcons(): Map<String, InlineTextContent> {
        val placeholder = Placeholder(
            width = 16.sp,
            height = 16.sp,
            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
        )
        val iconModifier = Modifier.size(16.dp)
        return mapOf(
            VOICE_MESSAGE to InlineTextContent(placeholder) {
                Icon(
                    modifier = iconModifier,
                    painter = painterResource(id = R.drawable.stream_design_ic_voice),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            },
            PHOTO to InlineTextContent(placeholder) {
                Icon(
                    modifier = iconModifier,
                    painter = painterResource(id = R.drawable.stream_design_ic_camera),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            },
            VIDEO to InlineTextContent(placeholder) {
                Icon(
                    modifier = iconModifier,
                    painter = painterResource(id = R.drawable.stream_design_ic_video),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            },
            FILE to InlineTextContent(placeholder) {
                Icon(
                    modifier = iconModifier,
                    painter = painterResource(id = R.drawable.stream_design_ic_file),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            },
            GIPHY to InlineTextContent(placeholder) {
                Icon(
                    modifier = iconModifier,
                    painter = painterResource(id = R.drawable.stream_design_ic_file),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            },
            LINK to InlineTextContent(placeholder) {
                Icon(
                    modifier = iconModifier,
                    painter = painterResource(id = R.drawable.stream_design_ic_link),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            },
            LOCATION to InlineTextContent(placeholder) {
                Icon(
                    modifier = iconModifier,
                    painter = painterResource(id = R.drawable.stream_design_ic_location),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            },
            POLL to InlineTextContent(placeholder) {
                Icon(
                    modifier = iconModifier,
                    painter = painterResource(id = R.drawable.stream_design_ic_poll),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            },
            DELETED to InlineTextContent(placeholder) {
                Icon(
                    modifier = iconModifier,
                    painter = painterResource(id = R.drawable.stream_design_ic_no_sign),
                    contentDescription = null,
                    tint = ChatTheme.colors.textSecondary,
                )
            },
        )
    }
}

@Preview
@Composable
private fun MessagePreviewIconsPreview() {
    ChatPreviewTheme {
        MessagePreviewIcons()
    }
}

@Composable
internal fun MessagePreviewIcons() {
    val icons = ChatTheme.messagePreviewIconFactory.createPreviewIcons()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        icons.keys.forEach { key ->
            Text(
                text = buildAnnotatedString {
                    appendInlineContent(key)
                    append(" $key")
                },
                inlineContent = icons,
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textSecondary,
            )
        }
    }
}
