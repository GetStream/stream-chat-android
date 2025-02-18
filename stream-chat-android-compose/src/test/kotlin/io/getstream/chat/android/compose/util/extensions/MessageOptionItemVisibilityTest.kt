package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.compose.ui.components.messageoptions.MessageOptionItemVisibility
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
import org.amshove.kluent.`should be`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MessageOptionItemVisibilityTest {

    @ParameterizedTest
    @MethodSource("canReplyToMessageArguments")
    fun `Verify canReplyToMessage() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canReplyToMessage(message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canThreadReplyToMessageArguments")
    fun `Verify canThreadReplyToMessage() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canThreadReplyToMessage(message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canCopyMessageArguments")
    fun `Verify canCopyMessage() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        message: Message,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canCopyMessage(message) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canEditMessageArguments")
    fun `Verify canEditMessage() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canEditMessage(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canDeleteMessageArguments")
    fun `Verify canDeleteMessage() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canDeleteMessage(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canFlagMessageArguments")
    fun `Verify canFlagMessage() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canFlagMessage(currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canPinMessageArguments")
    fun `Verify canPinMessage() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canPinMessage(message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canBlockUserArguments")
    fun `Verify canBlockUser() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        currentUser: User?,
        message: Message,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canBlockUser(currentUser, message) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canMarkAsUnreadArguments")
    fun `Verify canMarkAsUnread() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canMarkAsUnread(ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canRetryMessageArguments")
    fun `Verify canRetryMessage() extension function return proper value`(
        messageOptionItemVisibility: MessageOptionItemVisibility,
        currentUser: User?,
        message: Message,
        expectedResult: Boolean,
    ) {
        messageOptionItemVisibility.canRetryMessage(currentUser, message) `should be` expectedResult
    }

    companion object {

        private val currentUser = User(id = randomString())

        @JvmStatic
        fun canReplyToMessageArguments() = listOf(
            Arguments.of(
                MessageOptionItemVisibility(isReplyVisible = false),
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isReplyVisible = true),
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canMarkAsUnreadArguments() = listOf(
            Arguments.of(
                MessageOptionItemVisibility(isMarkAsUnreadVisible = false),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.READ_EVENTS)),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isMarkAsUnreadVisible = true),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.READ_EVENTS)),
                true,
            ),
        )

        @JvmStatic
        fun canPinMessageArguments() = listOf(
            Arguments.of(
                MessageOptionItemVisibility(isPinMessageVisible = false),
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.PIN_MESSAGE)),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isPinMessageVisible = true),
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.PIN_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canEditMessageArguments() = listOf(
            Arguments.of(
                MessageOptionItemVisibility(isEditMessageVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(command = AttachmentType.GIPHY),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(
                    exclude = setOf(
                        ChannelCapabilities.UPDATE_OWN_MESSAGE,
                        ChannelCapabilities.UPDATE_ANY_MESSAGE,
                    ),
                ),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isEditMessageVisible = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE)),
                true,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isEditMessageVisible = true),
                currentUser,
                randomMessage(user = currentUser),
                randomChannelCapabilities(
                    include = setOf(ChannelCapabilities.UPDATE_OWN_MESSAGE),
                    exclude = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE),
                ),
                true,
            ),
        )

        @JvmStatic
        fun canBlockUserArguments() = listOf(
            Arguments.of(
                MessageOptionItemVisibility(isBlockUserVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                currentUser,
                randomMessage(user = currentUser),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isBlockUserVisible = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                true,
            ),
        )

        @JvmStatic
        fun canRetryMessageArguments() = listOf(
            Arguments.of(
                MessageOptionItemVisibility(isRetryMessageVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                currentUser,
                randomMessage(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                currentUser,
                randomMessage(
                    user = currentUser,
                    syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.FAILED_PERMANENTLY)),
                ),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isRetryMessageVisible = true),
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
                MessageOptionItemVisibility(isFlagMessageVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.FLAG_MESSAGE)),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                currentUser,
                randomMessage(user = currentUser),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isFlagMessageVisible = true),
                currentUser,
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.FLAG_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canDeleteMessageArguments() = listOf(
            Arguments.of(
                MessageOptionItemVisibility(isDeleteMessageVisible = false),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
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
                MessageOptionItemVisibility(isDeleteMessageVisible = true),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.DELETE_ANY_MESSAGE)),
                true,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isDeleteMessageVisible = true),
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
                MessageOptionItemVisibility(isThreadReplyVisible = false),
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isThreadReplyVisible = true),
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canCopyMessageArguments() = listOf(
            Arguments.of(
                MessageOptionItemVisibility(isCopyTextVisible = false),
                randomMessage(),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                randomMessage(text = ""),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(),
                randomMessage(attachments = listOf(randomAttachment(titleLink = null, ogUrl = null))),
                false,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isCopyTextVisible = true),
                randomMessage(
                    text = randomString(),
                    attachments = emptyList(),
                ),
                true,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isCopyTextVisible = true),
                randomMessage(
                    text = randomString(),
                    attachments = listOf(randomAttachment(titleLink = randomString(), ogUrl = null)),
                ),
                true,
            ),
            Arguments.of(
                MessageOptionItemVisibility(isCopyTextVisible = true),
                randomMessage(
                    text = randomString(),
                    attachments = listOf(randomAttachment(titleLink = null, ogUrl = randomString())),
                ),
                true,
            ),
        )
    }
}