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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.messages.composer.AudioRecordingTheme
import io.getstream.chat.android.compose.ui.theme.messages.composer.attachments.AttachmentsPreviewTheme
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention

/**
 * Represents the theming for the message composer.
 * @param inputField The theming for the input field in the message composer.
 * @param actionsTheme The theming for the different composer actions.
 * @param audioRecording The theming for the audio recording in the message composer.
 * @param attachmentsPreview The theming for the attachments preview in the message composer.
 */
public data class MessageComposerTheme(
    val inputField: ComposerInputFieldTheme,
    val actionsTheme: ComposerActionsTheme,
    val audioRecording: AudioRecordingTheme,
    val attachmentsPreview: AttachmentsPreviewTheme,
) {

    public companion object {

        /**
         * Builds the default message composer theme.
         *
         * @return A [MessageComposerTheme] instance holding the default theming.
         */
        @Composable
        public fun defaultTheme(
            isInDarkMode: Boolean = isSystemInDarkTheme(),
            typography: StreamTypography = StreamTypography.defaultTypography(),
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            colors: StreamColors = when (isInDarkMode) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
        ): MessageComposerTheme {
            return MessageComposerTheme(
                inputField = ComposerInputFieldTheme.defaultTheme(typography, shapes, colors),
                actionsTheme = ComposerActionsTheme.defaultTheme(colors),
                audioRecording = AudioRecordingTheme.defaultTheme(isInDarkMode, typography, colors),
                attachmentsPreview = AttachmentsPreviewTheme.defaultTheme(isInDarkMode, typography, colors),
            )
        }
    }
}

/**
 * Represents the theming for the input field in the message composer.
 *
 * @param borderShape The shape of the border for the input field.
 * @param backgroundColor The background color for the input field.
 * @param textStyle The text style for the input field.
 * @param cursorBrushColor The color for the cursor in the input field.
 * @param mentionStyleFactory Factory for customization of the mention styles.
 */
public data class ComposerInputFieldTheme(
    val borderShape: Shape,
    val backgroundColor: Color,
    val textStyle: TextStyle,
    val cursorBrushColor: Color,
    val mentionStyleFactory: MentionStyleFactory = MentionStyleFactory.NoStyle,
) {

    public companion object {
        @Composable
        public fun defaultTheme(
            typography: StreamTypography = StreamTypography.defaultTypography(),
            shapes: StreamShapes = StreamShapes.defaultShapes(),
            colors: StreamColors = when (isSystemInDarkTheme()) {
                true -> StreamColors.defaultDarkColors()
                else -> StreamColors.defaultColors()
            },
            mentionStyleFactory: MentionStyleFactory = MentionStyleFactory.NoStyle,
        ): ComposerInputFieldTheme {
            return ComposerInputFieldTheme(
                borderShape = shapes.inputField,
                backgroundColor = colors.inputBackground,
                textStyle = typography.body.copy(
                    color = colors.textHighEmphasis,
                    textDirection = TextDirection.Content,
                ),
                cursorBrushColor = colors.primaryAccent,
                mentionStyleFactory = mentionStyleFactory,
            )
        }
    }
}

/**
 * Factory interface to provide custom styles for mentions.
 */
public interface MentionStyleFactory {

    /**
     * Returns the [SpanStyle] to be applied for the given [mention], or null to apply no special style.
     *
     * @param mention The mention for which to get the style.
     * @return The [SpanStyle] to be applied, or null.
     */
    public fun styleFor(mention: Mention): SpanStyle?

    public companion object {

        /**
         * A mention style factory that doesn't apply any styles.
         */
        public val NoStyle: MentionStyleFactory = object : MentionStyleFactory {
            override fun styleFor(mention: Mention): SpanStyle? = null
        }
    }
}

/**
 * Defines the theming options for the different composer actions.
 *
 * @param attachmentsButton The style for the attachments button.
 * @param commandsButton The style for the commands button.
 * @param sendButton The style for the send button.
 */
@Immutable
public data class ComposerActionsTheme(
    val attachmentsButton: IconContainerStyle,
    val commandsButton: IconContainerStyle,
    val sendButton: IconContainerStyle,
) {

    public companion object {

        /**
         * Builds the default composer actions theme.
         * @param colors The colors to use for the theming.
         *
         * @return A [ComposerActionsTheme] instance holding the default theming.
         */
        @Composable
        public fun defaultTheme(colors: StreamColors): ComposerActionsTheme {
            val attachmentsButton = IconContainerStyle(
                size = ComponentSize(width = 48.dp, height = 48.dp),
                padding = ComponentPadding(0.dp),
                icon = IconStyle(
                    painter = rememberVectorPainter(Icons.Outlined.Add),
                    tint = colors.textLowEmphasis,
                    size = ComponentSize(width = 20.dp, height = 20.dp),
                ),
            )
            val commandsButton = IconContainerStyle(
                size = ComponentSize(width = 32.dp, height = 32.dp),
                padding = ComponentPadding(0.dp),
                icon = IconStyle(
                    painter = painterResource(id = R.drawable.stream_compose_ic_command),
                    tint = colors.textLowEmphasis,
                    size = ComponentSize(width = 24.dp, height = 24.dp),
                ),
            )
            val sendButton = IconContainerStyle(
                size = ComponentSize(width = 48.dp, height = 48.dp),
                padding = ComponentPadding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                icon = IconStyle(
                    painter = painterResource(id = R.drawable.stream_compose_ic_send),
                    tint = colors.textLowEmphasis,
                    size = ComponentSize(width = 20.dp, height = 20.dp),
                ),
            )
            return ComposerActionsTheme(
                attachmentsButton = attachmentsButton,
                commandsButton = commandsButton,
                sendButton = sendButton,
            )
        }
    }
}
