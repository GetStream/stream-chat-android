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

package io.getstream.chat.android.compose.ui.components.composer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ComposerInputFieldTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.util.buildAnnotatedInputText
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState

/**
 * Input field for the Messages/Conversation screen. Allows label customization, as well as handlers
 * when the input changes.
 *
 * @param messageComposerState The state of the input.
 * @param onValueChange Handler when the value changes.
 * @param onAttachmentRemoved Handler when the user removes a selected attachment.
 * @param modifier Modifier for styling.
 * @param maxLines The number of lines that are allowed in the input.
 * @param keyboardOptions The [KeyboardOptions] to be applied to the input.
 * @param label Composable that represents the label UI, when there's no input.
 * @param innerLeadingContent Composable that represents the persistent inner leading content.
 * @param innerTrailingContent Composable that represents the persistent inner trailing content.
 */
@Composable
public fun MessageInput(
    messageComposerState: MessageComposerState,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    onLinkPreviewClick: ((LinkPreview) -> Unit)?,
    modifier: Modifier = Modifier,
    maxLines: Int = DefaultMessageInputMaxLines,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    label: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerLabel(state = it)
    },
    innerLeadingContent: @Composable RowScope.() -> Unit = {},
    innerTrailingContent: @Composable RowScope.() -> Unit = {},
) {
    val (value, attachments, activeAction) = messageComposerState
    val canSendMessage = messageComposerState.canSendMessage()

    val visualTransformation = MessageInputVisualTransformation(
        inputFieldTheme = ChatTheme.messageComposerTheme.inputField,
        typography = ChatTheme.typography,
        colors = ChatTheme.colors,
        mentions = messageComposerState.selectedMentions,
    )

    InputField(
        modifier = modifier,
        value = value,
        maxLines = maxLines,
        onValueChange = onValueChange,
        enabled = canSendMessage,
        innerPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            Column {
                if (activeAction is Reply) {
                    ChatTheme.componentFactory.MessageComposerQuotedMessage(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        state = messageComposerState,
                        quotedMessage = activeAction.message,
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                }

                if (ChatTheme.isComposerLinkPreviewEnabled && messageComposerState.linkPreviews.isNotEmpty()) {
                    ChatTheme.componentFactory.MessageComposerLinkPreview(
                        modifier = Modifier,
                        linkPreview = messageComposerState.linkPreviews.first(),
                        onClick = onLinkPreviewClick,
                    )
                }

                AttachmentPreview(attachments, activeAction, onAttachmentRemoved)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    innerLeadingContent()

                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()

                        if (value.isEmpty()) {
                            label(messageComposerState)
                        }
                    }

                    innerTrailingContent()
                }
            }
        },
    )
}

@Composable
private fun AttachmentPreview(
    attachments: List<Attachment>,
    activeAction: MessageAction?,
    onAttachmentRemoved: (Attachment) -> Unit,
) {
    if (attachments.isNotEmpty() && activeAction !is Edit) {
        val previewFactory = ChatTheme.attachmentFactories.firstOrNull { it.canHandle(attachments) }

        previewFactory?.previewContent?.invoke(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            attachments,
            onAttachmentRemoved,
        )

        Spacer(modifier = Modifier.size(16.dp))
    }
}

/**
 * Visual transformation applied to the message input field.
 * Applies text styling, link styling, and mention styling to the input text.
 *
 * @param inputFieldTheme The theme for the input field.
 * @param typography The typography styles to be used.
 * @param colors The color palette to be used.
 * @param mentions The set of mentions to be styled in the input text.
 */
private class MessageInputVisualTransformation(
    val inputFieldTheme: ComposerInputFieldTheme,
    val typography: StreamTypography,
    val colors: StreamColors,
    val mentions: Set<Mention>,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val textColor = inputFieldTheme.textStyle.color
        val fontStyle = typography.body.fontStyle
        val linkStyle = TextStyle(
            color = colors.primaryAccent,
            textDecoration = TextDecoration.Underline,
        )
        val transformed = buildAnnotatedInputText(
            text = text.text,
            textColor = textColor,
            textFontStyle = fontStyle,
            linkStyle = linkStyle,
            mentions = mentions,
            mentionStyleFactory = inputFieldTheme.mentionStyleFactory,
        )
        return TransformedText(transformed, OffsetMapping.Identity)
    }
}

/**
 * The default number of lines allowed in the input. The message input will become scrollable after
 * this threshold is exceeded.
 */
private const val DefaultMessageInputMaxLines = 6
