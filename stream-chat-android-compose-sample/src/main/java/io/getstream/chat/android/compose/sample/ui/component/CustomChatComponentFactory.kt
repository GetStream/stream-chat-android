/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.isPinned
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.channels.list.ChannelItem
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.showOriginalTextAsState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.translations.MessageOriginalTranslationsStore
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.common.state.messages.MessageAction

class CustomChatComponentFactory : ChatComponentFactory {

    @Composable
    override fun LazyItemScope.ChannelListItemContent(
        channelItem: ItemState.ChannelItemState,
        currentUser: User?,
        onChannelClick: (Channel) -> Unit,
        onChannelLongClick: (Channel) -> Unit,
    ) {
        ChannelItem(
            modifier = Modifier
                .animateItem()
                .run {
                    // Highlight the item background color if it is pinned
                    if (channelItem.channel.isPinned()) {
                        background(color = ChatTheme.colors.highlight)
                    } else {
                        this
                    }
                },
            channelItem = channelItem,
            currentUser = currentUser,
            onChannelClick = onChannelClick,
            onChannelLongClick = onChannelLongClick,
        )
    }

    @Composable
    override fun MessageMenu(
        modifier: Modifier,
        message: Message,
        messageOptions: List<MessageOptionItemState>,
        ownCapabilities: Set<String>,
        onMessageAction: (MessageAction) -> Unit,
        onShowMore: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        val isMine = message.user.id == ChatClient.instance().getCurrentUser()?.id
        if (ChatTheme.autoTranslationEnabled && message.isTranslated() && !isMine) {
            val showOriginalText by showOriginalTextAsState(cid = message.cid, messageId = message.id)
            val title = if (showOriginalText) {
                R.string.show_translation
            } else {
                R.string.show_original
            }
            val toggleOriginalTextItem = MessageOptionItemState(
                title = title,
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(R.drawable.ic_translate),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = CustomAction(
                    message = message,
                    extraProperties = mapOf("type" to "toggle_original")
                )
            )
            val action: (MessageAction) -> Unit = { action ->
                if (action is CustomAction && action.extraProperties["type"] == "toggle_original") {
                    // Handle the toggle original text action
                    MessageOriginalTranslationsStore.forChannel(action.message.cid)
                        .toggleOriginalText(action.message.id)
                    onMessageAction(action)
                } else {
                    // Handle other actions
                    onMessageAction(action)
                }
            }
            super.MessageMenu(
                modifier = modifier,
                message = message,
                messageOptions = messageOptions + toggleOriginalTextItem,
                ownCapabilities = ownCapabilities,
                onMessageAction = action,
                onShowMore = onShowMore,
                onDismiss = onDismiss,
            )
        } else {
            super.MessageMenu(
                modifier = modifier,
                message = message,
                messageOptions = messageOptions,
                ownCapabilities = ownCapabilities,
                onMessageAction = onMessageAction,
                onShowMore = onShowMore,
                onDismiss = onDismiss
            )
        }
    }

    private fun Message.isTranslated(): Boolean {
        val userLang = ChatClient.instance().getCurrentUser()?.language.orEmpty()
        val translatedText = getTranslation(userLang).ifEmpty { text }
        return userLang != originalLanguage && translatedText != text
    }
}
