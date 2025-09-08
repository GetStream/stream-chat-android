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

package io.getstream.chat.android.ui.feature.messages.list.internal

import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannelCapabilities
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomSyncStatus
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.randomMessageListViewStyle
import org.amshove.kluent.`should be`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MessageListViewExtensionsKtTest {

    @ParameterizedTest
    @MethodSource("canReplyToMessageArguments")
    fun `Verify canReplyToMessage() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canReplyToMessage(message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canThreadReplyToMessageArguments")
    fun `Verify canThreadReplyToMessage() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        isInThread: Boolean,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canThreadReplyToMessage(isInThread, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canCopyMessageArguments")
    fun `Verify canCopyMessage() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        message: Message,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canCopyMessage(message) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canEditMessageArguments")
    fun `Verify canEditMessage() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canEditMessage(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canDeleteMessageArguments")
    fun `Verify canDeleteMessage() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canDeleteMessage(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canFlagMessageArguments")
    fun `Verify canFlagMessage() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canFlagMessage(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canPinMessageArguments")
    fun `Verify canPinMessage() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canPinMessage(message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canBlockUserArguments")
    fun `Verify canBlockUser() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        currentUser: User?,
        message: Message,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canBlockUser(currentUser, message) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canMarkAsUnreadArguments")
    fun `Verify canMarkAsUnread() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canMarkAsUnread(ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canRetryMessageArguments")
    fun `Verify canRetryMessage() extension function return proper value`(
        messageListViewStyle: MessageListViewStyle,
        currentUser: User?,
        message: Message,
        expectedResult: Boolean,
    ) {
        messageListViewStyle.canRetryMessage(currentUser, message) `should be` expectedResult
    }

    companion object {

        private val currentUser = User(id = randomString())

        @JvmStatic
        fun canReplyToMessageArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(replyEnabled = false),
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(replyEnabled = true),
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canMarkAsUnreadArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(markAsUnreadEnabled = false),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.READ_EVENTS)),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(markAsUnreadEnabled = true),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.READ_EVENTS)),
                true,
            ),
        )

        @JvmStatic
        fun canPinMessageArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(pinMessageEnabled = false),
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.PIN_MESSAGE)),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(pinMessageEnabled = true),
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.PIN_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canEditMessageArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(editMessageEnabled = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(sharedLocation = null),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(command = AttachmentType.GIPHY, sharedLocation = null),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(sharedLocation = null),
                randomChannelCapabilities(
                    exclude = setOf(
                        ChannelCapabilities.UPDATE_OWN_MESSAGE,
                        ChannelCapabilities.UPDATE_ANY_MESSAGE,
                    ),
                ),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(editMessageEnabled = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(sharedLocation = null),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE)),
                true,
            ),
            Arguments.of(
                randomMessageListViewStyle(editMessageEnabled = true),
                currentUser,
                randomMessage(user = currentUser, sharedLocation = null),
                randomChannelCapabilities(
                    include = setOf(ChannelCapabilities.UPDATE_OWN_MESSAGE),
                    exclude = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE),
                ),
                true,
            ),
            Arguments.of(
                randomMessageListViewStyle(editMessageEnabled = true),
                null,
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE)),
                false,
            ),
        )

        @JvmStatic
        fun canBlockUserArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(blockUserEnabled = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                currentUser,
                randomMessage(user = currentUser),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(blockUserEnabled = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                true,
            ),
        )

        @JvmStatic
        fun canRetryMessageArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(retryMessageEnabled = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                currentUser,
                randomMessage(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                currentUser,
                randomMessage(
                    user = currentUser,
                    syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.FAILED_PERMANENTLY)),
                ),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(retryMessageEnabled = true),
                currentUser,
                randomMessage(
                    user = currentUser,
                    syncStatus = SyncStatus.FAILED_PERMANENTLY,
                ),
                true,
            ),
        )

        @JvmStatic
        fun canFlagMessageArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(flagEnabled = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.FLAG_MESSAGE)),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                currentUser,
                randomMessage(user = currentUser),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(flagEnabled = true),
                currentUser,
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.FLAG_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canDeleteMessageArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(deleteMessageEnabled = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(
                    exclude = setOf(
                        ChannelCapabilities.DELETE_OWN_MESSAGE,
                        ChannelCapabilities.DELETE_ANY_MESSAGE,
                    ),
                ),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(deleteMessageEnabled = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.DELETE_ANY_MESSAGE)),
                true,
            ),
            Arguments.of(
                randomMessageListViewStyle(deleteMessageEnabled = true),
                currentUser,
                randomMessage(user = currentUser),
                randomChannelCapabilities(
                    include = setOf(ChannelCapabilities.DELETE_OWN_MESSAGE),
                    exclude = setOf(ChannelCapabilities.DELETE_ANY_MESSAGE),
                ),
                true,
            ),
        )

        @JvmStatic
        fun canThreadReplyToMessageArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(threadsEnabled = false),
                randomBoolean(),
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                randomBoolean(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                randomBoolean(),
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.SEND_REPLY)),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(threadsEnabled = true),
                true,
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.SEND_REPLY)),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(threadsEnabled = true),
                false,
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.SEND_REPLY)),
                true,
            ),
        )

        @JvmStatic
        fun canCopyMessageArguments() = listOf(
            Arguments.of(
                randomMessageListViewStyle(copyTextEnabled = false),
                randomMessage(),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                randomMessage(text = ""),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(),
                randomMessage(attachments = listOf(randomAttachment(titleLink = null, ogUrl = null))),
                false,
            ),
            Arguments.of(
                randomMessageListViewStyle(copyTextEnabled = true),
                randomMessage(
                    text = randomString(),
                    attachments = emptyList(),
                ),
                true,
            ),
            Arguments.of(
                randomMessageListViewStyle(copyTextEnabled = true),
                randomMessage(
                    text = randomString(),
                    attachments = listOf(randomAttachment(titleLink = randomString(), ogUrl = null)),
                ),
                true,
            ),
            Arguments.of(
                randomMessageListViewStyle(copyTextEnabled = true),
                randomMessage(
                    text = randomString(),
                    attachments = listOf(randomAttachment(titleLink = null, ogUrl = randomString())),
                ),
                true,
            ),
        )
    }
}
