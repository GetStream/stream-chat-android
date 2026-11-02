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

package io.getstream.chat.android.client.errors

import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling.Reason
import io.getstream.chat.android.models.SyncStatus
import io.getstream.result.Error
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ErrorExtensionsTest {

    @ParameterizedTest
    @MethodSource("duplicateMessageErrorArguments")
    fun `isDuplicateMessageError should return expected result`(error: Error, expected: Boolean) {
        error.isDuplicateMessageError() shouldBeEqualTo expected
    }

    @ParameterizedTest
    @MethodSource("messageGoneErrorArguments")
    fun `deleteFailureSyncStatus should complete the delete only when the message is gone`(
        error: Error,
        expected: SyncStatus,
    ) {
        error.deleteFailureSyncStatus() shouldBeEqualTo expected
    }

    @ParameterizedTest
    @MethodSource("scheduledMessageRejectionArguments")
    fun `scheduledMessageRejectionReason should map the scheduled message error codes`(
        error: Error,
        expected: Reason?,
    ) {
        error.scheduledMessageRejectionReason() shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        fun scheduledMessageRejectionArguments() = listOf(
            Arguments.of(Error.NetworkError("disabled", 114, 400), Reason.ScheduledMessagesDisabled),
            Arguments.of(Error.NetworkError("too soon", 115, 400), Reason.ScheduledAtTooSoon),
            Arguments.of(Error.NetworkError("too far", 116, 400), Reason.ScheduledAtTooFar),
            Arguments.of(Error.NetworkError("channel limit", 117, 400), Reason.ScheduledLimitChannel),
            Arguments.of(Error.NetworkError("user limit", 118, 400), Reason.ScheduledLimitUser),
            Arguments.of(Error.NetworkError("id taken", 119, 400), Reason.ScheduledMessageIdTaken),
            Arguments.of(Error.NetworkError("not editable", 120, 400), Reason.ScheduledMessageNotEditable),
            Arguments.of(Error.NetworkError("sending", 121, 409), Reason.ScheduledMessageSending),
            Arguments.of(Error.NetworkError("conflict", 122, 409), Reason.ScheduledMessageConflict),
            Arguments.of(Error.NetworkError("already sent", 123, 400), Reason.ScheduledMessageAlreadySent),
            Arguments.of(Error.NetworkError("canceled", 124, 400), Reason.ScheduledMessageCanceled),
            Arguments.of(Error.NetworkError("other", 113, 400), null),
            Arguments.of(Error.NetworkError("other", 125, 400), null),
            Arguments.of(Error.NetworkError("not found", 16, 404), null),
            Arguments.of(Error.GenericError("no answer"), null),
        )

        @JvmStatic
        fun messageGoneErrorArguments() = listOf(
            Arguments.of(Error.NetworkError("message with id m1 has been deleted", 16, 404), SyncStatus.COMPLETED),
            Arguments.of(Error.NetworkError("message with id m1 doesn't exist", 16, 404), SyncStatus.COMPLETED),
            // A 404 without the backend's code, e.g. from a proxy
            Arguments.of(Error.NetworkError("Not Found", -1, 404), SyncStatus.SYNC_NEEDED),
            Arguments.of(Error.NetworkError("permission denied", 17, 403), SyncStatus.SYNC_NEEDED),
            Arguments.of(Error.NetworkError("internal error", -1, 500), SyncStatus.SYNC_NEEDED),
            Arguments.of(Error.GenericError("message has been deleted"), SyncStatus.SYNC_NEEDED),
        )

        @JvmStatic
        fun duplicateMessageErrorArguments() = listOf(
            Arguments.of(alreadyExistsError(), true),
            Arguments.of(
                alreadyExistsError(
                    message = "SendMessage failed with error: \"a message with ID abc already exists\"",
                ),
                true,
            ),
            Arguments.of(alreadyExistsError(message = "channel members are limited to 100"), false),
            Arguments.of(alreadyExistsError(message = "poll with ID `p1` already exists"), false),
            Arguments.of(alreadyExistsError(message = "vote already exists for user `u1` on poll `p1`"), false),
            Arguments.of(alreadyExistsError(code = ChatErrorCode.AUTHENTICATION_ERROR.code), false),
            Arguments.of(Error.GenericError("a message with ID abc already exists"), false),
        )

        private fun alreadyExistsError(
            code: Int = ChatErrorCode.VALIDATION_ERROR.code,
            message: String = "a message with ID abc already exists",
        ): Error.NetworkError = Error.NetworkError(message, code, 400)
    }
}
