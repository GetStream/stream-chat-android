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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.MessageListItemDecoratorProvider
import io.getstream.chat.android.ui.feature.messages.list.background.MessageBackgroundFactory

/**
 * A factory responsible for creating [DecoratorProvider]s
 * to be used in [io.getstream.chat.android.ui.feature.messages.list.MessageListView].
 */
public interface DecoratorProviderFactory {

    /**
     * Creates a new [DecoratorProvider] for the given [channel].
     *
     * @param channel The channel whose messages are being decorated.
     * @param dateFormatter Formatter used to render message timestamps.
     * @param messageListViewStyle Style configuration for the message list.
     * @param showAvatarPredicate Predicate invoked per message to decide avatar visibility.
     * @param messageBackgroundFactory Factory for message background drawables.
     * @param getLanguageDisplayName Resolves a language code to its display name.
     */
    @Suppress("LongParameterList")
    public fun createDecoratorProvider(
        channel: Channel,
        dateFormatter: DateFormatter,
        messageListViewStyle: MessageListViewStyle,
        showAvatarPredicate: MessageListView.ShowAvatarPredicate,
        messageBackgroundFactory: MessageBackgroundFactory,
        getLanguageDisplayName: (code: String) -> String,
    ): DecoratorProvider

    public companion object {

        /**
         * Creates the default [DecoratorProviderFactory].
         */
        @JvmStatic
        public fun defaultFactory(
            predicate: (Decorator) -> Boolean = { true },
        ): DecoratorProviderFactory = object : DecoratorProviderFactory {
            override fun createDecoratorProvider(
                channel: Channel,
                dateFormatter: DateFormatter,
                messageListViewStyle: MessageListViewStyle,
                showAvatarPredicate: MessageListView.ShowAvatarPredicate,
                messageBackgroundFactory: MessageBackgroundFactory,
                getLanguageDisplayName: (code: String) -> String,
            ): DecoratorProvider {
                return MessageListItemDecoratorProvider(
                    channel,
                    dateFormatter,
                    messageListViewStyle,
                    showAvatarPredicate,
                    messageBackgroundFactory,
                    getLanguageDisplayName,
                    predicate,
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
    override fun createDecoratorProvider(
        channel: Channel,
        dateFormatter: DateFormatter,
        messageListViewStyle: MessageListViewStyle,
        showAvatarPredicate: MessageListView.ShowAvatarPredicate,
        messageBackgroundFactory: MessageBackgroundFactory,
        getLanguageDisplayName: (code: String) -> String,
    ): DecoratorProvider {
        return this@plus.createDecoratorProvider(
            channel,
            dateFormatter,
            messageListViewStyle,
            showAvatarPredicate,
            messageBackgroundFactory,
            getLanguageDisplayName,
        ) + other.createDecoratorProvider(
            channel,
            dateFormatter,
            messageListViewStyle,
            showAvatarPredicate,
            messageBackgroundFactory,
            getLanguageDisplayName,
        )
    }
}
