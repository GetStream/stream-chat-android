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

package io.getstream.chat.android.guides.catalog.compose.customattachments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.datepicker.MaterialDatePicker
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.guides.R
import io.getstream.chat.android.guides.catalog.compose.customattachments.factory.dateAttachmentFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ReactionSortingByFirstReactionAt
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import java.text.SimpleDateFormat
import java.util.Date

/**
 * An Activity representing a self-contained chat screen with custom attachment factories.
 */
class MessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID))

        val customFactories = listOf(dateAttachmentFactory)
        val defaultFactories = StreamAttachmentFactories.defaults()

        setContent {
            ChatTheme(
                attachmentFactories = customFactories + defaultFactories,
            ) {
                CustomMessagesScreen(
                    channelId = channelId,
                    onBackPressed = { finish() },
                    threadLoadOlderToNewer = false,
                )
            }
        }
    }

    /**
     * A custom [MessagesScreen] with the support for date attachments.
     *
     * @param channelId The ID of the opened channel.
     * @param threadLoadOlderToNewer Flag to load older messages to newer messages in the thread.
     * @param onBackPressed Handler for the back action.
     */
    @Composable
    fun CustomMessagesScreen(
        channelId: String,
        threadLoadOlderToNewer: Boolean,
        onBackPressed: () -> Unit = {},
    ) {
        val factory = MessagesViewModelFactory(
            context = LocalContext.current,
            channelId = channelId,
            threadLoadOlderToNewer = threadLoadOlderToNewer,
        )

        val messageListViewModel = viewModel(MessageListViewModel::class.java, factory = factory)
        val composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = factory)

        val messageMode = messageListViewModel.messageMode
        val currentUser by messageListViewModel.user.collectAsState()
        val connectionState by messageListViewModel.connectionState.collectAsState()

        BackHandler(enabled = true, onBack = onBackPressed)

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    MessageListHeader(
                        modifier = Modifier.height(56.dp),
                        channel = messageListViewModel.channel,
                        currentUser = currentUser,
                        messageMode = messageMode,
                        onBackPressed = onBackPressed,
                        connectionState = connectionState,
                    )
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
                                extraData = mutableMapOf("payload" to payload),
                            )

                            // 3
                            composerViewModel.addSelectedAttachments(listOf(attachment))
                        },
                    )
                },
            ) {
                MessageList(
                    modifier = Modifier
                        .padding(it)
                        .background(ChatTheme.colors.appBackground)
                        .fillMaxSize(),
                    viewModel = messageListViewModel,
                    reactionSorting = ReactionSortingByFirstReactionAt,
                    onThreadClick = { message ->
                        composerViewModel.setMessageMode(MessageMode.MessageThread(message))
                        messageListViewModel.openMessageThread(message)
                    },
                    onLongItemClick = {
                        composerViewModel.performMessageAction(Reply(it))
                    },
                )
            }
        }
    }

    /**
     * A custom [MessageComposer] with a button that shows [MaterialDatePicker].
     *
     * @param viewModel The ViewModel that provides pieces of data to show in the composer, like the
     * currently selected integration data or the user input. It also handles sending messages.
     * @param onDateSelected Handler when the user selects a date.
     */
    @Composable
    private fun CustomMessageComposer(
        viewModel: MessageComposerViewModel,
        onDateSelected: (Long) -> Unit,
    ) {
        val activity = LocalContext.current as AppCompatActivity

        MessageComposer(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            viewModel = viewModel,
            integrations = {
                IconButton(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp),
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = null,
                            tint = ChatTheme.colors.textLowEmphasis,
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
                    },
                )
            },
        )
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        /**
         * Creates an [Intent] to start [MessagesActivity].
         *
         * @param context The context used to create the intent.
         * @param channelId The id of the channel.
         * @return The [Intent] to start [MessagesActivity].
         */
        fun createIntent(context: Context, channelId: String): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
            }
        }
    }
}
