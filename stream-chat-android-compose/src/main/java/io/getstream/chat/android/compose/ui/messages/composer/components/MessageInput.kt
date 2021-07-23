package io.getstream.chat.android.compose.ui.messages.composer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.utils.UiUtils
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.list.MessageAction
import io.getstream.chat.android.compose.state.messages.list.Reply
import io.getstream.chat.android.compose.ui.components.InputField
import io.getstream.chat.android.compose.ui.messages.composer.DefaultComposerLabel
import io.getstream.chat.android.compose.ui.messages.list.QuotedMessage
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Shows the options "header" for the message input component. This is based on the currently active
 * message action - [Reply] or [Edit].
 *
 * @param modifier - Modifier for styling.
 * @param activeAction - Currently active [MessageAction].
 * @param onCancelAction - Handler when the user cancels the current action.
 * */
@Composable
fun MessageInputOptions(
    activeAction: MessageAction,
    onCancelAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val optionImage = if (activeAction is Reply) Icons.Default.Reply else Icons.Default.Edit
    val title = stringResource(
        id = if (activeAction is Reply) R.string.reply_to_message else R.string.edit_message
    )

    Row(
        modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            modifier = Modifier.padding(4.dp),
            imageVector = optionImage,
            contentDescription = null
        )

        Text(
            text = title,
            style = ChatTheme.typography.bodyBold
        )

        Icon(
            modifier = Modifier
                .padding(4.dp)
                .clickable(
                    onClick = onCancelAction,
                    indication = rememberRipple(bounded = false),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            imageVector = Icons.Default.Cancel,
            contentDescription = stringResource(id = R.string.cancel)
        )
    }
}

/**
 * Input field for the Messages/Conversation screen. Allows label customization, as well as handlers
 * when the input changes.
 *
 * @param value - Current value of the input.
 * @param attachments - Currently selected and visible attachments.
 * @param activeAction - Currently active action (for [Edit] UI).
 * @param onValueChange - Handler when the value changes.
 * @param onAttachmentRemoved - Handler when the user removes a selected attachment.
 * @param modifier - Modifier for styling.
 * @param label - Composable function that represents the label UI, when there's no input/focus.
 * */
@InternalStreamChatApi
@ExperimentalFoundationApi
@Composable
fun MessageInput(
    value: String,
    attachments: List<Attachment>,
    activeAction: MessageAction?,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = { DefaultComposerLabel() },
) {

    InputField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        decorationBox = { innerTextField ->
            Column {
                if (activeAction is Reply) {
                    QuotedMessage(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        message = activeAction.message
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                }

                if (attachments.isNotEmpty()) {
                    MessageInputAttachments(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        attachments = attachments,
                        onAttachmentRemoved = onAttachmentRemoved
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                }

                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    if (value.isEmpty()) {
                        label()
                    }

                    innerTextField()
                }
            }
        }
    )
}

/**
 * Shows the selected attachments within the composer, based on if they're images or files.
 *
 * @param attachments - List of selected attachments.
 * @param onAttachmentRemoved - Handler when the user removes a selected attachment.
 * @param modifier - Modifier for styling.
 * */
@InternalStreamChatApi
@ExperimentalFoundationApi
@Composable
private fun MessageInputAttachments(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (attachments.any { it.type == "image" }) {
        MessageInputImageAttachments(attachments, onAttachmentRemoved, modifier)
    } else {
        MessageInputFileAttachments(attachments, onAttachmentRemoved, modifier)
    }
}

/**
 * UI for currently selected image attachments, within the [MessageInput].
 *
 * @param attachments - Selected attachments.
 * @param onAttachmentRemoved - Handler when the user removes an attachment from the list.
 * @param modifier - Modifier for styling.
 * */
@Composable
internal fun MessageInputImageAttachments(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start)
    ) {
        items(attachments) { image ->
            val painter = rememberImagePainter(data = image.upload?.toUri())
            Box(
                modifier = Modifier
                    .size(95.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )

                Icon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onAttachmentRemoved(image) }
                        ),
                    imageVector = Icons.Default.Cancel,
                    contentDescription = stringResource(id = R.string.cancel)
                )
            }
        }
    }
}

/**
 * UI for currently selected file attachments, within the [MessageInput].
 *
 * @param attachments - Selected attachments.
 * @param onAttachmentRemoved - Handler when the user removes an attachment from the list.
 * @param modifier - Modifier for styling.
 * */
@InternalStreamChatApi
@Composable
internal fun MessageInputFileAttachments(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .heightIn(max = 300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
    ) {
        items(attachments) { file ->
            Surface(
                modifier = Modifier.padding(1.dp),
                color = ChatTheme.colors.appBackground, shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ChatTheme.colors.borders)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(height = 40.dp, width = 35.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        painter = painterResource(id = UiUtils.getIcon(file.mimeType)),
                        contentDescription = null
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(start = 16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = file.title ?: file.name ?: "",
                            style = ChatTheme.typography.body,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = ChatTheme.colors.textHighEmphasis
                        )

                        Text(
                            text = UiUtils.getFileSizeHumanized(file.fileSize),
                            style = ChatTheme.typography.footnote,
                            color = ChatTheme.colors.textLowEmphasis
                        )
                    }

                    Icon(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { onAttachmentRemoved(file) }
                            ),
                        imageVector = Icons.Default.Cancel,
                        contentDescription = stringResource(id = R.string.cancel)
                    )
                }
            }
        }
    }
}