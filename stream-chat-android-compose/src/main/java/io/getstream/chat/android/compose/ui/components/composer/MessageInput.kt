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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.compose.ui.components.composer

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ComposerInputFieldTheme
import io.getstream.chat.android.compose.ui.theme.LocalComposerLinkPreviewEnabled
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.util.buildAnnotatedInputText
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.previewdata.PreviewAttachmentData
import io.getstream.chat.android.previewdata.PreviewLinkData
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState

/**
 * Input field for the Messages/Conversation screen. Allows label customization, as well as handlers
 * when the input changes.
 *
 * @param messageComposerState The state of the input.
 * @param onValueChange Handler when the value changes.
 * @param onAttachmentRemoved Handler when the user removes a selected attachment.
 * @param onCancelAction Handler when the cancel action is clicked.
 * @param onLinkPreviewClick Handler when a link preview is clicked.
 * @param onCancelLinkPreviewClick Handler when the cancel link preview button is clicked.
 * @param onSendClick Handler when the send button is clicked.
 * @param recordingActions The [AudioRecordingActions] to be applied to the input.
 * @param modifier Modifier for styling.
 * @param maxLines The number of lines that are allowed in the input.
 * @param keyboardOptions The [KeyboardOptions] to be applied to the input.
 * @param label Composable that represents the label UI, when there's no input.
 * @param leadingContent The content to be displayed at the start of the input.
 * @param trailingContent The content to be displayed at the end of the input.
 */
@Composable
public fun MessageInput(
    messageComposerState: MessageComposerState,
    onValueChange: (String) -> Unit = {},
    onAttachmentRemoved: (Attachment) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onLinkPreviewClick: ((LinkPreview) -> Unit)? = {},
    onCancelLinkPreviewClick: (() -> Unit)? = null,
    onSendClick: (String, List<Attachment>) -> Unit = { _, _ -> },
    recordingActions: AudioRecordingActions = AudioRecordingActions.None,
    modifier: Modifier = Modifier,
    maxLines: Int = DefaultMessageInputMaxLines,
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    label: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerLabel(state = it)
    },
    leadingContent: @Composable RowScope.() -> Unit = {
        ChatTheme.componentFactory.MessageComposerInputLeadingContent(
            state = messageComposerState,
        )
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        ChatTheme.componentFactory.MessageComposerInputTrailingContent(
            state = messageComposerState,
            onSendClick = onSendClick,
            recordingActions = recordingActions,
        )
    },
) {
    val value = messageComposerState.inputValue
    val canSendMessage = messageComposerState.canSendMessage()

    val visualTransformation = MessageInputVisualTransformation(
        inputFieldTheme = ChatTheme.messageComposerTheme.inputField,
        typography = ChatTheme.typography,
        colors = ChatTheme.colors,
        mentions = messageComposerState.selectedMentions,
    )

    val description = stringResource(id = R.string.stream_compose_cd_message_input)

    TextField(
        modifier = modifier
            .semantics { contentDescription = description }
            .testTag("Stream_ComposerInputField"),
        value = value,
        maxLines = maxLines,
        onValueChange = onValueChange,
        enabled = canSendMessage,
        innerPadding = PaddingValues(),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            Column(
                modifier = Modifier.animateContentSize(alignment = Alignment.BottomStart),
                verticalArrangement = Arrangement.Bottom,
            ) {
                MessageInputHeader(
                    messageComposerState = messageComposerState,
                    onAttachmentRemoved = onAttachmentRemoved,
                    onLinkPreviewClick = onLinkPreviewClick,
                    onCancelActionClick = onCancelAction,
                    onCancelLinkPreviewClick = onCancelLinkPreviewClick,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    leadingContent()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                start = StreamTokens.spacingMd,
                                bottom = StreamTokens.spacingMd,
                            ),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        innerTextField()

                        if (value.isEmpty()) {
                            label(messageComposerState)
                        }
                    }

                    trailingContent()
                }
            }
        },
    )
}

