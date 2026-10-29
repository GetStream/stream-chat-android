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

package io.getstream.chat.android.compose.sample.ui.channel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.data.customSettings
import io.getstream.chat.android.compose.sample.feature.channel.isGroupChannel
import io.getstream.chat.android.compose.sample.feature.scheduled.ChannelScheduledMessagesViewModel
import io.getstream.chat.android.compose.sample.feature.scheduled.ScheduledMessagesActivity
import io.getstream.chat.android.compose.sample.feature.scheduled.ScheduledMessagesComposer
import io.getstream.chat.android.compose.sample.ui.SampleChatTheme
import io.getstream.chat.android.compose.sample.ui.location.LocationComponentFactory
import io.getstream.chat.android.compose.sample.vm.SharedLocationViewModelFactory
import io.getstream.chat.android.compose.ui.messages.ChannelScreen
import io.getstream.chat.android.compose.ui.theme.AttachmentPickerConfig
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ChatUiConfig
import io.getstream.chat.android.compose.ui.theme.ComposerConfig
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.ComposerOptions
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper

class ChannelActivity : ComponentActivity() {

    private val settings by lazy { customSettings() }

    private val cid: String by lazy {
        requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)) { "Channel ID must be provided" }
    }

    private val scheduledMessageId: String? by lazy { intent.getStringExtra(KEY_SCHEDULED_MESSAGE_ID) }
    private val isScheduledAgain: Boolean by lazy { intent.getBooleanExtra(KEY_SCHEDULED_AGAIN, false) }

    private val factory by lazy {
        ChannelViewModelFactory(
            context = this,
            channelId = cid,
            composerOptions = ComposerOptions(
                linkPreviewEnabled = settings.isComposerLinkPreviewEnabled,
                // The scheduled message edited in the composer must not be saved as a draft.
                draftMessagesEnabled = scheduledMessageId == null,
            ),
            messageId = intent.getStringExtra(KEY_MESSAGE_ID),
            parentMessageId = intent.getStringExtra(KEY_PARENT_MESSAGE_ID),
        )
    }

    private val channelInfoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetupChatTheme()
        }
    }

    @Composable
    private fun SetupChatTheme() {
        val componentFactory = remember {
            if (settings.isLocationSharingEnabled) {
                LocationComponentFactory(locationViewModelFactory = SharedLocationViewModelFactory(cid))
            } else {
                object : ChatComponentFactory {}
            }
        }
        SampleChatTheme(
            config = ChatUiConfig(
                composer = ComposerConfig(
                    linkPreviewEnabled = settings.isComposerLinkPreviewEnabled,
                    floatingStyleEnabled = settings.isComposerFloatingStyleEnabled,
                ),
                attachmentPicker = AttachmentPickerConfig(useSystemPicker = settings.isSystemAttachmentPickerEnabled),
            ),
            componentFactory = componentFactory,
        ) {
            SetupContent()
        }
    }

    @Composable
    private fun SetupContent() {
        if (settings.isScheduledMessagesEnabled) {
            ScheduledMessagesContent { ChannelContent() }
        } else {
            ChannelContent()
        }
    }

    @Composable
    private fun ChannelContent() {
        ChannelScreen(
            viewModelFactory = factory,
            onBackPressed = { finish() },
            onChannelAvatarClick = ::openChannelInfo,
            onChannelAvatarClickLabel = stringResource(R.string.channel_open_info),
        )
    }

    @OptIn(InternalStreamChatApi::class)
    @Composable
    private fun ScheduledMessagesContent(content: @Composable () -> Unit) {
        val listViewModel = viewModel(MessageListViewModel::class.java, factory = factory)
        val user by listViewModel.user.collectAsStateWithLifecycle()
        ScheduledMessagesComposer(
            composerViewModel = viewModel(MessageComposerViewModel::class.java, factory = factory),
            attachmentsPickerViewModel = viewModel(AttachmentsPickerViewModel::class.java, factory = factory),
            viewModel = viewModel {
                val storageHelper = AttachmentStorageHelper(applicationContext)
                ChannelScheduledMessagesViewModel(
                    cid = cid,
                    editedMessageId = scheduledMessageId,
                    isScheduledAgain = isScheduledAgain,
                    resolveAttachments = storageHelper::resolveAttachmentFiles,
                )
            },
            channelName = ChatTheme.channelNameFormatter.formatChannelName(listViewModel.channel, user),
            onViewClick = ::openScheduledMessages,
            content = content,
        )
    }

    private fun openScheduledMessages() {
        startActivity(Intent(this, ScheduledMessagesActivity::class.java))
    }

    private fun openChannelInfo(channel: Channel) {
        val intent = if (channel.isGroupChannel) {
            GroupChannelInfoActivity.createIntent(applicationContext, channelId = channel.cid)
        } else {
            DirectChannelInfoActivity.createIntent(applicationContext, channelId = channel.cid)
        }
        channelInfoLauncher.launch(intent)
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_PARENT_MESSAGE_ID = "parentMessageId"
        private const val KEY_SCHEDULED_MESSAGE_ID = "scheduledMessageId"
        private const val KEY_SCHEDULED_AGAIN = "scheduledAgain"

        fun createIntent(
            context: Context,
            channelId: String,
            messageId: String? = null,
            parentMessageId: String? = null,
            scheduledMessageId: String? = null,
            isScheduledAgain: Boolean = false,
        ): Intent {
            return Intent(context, ChannelActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
                putExtra(KEY_MESSAGE_ID, messageId)
                putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
                putExtra(KEY_SCHEDULED_MESSAGE_ID, scheduledMessageId)
                putExtra(KEY_SCHEDULED_AGAIN, isScheduledAgain)
            }
        }
    }
}
