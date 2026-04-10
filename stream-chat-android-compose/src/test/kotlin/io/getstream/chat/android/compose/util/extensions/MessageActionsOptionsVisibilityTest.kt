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

package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.compose.ui.components.messageoptions.MessageActionsOptionsVisibility
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannelCapabilities
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomSyncStatus
import org.amshove.kluent.`should be`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date

internal class MessageActionsOptionsVisibilityTest {

    @ParameterizedTest
    @MethodSource("canReplyToMessageArguments")
    fun `Verify canReplyToMessage() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canReplyToMessage(message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canThreadReplyToMessageArguments")
    fun `Verify canThreadReplyToMessage() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        isInThread: Boolean,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canThreadReplyToMessage(
            isInThread,
            message,
            ownCapabilities,
        ) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canCopyMessageArguments")
    fun `Verify canCopyMessage() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        message: Message,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canCopyMessage(message) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canEditMessageArguments")
    fun `Verify canEditMessage() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canEditMessage(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canDeleteMessageArguments")
    fun `Verify canDeleteMessage() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canDeleteMessage(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canFlagMessageArguments")
    fun `Verify canFlagMessage() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canFlagMessage(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canPinMessageArguments")
    fun `Verify canPinMessage() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canPinMessage(message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canMuteUserArguments")
    fun `Verify canMuteUser() extension function returns proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        currentUser: User?,
        message: Message,
        channel: Channel,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canMuteUser(currentUser, message, channel) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canBlockUserArguments")
    fun `Verify canBlockUser() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        currentUser: User?,
        message: Message,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canBlockUser(currentUser, message) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canMarkAsUnreadArguments")
    fun `Verify canMarkAsUnread() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canMarkAsUnread(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canRetryMessageArguments")
    fun `Verify canRetryMessage() extension function return proper value`(
        messageActionsOptionsVisibility: MessageActionsOptionsVisibility,
        currentUser: User?,
        message: Message,
        expectedResult: Boolean,
    ) {
        messageActionsOptionsVisibility.canRetryMessage(currentUser, message) `should be` expectedResult
    }

    companion object {

        private val currentUser = User(id = randomString())

        @JvmStatic
        fun canReplyToMessageArguments() = listOf(
            // case: reply disabled
            Arguments.of(
                MessageActionsOptionsVisibility(isReplyVisible = false),
                randomMessage(deletedAt = null, deletedForMe = false),
                randomChannelCapabilities(),
                false,
            ),
            // case: message not synced
            Arguments.of(
                MessageActionsOptionsVisibility(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            // case: no QUOTE_MESSAGE capability
            Arguments.of(
                MessageActionsOptionsVisibility(),
                randomMessage(deletedAt = null, deletedForMe = false),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                false,
            ),
            // case: message is deleted
            Arguments.of(
                MessageActionsOptionsVisibility(isReplyVisible = true),
                randomMessage(syncStatus = SyncStatus.COMPLETED, deletedAt = Date(), deletedForMe = false),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                false,
            ),
            // case: all conditions met
            Arguments.of(
                MessageActionsOptionsVisibility(isReplyVisible = true),
                randomMessage(syncStatus = SyncStatus.COMPLETED, deletedAt = null, deletedForMe = false),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canMarkAsUnreadArguments() = listOf(
            // case: visibility disabled
            Arguments.of(
                MessageActionsOptionsVisibility(isMarkAsUnreadVisible = false),
                currentUser,
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.READ_EVENTS)),
                false,
            ),
            // case: no READ_EVENTS capability
            Arguments.of(
                MessageActionsOptionsVisibility(isMarkAsUnreadVisible = true),
                currentUser,
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.READ_EVENTS)),
                false,
            ),
            // case: own message
            Arguments.of(
                MessageActionsOptionsVisibility(isMarkAsUnreadVisible = true),
                currentUser,
                randomMessage(user = currentUser),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.READ_EVENTS)),
                false,
            ),
            // case: all conditions met
            Arguments.of(
                MessageActionsOptionsVisibility(isMarkAsUnreadVisible = true),
                currentUser,
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.READ_EVENTS)),
                true,
            ),
        )

