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

package io.getstream.chat.android.ui.common.utils

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

internal class CapabilitiesHelperTest {

    @ParameterizedTest
    @MethodSource("canReplyToMessageArguments")
    fun `Verify canReplyToMessage() extension function return proper value`(
        localFlag: Boolean,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        canReplyToMessage(localFlag, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canThreadReplyToMessageArguments")
    fun `Verify canThreadReplyToMessage() extension function return proper value`(
        localFlag: Boolean,
        isInThread: Boolean,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        canThreadReplyToMessage(localFlag, isInThread, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canCopyMessageArguments")
    fun `Verify canCopyMessage() extension function return proper value`(
        localFlag: Boolean,
        message: Message,
        expectedResult: Boolean,
    ) {
        canCopyMessage(localFlag, message) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canEditMessageArguments")
    fun `Verify canEditMessage() extension function return proper value`(
        localFlag: Boolean,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        canEditMessage(localFlag, currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canDeleteMessageArguments")
    fun `Verify canDeleteMessage() extension function return proper value`(
        localFlag: Boolean,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        canDeleteMessage(localFlag, currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canFlagMessageArguments")
    fun `Verify canFlagMessage() extension function return proper value`(
        localFlag: Boolean,
        currentUser: User?,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        canFlagMessage(localFlag, currentUser, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canPinMessageArguments")
    fun `Verify canPinMessage() extension function return proper value`(
        localFlag: Boolean,
        message: Message,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        canPinMessage(localFlag, message, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canBlockUserArguments")
    fun `Verify canBlockUser() extension function return proper value`(
        localFlag: Boolean,
        currentUser: User?,
        message: Message,
        expectedResult: Boolean,
    ) {
        canBlockUser(localFlag, currentUser, message) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canMarkAsUnreadArguments")
    fun `Verify canMarkAsUnread() extension function return proper value`(
        localFlag: Boolean,
        ownCapabilities: Set<String>,
        expectedResult: Boolean,
    ) {
        canMarkAsUnread(localFlag, ownCapabilities) `should be` expectedResult
    }

    @ParameterizedTest
    @MethodSource("canRetryMessageArguments")
    fun `Verify canRetryMessage() extension function return proper value`(
        localFlag: Boolean,
        currentUser: User?,
        message: Message,
        expectedResult: Boolean,
    ) {
        canRetryMessage(localFlag, currentUser, message) `should be` expectedResult
    }

    companion object {

        private val currentUser = User(id = randomString())

        @JvmStatic
        fun canReplyToMessageArguments() = listOf(
            Arguments.of(
                false,
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                false,
            ),
            Arguments.of(
                true,
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.QUOTE_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canMarkAsUnreadArguments() = listOf(
            Arguments.of(
                false,
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.READ_EVENTS)),
                false,
            ),
            Arguments.of(
                true,
                randomChannelCapabilities(include = setOf(ChannelCapabilities.READ_EVENTS)),
                true,
            ),
        )

        @JvmStatic
        fun canPinMessageArguments() = listOf(
            Arguments.of(
                false,
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                randomMessage(syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED))),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.PIN_MESSAGE)),
                false,
            ),
            Arguments.of(
                true,
                randomMessage(syncStatus = SyncStatus.COMPLETED),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.PIN_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canEditMessageArguments() = listOf(
            Arguments.of(
                false,
                currentUser.takeIf { randomBoolean() },
                randomMessage(sharedLocation = null),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(command = AttachmentType.GIPHY, sharedLocation = null),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
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
                true,
                currentUser.takeIf { randomBoolean() },
                randomMessage(sharedLocation = null),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE)),
                true,
            ),
            Arguments.of(
                true,
                currentUser,
                randomMessage(user = currentUser, sharedLocation = null),
                randomChannelCapabilities(
                    include = setOf(ChannelCapabilities.UPDATE_OWN_MESSAGE),
                    exclude = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE),
                ),
                true,
            ),
            Arguments.of(
                true,
                null,
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.UPDATE_ANY_MESSAGE)),
                false,
            ),
        )

        @JvmStatic
        fun canBlockUserArguments() = listOf(
            Arguments.of(
                false,
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                currentUser,
                randomMessage(user = currentUser),
                false,
            ),
            Arguments.of(
                true,
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                true,
            ),
        )

        @JvmStatic
        fun canRetryMessageArguments() = listOf(
            Arguments.of(
                false,
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                currentUser,
                randomMessage(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                currentUser,
                randomMessage(
                    user = currentUser,
                    syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.FAILED_PERMANENTLY)),
                ),
                false,
            ),
            Arguments.of(
                true,
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
                false,
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.FLAG_MESSAGE)),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                currentUser,
                randomMessage(user = currentUser),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                true,
                currentUser,
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.FLAG_MESSAGE)),
                true,
            ),
        )

        @JvmStatic
        fun canDeleteMessageArguments() = listOf(
            Arguments.of(
                false,
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
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
                true,
                currentUser.takeIf { randomBoolean() },
                randomMessage(),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.DELETE_ANY_MESSAGE)),
                true,
            ),
            Arguments.of(
                true,
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
                false,
                false,
                randomMessage(
                    parentId = null,
                    threadParticipants = emptyList(),
                ),
                randomChannelCapabilities(),
                false,
            ),
            // case: message not synced
            Arguments.of(
                randomBoolean(),
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
                randomBoolean(),
                randomBoolean(),
                randomMessage(
                    parentId = null,
                    threadParticipants = emptyList(),
                ),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.SEND_REPLY)),
                false,
            ),
            // case: message already in a thread
            Arguments.of(
                true,
                true,
                randomMessage(
                    parentId = null,
                    threadParticipants = emptyList(),
                    syncStatus = SyncStatus.COMPLETED,
                ),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.SEND_REPLY)),
                false,
            ),
            // case: message not synced, with SEND_REPLY capability
            Arguments.of(
                true,
                false,
                randomMessage(
                    parentId = null,
                    threadParticipants = emptyList(),
                    syncStatus = randomSyncStatus(exclude = listOf(SyncStatus.COMPLETED)),
                ),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.SEND_REPLY)),
                false,
            ),
            // case: message is synced, without SEND_REPLY capability
            Arguments.of(
                true,
                false,
                randomMessage(
                    parentId = null,
                    threadParticipants = emptyList(),
                    syncStatus = SyncStatus.COMPLETED,
                ),
                randomChannelCapabilities(exclude = setOf(ChannelCapabilities.SEND_REPLY)),
                false,
            ),
            // case: message is a thread reply
            Arguments.of(
                true,
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
                true,
                false,
                randomMessage(
                    parentId = null,
                    threadParticipants = emptyList(),
                    syncStatus = SyncStatus.COMPLETED,
                ),
                randomChannelCapabilities(include = setOf(ChannelCapabilities.SEND_REPLY)),
                true,
            ),
        )

        @JvmStatic
        fun canCopyMessageArguments() = listOf(
            Arguments.of(
                false,
                randomMessage(),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                randomMessage(text = ""),
                false,
            ),
            Arguments.of(
                randomBoolean(),
                randomMessage(attachments = listOf(randomAttachment(titleLink = null, ogUrl = null))),
                false,
            ),
            Arguments.of(
                true,
                randomMessage(
                    text = randomString(),
                    attachments = emptyList(),
                ),
                true,
            ),
            Arguments.of(
                true,
                randomMessage(
                    text = randomString(),
                    attachments = listOf(randomAttachment(titleLink = randomString(), ogUrl = null)),
                ),
                true,
            ),
            Arguments.of(
                true,
                randomMessage(
                    text = randomString(),
                    attachments = listOf(randomAttachment(titleLink = null, ogUrl = randomString())),
                ),
                true,
            ),
        )
    }
}
