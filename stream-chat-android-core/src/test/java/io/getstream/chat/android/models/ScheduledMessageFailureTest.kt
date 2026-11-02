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

package io.getstream.chat.android.models

import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling.Reason
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ScheduledMessageFailureTest {

    @ParameterizedTest
    @MethodSource("rejections")
    fun `A rejection is retryable only when sending the same changes again can succeed`(
        reason: Reason?,
        expected: Boolean,
    ) {
        ServerRejectedScheduling(reason = reason, message = "rejected").isRetryable shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        fun rejections() = listOf(
            Arguments.of(Reason.ScheduledMessagesDisabled, false),
            Arguments.of(Reason.ScheduledAtTooSoon, false),
            Arguments.of(Reason.ScheduledAtTooFar, false),
            Arguments.of(Reason.ScheduledLimitChannel, false),
            Arguments.of(Reason.ScheduledLimitUser, false),
            Arguments.of(Reason.ScheduledMessageIdTaken, false),
            Arguments.of(Reason.ScheduledMessageNotEditable, false),
            Arguments.of(Reason.ScheduledMessageSending, false),
            Arguments.of(Reason.ScheduledMessageConflict, true),
            Arguments.of(Reason.ScheduledMessageAlreadySent, false),
            Arguments.of(Reason.ScheduledMessageCanceled, false),
            Arguments.of(null, false),
        )
    }
}