        @JvmStatic
        fun canPinMessageArguments() = listOf(
            Arguments.of(
                MessageActionsOptionsVisibility(isPinMessageVisible = false),
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.PIN_MESSAGE)),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isPinMessageVisible = true),
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.PIN_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canEditMessageArguments() = listOf(
            Arguments.of(
                MessageActionsOptionsVisibility(isEditMessageVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(sharedLocation = null),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(command = AttachmentType.GIPHY, sharedLocation = null),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isEditMessageVisible = true),
                currentUser,
                randomMessage(poll = randomPoll(), command = null, sharedLocation = null),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE)),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
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
                MessageActionsOptionsVisibility(isEditMessageVisible = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(sharedLocation = null),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE)),
                true,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isEditMessageVisible = true),
                currentUser,
                randomMessage(user = currentUser, sharedLocation = null),
                randomChannelCapabilities(
                    include = setOf(ChannelCapabilities.UPDATE_OWN_MESSAGE),
                    exclude = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE),
                ),
                true,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isEditMessageVisible = true),
                null,
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE)),
                false,
            ),
        )

        @JvmStatic
        fun canMuteUserArguments() = listOf(
            Arguments.of(
                MessageActionsOptionsVisibility(isMuteUserVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                Channel(config = Config(muteEnabled = true)),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                currentUser,
                randomMessage(user = currentUser),
                Channel(config = Config(muteEnabled = true)),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isMuteUserVisible = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                Channel(config = Config(muteEnabled = false)),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isMuteUserVisible = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                Channel(config = Config(muteEnabled = true)),
                true,
            ),
        )

        @JvmStatic
        fun canBlockUserArguments() = listOf(
            Arguments.of(
                MessageActionsOptionsVisibility(isBlockUserVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                currentUser,
                randomMessage(user = currentUser),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isBlockUserVisible = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                true,
            ),
        )

        @JvmStatic
        fun canRetryMessageArguments() = listOf(
            Arguments.of(
                MessageActionsOptionsVisibility(isRetryMessageVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                currentUser,
                randomMessage(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                currentUser,
                randomMessage(
                    user = currentUser,
                    syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.FAILED_PERMANENTLY)),
                ),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isRetryMessageVisible = true),
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
                MessageActionsOptionsVisibility(isFlagMessageVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.FLAG_MESSAGE)),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                currentUser,
                randomMessage(user = currentUser),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isFlagMessageVisible = true),
                currentUser,
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.FLAG_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canDeleteMessageArguments() = listOf(
            Arguments.of(
                MessageActionsOptionsVisibility(isDeleteMessageVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
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
                MessageActionsOptionsVisibility(isDeleteMessageVisible = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.DELETE_ANY_MESSAGE)),
                true,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isDeleteMessageVisible = true),
                currentUser,
                randomMessage(user = currentUser),
                randomChannelCapabilities(
                    include = setOf(ChannelCapabilities.DELETE_OWN_MESSAGE),
                    exclude = setOf(ChannelCapabilities.DELETE_ANY_MESSAGE),
                ),
                true,
            ),
        )

        @Suppress("LongMethod")
        @JvmStatic
        fun canThreadReplyToMessageArguments() = listOf(
            // case: threads disabled
            Arguments.of(
                MessageActionsOptionsVisibility(isThreadReplyVisible = false),
                randomBoolean(),
                randomMessage(parentId = null, threadParticipants = emptyList()),
                randomChannelCapabilities(),
                false,
            ),
            // case: message not synced
            Arguments.of(
                MessageActionsOptionsVisibility(),
                randomBoolean(),
                randomMessage(
                    parentId = null,
                    threadParticipants = emptyList(),
                    syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED)),
                ),
                randomChannelCapabilities(),
                false,
            ),
            // case: no SEND_REPLY capability
            Arguments.of(
                MessageActionsOptionsVisibility(),
                randomBoolean(),
                randomMessage(parentId = null, threadParticipants = emptyList()),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.SEND_REPLY)),
                false,
            ),
            // case: already in thread
            Arguments.of(
                MessageActionsOptionsVisibility(isThreadReplyVisible = true),
                true,
                randomMessage(parentId = null, threadParticipants = emptyList(), syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.SEND_REPLY)),
                false,
            ),
            // case: message is a thread reply
            Arguments.of(
                MessageActionsOptionsVisibility(isThreadReplyVisible = true),
                false,
                randomMessage(
                    parentId = "parentId",
                    threadParticipants = emptyList(),
                    syncStatus = SyncStatus.COMPLETED,
                ),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.SEND_REPLY)),
                false,
            ),
            // case: all conditions met
            Arguments.of(
                MessageActionsOptionsVisibility(isThreadReplyVisible = true),
                false,
                randomMessage(parentId = null, threadParticipants = emptyList(), syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.SEND_REPLY)),
                true,
            ),
        )

        @JvmStatic
        fun canCopyMessageArguments() = listOf(
            Arguments.of(
                MessageActionsOptionsVisibility(isCopyTextVisible = false),
                randomMessage(),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                randomMessage(text = ""),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(),
                randomMessage(attachments = listOf(randomAttachment(titleLink = null, ogUrl = null))),
                false,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isCopyTextVisible = true),
                randomMessage(
                    text = randomString(),
                    attachments = emptyList(),
                ),
                true,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isCopyTextVisible = true),
                randomMessage(
                    text = randomString(),
                    attachments = listOf(randomAttachment(titleLink = randomString(), ogUrl = null)),
                ),
                true,
            ),
            Arguments.of(
                MessageActionsOptionsVisibility(isCopyTextVisible = true),
                randomMessage(
                    text = randomString(),
                    attachments = listOf(randomAttachment(titleLink = null, ogUrl = randomString())),
                ),
                true,
            ),
        )
    }
}