@Composable
private fun MessageInputHeader(
    messageComposerState: MessageComposerState,
    onAttachmentRemoved: (Attachment) -> Unit,
    onCancelActionClick: () -> Unit,
    onLinkPreviewClick: ((LinkPreview) -> Unit)?,
    onCancelLinkPreviewClick: (() -> Unit)?,
) {
    val activeAction = messageComposerState.action
    val attachments = messageComposerState.attachments
    val linkPreviews = messageComposerState.linkPreviews
    val showQuoted = activeAction is Reply
    val showAttachments = attachments.isNotEmpty() && activeAction !is Edit
    val showLinkPreview = ChatTheme.isComposerLinkPreviewEnabled && linkPreviews.isNotEmpty()

    if (showQuoted || showAttachments || showLinkPreview) {
        Column(
            modifier = Modifier.padding(
                top = StreamTokens.spacingSm,
                bottom = StreamTokens.spacing2xs,
            ),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            if (showQuoted) {
                ChatTheme.componentFactory.MessageComposerQuotedMessage(
                    modifier = Modifier.padding(horizontal = StreamTokens.spacingSm),
                    state = messageComposerState,
                    quotedMessage = activeAction.message,
                    onCancelClick = onCancelActionClick,
                )
            }

            if (showAttachments) {
                val previewFactory = ChatTheme.attachmentFactories.firstOrNull { it.canHandle(attachments) }

                previewFactory?.previewContent?.invoke(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    attachments,
                    onAttachmentRemoved,
                )
            }

            if (showLinkPreview) {
                ChatTheme.componentFactory.MessageComposerLinkPreview(
                    modifier = Modifier.padding(horizontal = StreamTokens.spacingSm),
                    linkPreview = linkPreviews.first(),
                    onContentClick = onLinkPreviewClick,
                    onCancelClick = onCancelLinkPreviewClick,
                )
            }
        }
    }
}

