/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.MessageListItemDecoratorProvider
import io.getstream.chat.android.ui.feature.messages.list.background.MessageBackgroundFactory

public interface DecoratorProviderFactory {
    public fun createProvider(
        channel: Channel,
        dateFormatter: DateFormatter,
        messageListViewStyle: MessageListViewStyle,
        showAvatarPredicate: MessageListView.ShowAvatarPredicate,
        messageBackgroundFactory: MessageBackgroundFactory,
        deletedMessageVisibility: () -> DeletedMessageVisibility,
        getLanguageDisplayName: (code: String) -> String,
    ): DecoratorProvider

    public companion object {
        public fun default(): DecoratorProviderFactory = object : DecoratorProviderFactory {
            override fun createProvider(
                channel: Channel,
                dateFormatter: DateFormatter,
                messageListViewStyle: MessageListViewStyle,
                showAvatarPredicate: MessageListView.ShowAvatarPredicate,
                messageBackgroundFactory: MessageBackgroundFactory,
                deletedMessageVisibility: () -> DeletedMessageVisibility,
                getLanguageDisplayName: (code: String) -> String,
            ): DecoratorProvider {
                return MessageListItemDecoratorProvider(
                    channel,
                    dateFormatter,
                    messageListViewStyle,
                    showAvatarPredicate,
                    messageBackgroundFactory,
                    deletedMessageVisibility,
                    getLanguageDisplayName,
                )
            }
        }
    }
}

/**
 * Combines two [DecoratorProviderFactory]s into a single [DecoratorProviderFactory].
 */
public operator fun DecoratorProviderFactory.plus(
    other: DecoratorProviderFactory,
): DecoratorProviderFactory = object : DecoratorProviderFactory {
    override fun createProvider(
        channel: Channel,
        dateFormatter: DateFormatter,
        messageListViewStyle: MessageListViewStyle,
        showAvatarPredicate: MessageListView.ShowAvatarPredicate,
        messageBackgroundFactory: MessageBackgroundFactory,
        deletedMessageVisibility: () -> DeletedMessageVisibility,
        getLanguageDisplayName: (code: String) -> String,
    ): DecoratorProvider {
        return this@plus.createProvider(
            channel,
            dateFormatter,
            messageListViewStyle,
            showAvatarPredicate,
            messageBackgroundFactory,
            deletedMessageVisibility,
            getLanguageDisplayName,
        ) + other.createProvider(
            channel,
            dateFormatter,
            messageListViewStyle,
            showAvatarPredicate,
            messageBackgroundFactory,
            deletedMessageVisibility,
            getLanguageDisplayName,
        )
    }
}
