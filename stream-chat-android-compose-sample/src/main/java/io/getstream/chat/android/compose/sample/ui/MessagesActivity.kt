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

package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.sample.feature.channel.isGroupChannel
import io.getstream.chat.android.compose.sample.ui.channel.DirectChannelInfoActivity
import io.getstream.chat.android.compose.sample.ui.channel.GroupChannelInfoActivity
import io.getstream.chat.android.compose.sample.ui.component.CustomChatComponentFactory
import io.getstream.chat.android.compose.sample.ui.component.CustomMentionStyleFactory
import io.getstream.chat.android.compose.sample.vm.SharedLocationViewModelFactory
import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptionItemVisibility
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.AttachmentPickerConfig
import io.getstream.chat.android.compose.ui.theme.ChatConfig
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ComposerConfig
import io.getstream.chat.android.compose.ui.theme.ComposerInputFieldTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerTheme
import io.getstream.chat.android.compose.ui.theme.MessageOptionsTheme
import io.getstream.chat.android.compose.ui.theme.ReactionOptionsTheme
import io.getstream.chat.android.compose.ui.theme.StreamDesign
import io.getstream.chat.android.compose.ui.theme.TranslationConfig
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ReactionSortingByLastReactionAt
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility

class MessagesActivity : ComponentActivity() {

    private val cid: String by lazy {
        requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)) { "Channel ID must be provided" }
    }

    private val factory by lazy {
        MessagesViewModelFactory(
            context = this,
            channelId = cid,
            autoTranslationEnabled = ChatApp.autoTranslationEnabled,
            isComposerLinkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
            deletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
            messageId = intent.getStringExtra(KEY_MESSAGE_ID),
            parentMessageId = intent.getStringExtra(KEY_PARENT_MESSAGE_ID),
            isComposerDraftMessageEnabled = true,
        )
    }

    private val listViewModel by viewModels<MessageListViewModel>(factoryProducer = { factory })

    private val attachmentsPickerViewModel by viewModels<AttachmentsPickerViewModel>(factoryProducer = { factory })
    private val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

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
        val isInDarkMode = isSystemInDarkTheme()
        val colors = if (isInDarkMode) StreamDesign.Colors.defaultDark() else StreamDesign.Colors.default()
        val typography = StreamDesign.Typography.default()
        val messageComposerTheme = MessageComposerTheme
            .defaultTheme(isInDarkMode, typography, colors)
            .copy(
                inputField = ComposerInputFieldTheme.defaultTheme(
                    mentionStyleFactory = CustomMentionStyleFactory(colors.accentPrimary),
                ),
            )
        val locationViewModelFactory = SharedLocationViewModelFactory(cid)
        ChatTheme(
            isInDarkMode = isInDarkMode,
            colors = colors,
            typography = typography,
            componentFactory = CustomChatComponentFactory(locationViewModelFactory = locationViewModelFactory),
            dateFormatter = ChatApp.dateFormatter,
            config = ChatConfig(
                composer = ComposerConfig(
                    audioRecordingEnabled = true,
                    linkPreviewEnabled = ChatApp.isComposerLinkPreviewEnabled,
                ),
                translation = TranslationConfig(enabled = ChatApp.autoTranslationEnabled),
                attachmentPicker = AttachmentPickerConfig(useSystemPicker = false),
            ),
            allowUIAutomationTest = true,
            messageComposerTheme = messageComposerTheme,
            reactionOptionsTheme = ReactionOptionsTheme.defaultTheme(),
            messageOptionsTheme = MessageOptionsTheme.defaultTheme(
                optionVisibility = MessageOptionItemVisibility(),
            ),
        ) {
            SetupContent()
        }
    }

    @Composable
    private fun SetupContent() {
        MessagesScreen(
            viewModelFactory = factory,
            reactionSorting = ReactionSortingByLastReactionAt,
            onBackPressed = { finish() },
            onHeaderTitleClick = ::openChannelInfo,
            onMessageLinkClick = { _, link ->
                openLink(link)
            },
        )
    }

    private fun openChannelInfo(channel: Channel) {
        val intent = if (channel.isGroupChannel) {
            GroupChannelInfoActivity.createIntent(applicationContext, channelId = channel.cid)
        } else {
            DirectChannelInfoActivity.createIntent(applicationContext, channelId = channel.cid)
        }
        channelInfoLauncher.launch(intent)
    }

    private fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, link.toUri())
        startActivity(intent)
    }

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"
        private const val KEY_MESSAGE_ID = "messageId"
        private const val KEY_PARENT_MESSAGE_ID = "parentMessageId"

        fun createIntent(
            context: Context,
            channelId: String,
            messageId: String? = null,
            parentMessageId: String? = null,
        ): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(KEY_CHANNEL_ID, channelId)
                putExtra(KEY_MESSAGE_ID, messageId)
                putExtra(KEY_PARENT_MESSAGE_ID, parentMessageId)
            }
        }
    }
}