@Composable
private fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    maxLines: Int,
    innerPadding: PaddingValues,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
) {
    var textState by remember { mutableStateOf(TextFieldValue(text = value)) }

    if (textState.text != value) {
        // Workaround to move cursor to the end after selecting a suggestion
        LaunchedEffect(value) {
            if (textState.text != value) {
                textState = textState.copy(
                    text = value,
                    selection = TextRange(value.length),
                )
            }
        }
    }

    val theme = ChatTheme.messageComposerTheme.inputField
    val shape = ChatTheme.shapes.inputField

    BasicTextField(
        modifier = modifier
            .border(width = 1.dp, color = ChatTheme.colors.borders, shape = shape)
            .then(
                if (ChatTheme.messageComposerFloatingStyleEnabled) {
                    Modifier.shadow(6.dp, shape = shape)
                } else {
                    Modifier
                },
            )
            .background(ChatTheme.colors.barsBackground)
            .defaultMinSize(minHeight = 48.dp)
            .padding(innerPadding),
        value = textState,
        onValueChange = {
            textState = it
            if (value != it.text) {
                onValueChange(it.text)
            }
        },
        visualTransformation = visualTransformation,
        textStyle = theme.textStyle,
        cursorBrush = SolidColor(theme.cursorBrushColor),
        decorationBox = { innerTextField -> decorationBox(innerTextField) },
        maxLines = maxLines,
        singleLine = maxLines == 1,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
    )
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
 * The default number of lines allowed in the input.
 * The message input will become scrollable after this threshold is exceeded.
 */
private const val DefaultMessageInputMaxLines = 5

@Preview
@Composable
private fun MessageComposerInputPlaceholderPreview() {
    ChatTheme {
        MessageComposerInputPlaceholder()
    }
}

@Composable
internal fun MessageComposerInputPlaceholder() {
    MessageInput(
        messageComposerState = PreviewMessageComposerState,
    )
}

@Preview
@Composable
private fun MessageComposerInputFilledPreview() {
    ChatTheme {
        MessageComposerInputFilled()
    }
}

@Composable
internal fun MessageComposerInputFilled() {
    MessageInput(
        messageComposerState = PreviewMessageComposerState.copy(
            inputValue = "Hello word",
        ),
    )
}

@Preview
@Composable
private fun MessageComposerInputOverflowPreview() {
    ChatTheme {
        MessageComposerInputOverflow()
    }
}

@Composable
internal fun MessageComposerInputOverflow() {
    MessageInput(
        messageComposerState = PreviewMessageComposerState.copy(
            inputValue = "I’ve been thinking about our plan for the next few weeks, " +
                "and I wanted to check in with you about it. There are a few things I’d like to get aligned on, " +
                "especially how we want to divide the work and what the priorities should be. " +
                "I feel like we’ve made good progress so far, " +
                "but there are still a couple of details that keep popping up, " +
                "and it would be nice to sort them out before they turn into bigger issues. " +
                "Let me know when you have a moment so we can go through everything together",
        ),
    )
}

@Preview
@Composable
private fun MessageComposerInputSlowModePreview() {
    ChatTheme {
        MessageComposerInputSlowMode()
    }
}

@Composable
internal fun MessageComposerInputSlowMode() {
    MessageInput(
        messageComposerState = PreviewMessageComposerState.copy(
            inputValue = "Slow mode, wait 9s",
            coolDownTime = 9,
        ),
    )
}

@Preview
@Composable
private fun MessageComposerInputAttachmentsPreview() {
    ChatTheme {
        MessageComposerInputAttachments()
    }
}

@Composable
internal fun MessageComposerInputAttachments() {
    MessageInput(
        messageComposerState = PreviewMessageComposerState.copy(
            attachments = listOf(
                PreviewAttachmentData.attachmentImage1,
                PreviewAttachmentData.attachmentImage2,
                PreviewAttachmentData.attachmentImage3,
                PreviewAttachmentData.attachmentVideo1,
                PreviewAttachmentData.attachmentVideo2,
            ),
        ),
    )
}

@Preview
@Composable
private fun MessageComposerInputLinkPreview() {
    ChatTheme {
        MessageComposerInputLink()
    }
}

@Composable
internal fun MessageComposerInputLink() {
    CompositionLocalProvider(LocalComposerLinkPreviewEnabled provides true) {
        MessageInput(
            messageComposerState = PreviewMessageComposerState.copy(
                linkPreviews = listOf(PreviewLinkData.link1),
            ),
            onCancelLinkPreviewClick = {},
        )
    }
}

@Preview
@Composable
private fun MessageComposerInputReplyPreview() {
    ChatTheme {
        MessageComposerInputReply()
    }
}

@Composable
internal fun MessageComposerInputReply() {
    MessageInput(
        messageComposerState = PreviewMessageComposerState.copy(
            action = Reply(PreviewMessageData.message1),
        ),
    )
}

@Preview
@Composable
private fun MessageComposerInputAttachmentsAndLinkPreview() {
    ChatTheme(
        isComposerLinkPreviewEnabled = true,
    ) {
        MessageComposerInputAttachmentsAndLink()
    }
}

@Composable
internal fun MessageComposerInputAttachmentsAndLink() {
    MessageInput(
        messageComposerState = PreviewMessageComposerState.copy(
            attachments = listOf(
                PreviewAttachmentData.attachmentImage1,
                PreviewAttachmentData.attachmentVideo1,
            ),
            linkPreviews = listOf(PreviewLinkData.link1),
        ),
    )
}

@Preview
@Composable
private fun MessageComposerInputReplyAttachmentsAndLinkPreview() {
    ChatTheme(
        isComposerLinkPreviewEnabled = true,
    ) {
        MessageComposerInputReplyAttachmentsAndLink()
    }
}

@Composable
internal fun MessageComposerInputReplyAttachmentsAndLink() {
    MessageInput(
        messageComposerState = PreviewMessageComposerState.copy(
            action = Reply(PreviewMessageData.message1),
            attachments = listOf(
                PreviewAttachmentData.attachmentImage1,
                PreviewAttachmentData.attachmentVideo1,
            ),
            linkPreviews = listOf(PreviewLinkData.link1),
        ),
    )
}

private val PreviewMessageComposerState = MessageComposerState(
    ownCapabilities = ChannelCapabilities.toSet(),
    hasCommands = true,
)
