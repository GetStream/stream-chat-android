// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.guides

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.docs.R
import java.text.SimpleDateFormat
import java.util.Date

/**
 * [Adding Custom Attachments](https://getstream.io/chat/docs/sdk/android/compose/guides/adding-custom-attachments/)
 */
private object AddingCustomAttachmentsSnippet {

    class MessagesActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))

            val customFactories = listOf(dateAttachmentFactory)
            val defaultFactories = StreamAttachmentFactories.defaults()

            setContent {
                // Pass in custom factories or combine them with the default ones
                ChatTheme(
                    componentFactory = CustomComponentFactory(),
                    attachmentFactories = customFactories + defaultFactories,
                ) {
                    CustomMessagesScreen(
                        channelId = channelId,
                        onBackPressed = { finish() },
                    )
                }
            }
        }

        companion object {
            private const val KEY_CHANNEL_ID = "channelId"

            fun getIntent(context: Context, channelId: String): Intent {
                return Intent(context, MessagesActivity::class.java).apply {
                    putExtra(KEY_CHANNEL_ID, channelId)
                }
            }
        }
    }

    @Composable
    fun CustomMessagesScreen(
        channelId: String,
        onBackPressed: () -> Unit = {},
    ) {
        val factory = MessagesViewModelFactory(
            context = LocalContext.current,
            channelId = channelId,
        )

        val composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = factory)

        // Other declarations

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    // Message list header
                },
                bottomBar = {
                    // 1
                    CustomMessageComposer(
                        viewModel = composerViewModel,
                        onDateSelected = { date ->
                            // 2
                            val payload = SimpleDateFormat("MMMM dd, yyyy").format(Date(date))
                            val attachment = Attachment(
                                type = "date",
                                extraData = mutableMapOf("payload" to payload)
                            )

                            // 3
                            composerViewModel.addSelectedAttachments(listOf(attachment))
                        },
                    )
                }
            ) {
                // Message list
            }
        }
    }

    @Composable
    fun CustomMessageComposer(
        viewModel: MessageComposerViewModel,
        onDateSelected: (Long) -> Unit,
    ) {
        val activity = LocalContext.current as AppCompatActivity

        MessageComposer(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            viewModel = viewModel,
            leadingContent = { // here
                IconButton(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp),
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = null,
                            tint = ChatTheme.colors.textLowEmphasis
                        )
                    },
                    onClick = {
                        MaterialDatePicker.Builder
                            .datePicker()
                            .build()
                            .apply {
                                show(activity.supportFragmentManager, null)
                                addOnPositiveButtonClickListener {
                                    onDateSelected(it)
                                }
                            }
                    }
                )
            }
        )
    }
}

class CustomComponentFactory : ChatComponentFactory {
    @Composable
    override fun CustomAttachmentContent(state: AttachmentState, modifier: Modifier) {
        if (state.message.attachments.any { it.type == "date" }) {
            DateAttachmentContent(state, modifier)
        }
    }
}

val dateAttachmentFactory: AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.type == "date" } },
    previewContent = { modifier, attachments, onAttachmentRemoved ->
        DateAttachmentPreviewContent(
            modifier = modifier,
            attachments = attachments,
            onAttachmentRemoved = onAttachmentRemoved
        )
    },
    textFormatter = { attachment ->
        attachment.extraData["payload"].toString()
    },
)

@Composable
fun DateAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
) {
    val attachment = attachmentState.message.attachments.first { it.type == "date" }
    val formattedDate = attachment.extraData["payload"].toString()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(ChatTheme.shapes.attachment)
            .background(ChatTheme.colors.infoAccent)
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(id = R.drawable.ic_calendar),
                contentDescription = null,
                tint = ChatTheme.colors.textHighEmphasis,
            )

            Text(
                text = formattedDate,
                style = ChatTheme.typography.body,
                maxLines = 1,
                color = ChatTheme.colors.textHighEmphasis
            )
        }
    }
}

@Composable
fun DateAttachmentPreviewContent(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val attachment = attachments.first { it.type == "date" }
    val formattedDate = attachment.extraData["payload"].toString()

    Box(
        modifier = modifier
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(color = ChatTheme.colors.barsBackground)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(16.dp)
                .fillMaxWidth(),
            text = formattedDate,
            style = ChatTheme.typography.body,
            maxLines = 1,
            color = ChatTheme.colors.textHighEmphasis
        )

        ComposerCancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            onClick = { onAttachmentRemoved(attachment) }
        )
    }
}
