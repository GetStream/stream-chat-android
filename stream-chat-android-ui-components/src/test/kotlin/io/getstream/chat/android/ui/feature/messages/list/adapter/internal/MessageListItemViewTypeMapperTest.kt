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

package io.getstream.chat.android.ui.feature.messages.list.adapter.internal

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMediaAttachment
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.DATE_DIVIDER
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.LOADING_INDICATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.START_OF_THE_CHANNEL
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.THREAD_PLACEHOLDER
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.THREAD_SEPARATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.TYPING_INDICATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.UNREAD_SEPARATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.MessageListItemViewTypeMapperTest.Companion.generateGetViewTypeValueInput
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.randomMessageItem
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MessageListItemViewTypeMapperTest {

    /**
     * This method use [generateGetViewTypeValueInput] to generate input.
     */
    @ParameterizedTest
    @MethodSource("generateGetViewTypeValueInput")
    fun `Should generate proper View Type Value`(
        attachmentFactoryManager: AttachmentFactoryManager,
        messageListItem: MessageListItem,
        expected: Int,
    ) {
        val result = MessageListItemViewTypeMapper.getViewTypeValue(
            messageListItem = messageListItem,
            attachmentFactoryManager = attachmentFactoryManager,
        )

        result `should be equal to` expected
    }

    companion object {
        private val attachmentFactoryManager = AttachmentFactoryManager()

        @JvmStatic
        @Suppress("LongMethod")
        fun generateGetViewTypeValueInput() = listOf(
            Arguments.of(
                attachmentFactoryManager,
                MessageListItem.DateSeparatorItem(randomDate()),
                DATE_DIVIDER,
            ),
            Arguments.of(
                attachmentFactoryManager,
                MessageListItem.LoadingMoreIndicatorItem,
                LOADING_INDICATOR,
            ),
            Arguments.of(
                attachmentFactoryManager,
                MessageListItem.ThreadSeparatorItem(
                    date = randomDate(),
                    messageCount = randomInt(),
                ),
                THREAD_SEPARATOR,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = randomDate(),
                        attachments = listOf(
                            Attachment(
                                type = randomString(),
                            ),
                        ),
                    ),
                ),
                MessageListItemViewType.MESSAGE_DELETED,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = null,
                        deletedForMe = false,
                        type = MessageType.ERROR,
                    ),
                ),
                MessageListItemViewType.ERROR_MESSAGE,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = null,
                        deletedForMe = false,
                        type = MessageType.SYSTEM,
                    ),
                ),
                MessageListItemViewType.SYSTEM_MESSAGE,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = null,
                        deletedForMe = false,
                        type = MessageType.EPHEMERAL,
                        command = AttachmentType.GIPHY,
                    ),
                ),
                MessageListItemViewType.GIPHY,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = null,
                        deletedForMe = false,
                        attachments = listOf(Attachment(type = randomString())),
                    ),
                ),
                MessageListItemViewType.CUSTOM_ATTACHMENTS,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = null,
                        deletedForMe = false,
                        attachments = listOf(
                            Attachment(
                                type = AttachmentType.GIPHY,
                                titleLink = randomString(),
                            ),
                        ),
                    ),
                ),
                MessageListItemViewType.GIPHY_ATTACHMENT,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = null,
                        deletedForMe = false,
                        attachments = listOf(Attachment(titleLink = randomString())),
                    ),
                ),
                MessageListItemViewType.LINK_ATTACHMENTS,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = null,
                        deletedForMe = false,
                        attachments = listOf(randomMediaAttachment()),
                    ),
                ),
                MessageListItemViewType.MEDIA_ATTACHMENT,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = null,
                        deletedForMe = false,
                        attachments = listOf(Attachment(type = AttachmentType.FILE)),
                    ),
                ),
                MessageListItemViewType.FILE_ATTACHMENTS,
            ),
            Arguments.of(
                attachmentFactoryManager,
                randomMessageItem(
                    message = randomMessage(
                        deletedAt = null,
                        deletedForMe = false,
                        attachments = emptyList(),
                    ),
                ),
                MessageListItemViewType.PLAIN_TEXT,
            ),
            Arguments.of(
                attachmentFactoryManager,
                MessageListItem.TypingItem(
                    users = listOf(randomUser()),
                ),
                TYPING_INDICATOR,
            ),
            Arguments.of(
                attachmentFactoryManager,
                MessageListItem.ThreadPlaceholderItem,
                THREAD_PLACEHOLDER,
            ),
            Arguments.of(
                attachmentFactoryManager,
                MessageListItem.UnreadSeparatorItem(randomInt()),
                UNREAD_SEPARATOR,
            ),
            Arguments.of(
                attachmentFactoryManager,
                MessageListItem.StartOfTheChannelItem(randomChannel()),
                START_OF_THE_CHANNEL,
            ),
        )
    }
}
