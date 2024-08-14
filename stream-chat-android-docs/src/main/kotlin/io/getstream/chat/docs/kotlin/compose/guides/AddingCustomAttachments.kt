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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.state.messages.attachments.StatefulStreamMediaRecorder
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.components.CancelIcon
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.docs.R
import io.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import java.text.SimpleDateFormat
import java.util.Date

/**
 * [Adding Custom Attachments](https://getstream.io/chat/docs/sdk/android/compose/guides/adding-custom-attachments/)
 */
private object AddingCustomAttachmentsSnippet {

    class MessagesActivity : AppCompatActivity() {

        //TODO add this and related entries to docs when documentation effort occurs
        private val streamMediaRecorder: StreamMediaRecorder by lazy { DefaultStreamMediaRecorder(applicationContext) }
        private val statefulStreamMediaRecorder by lazy { StatefulStreamMediaRecorder(streamMediaRecorder) }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))

            val customFactories = listOf(dateAttachmentFactory)
            val defaultFactories = StreamAttachmentFactories.defaultFactories()

            setContent {
                // Pass in custom factories or combine them with the default ones
                ChatTheme(attachmentFactories = customFactories + defaultFactories) {
                    CustomMessagesScreen(
                        channelId = channelId,
                        statefulStreamMediaRecorder = statefulStreamMediaRecorder,
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
        statefulStreamMediaRecorder: StatefulStreamMediaRecorder,
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
                        statefulStreamMediaRecorder = statefulStreamMediaRecorder,
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
        statefulStreamMediaRecorder: StatefulStreamMediaRecorder,
        onDateSelected: (Long) -> Unit,
    ) {
        val activity = LocalContext.current as AppCompatActivity

        MessageComposer(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            viewModel = viewModel,
            //TODO add this and related entries to docs when documentation effort occurs
            statefulStreamMediaRecorder = statefulStreamMediaRecorder,
            integrations = { // here
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

    val dateAttachmentFactory: AttachmentFactory = AttachmentFactory(
        canHandle = { attachments -> attachments.any { it.type == "date" } },
        content = @Composable { modifier, attachmentState ->
            DateAttachmentContent(
                modifier = modifier,
                attachmentState = attachmentState
            )
        },
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

            CancelIcon(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                onClick = { onAttachmentRemoved(attachment) }
            )
        }
    }

    /**
     * Rename to [MessagesActivity] when adding to docs. This is so we can avoid name conflicts.
     * Snippets used in (https://getstream.io/chat/docs/sdk/android/compose/guides/adding-custom-attachments/).
     */
    class QuotedMessagesActivity : AppCompatActivity() {

        //TODO add this and related entries to docs when documentation effort occurs
        private val streamMediaRecorder: StreamMediaRecorder by lazy { DefaultStreamMediaRecorder(applicationContext) }
        private val statefulStreamMediaRecorder by lazy { StatefulStreamMediaRecorder(streamMediaRecorder) }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))

            val customFactories = listOf(dateAttachmentFactory)
            val defaultFactories = StreamAttachmentFactories.defaultFactories()

            val customQuotedFactories = listOf(quotedDateAttachmentFactory)
            val defaultQuotedFactories = StreamAttachmentFactories.defaultQuotedFactories()

            setContent {
                // pass in custom factories or combine them with the default ones
                ChatTheme(attachmentFactories = customFactories + defaultFactories,
                    quotedAttachmentFactories = customQuotedFactories + defaultQuotedFactories) {
                    CustomMessagesScreen(
                        channelId = channelId,
                        statefulStreamMediaRecorder = statefulStreamMediaRecorder,
                        onBackPressed = { finish() }
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

    val quotedDateAttachmentFactory: AttachmentFactory = AttachmentFactory(
        canHandle = { attachments -> attachments.any { it.type == "date" } },
        content = @Composable { modifier, attachmentState ->
            QuotedDateAttachmentContent(
                modifier = modifier,
                attachmentState = attachmentState
            )
        }
    )

    @Composable
    fun QuotedDateAttachmentContent(
        attachmentState: AttachmentState,
        modifier: Modifier = Modifier,
    ) {
        val attachment = attachmentState.message
            .attachments
            .first { it.type == "date" }
        val formattedDate = attachment.extraData["payload"]
            .toString()
            .replace(",", "\n")

        Column(
            modifier = modifier
                .padding(4.dp)
                .clip(ChatTheme.shapes.attachment)
                .background(ChatTheme.colors.infoAccent)
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
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
                    color = ChatTheme.colors.textHighEmphasis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
