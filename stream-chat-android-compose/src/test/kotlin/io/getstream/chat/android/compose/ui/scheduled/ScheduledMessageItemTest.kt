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

package io.getstream.chat.android.compose.ui.scheduled

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.LocalDeviceFailedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.ui.common.state.scheduled.ScheduledMessageItemState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ScheduledMessageItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `a failed row tells why the server could not send it`() {
        assertReasons(
            ServerFailedPublishing(ServerFailedPublishing.Reason.Banned) to
                "You can't send messages in this channel.",
            ServerFailedPublishing(ServerFailedPublishing.Reason.NoPermission) to
                "You're no longer allowed to post here.",
            ServerFailedPublishing(ServerFailedPublishing.Reason.ChannelFrozen) to "This channel is frozen.",
            ServerFailedPublishing(ServerFailedPublishing.Reason.ChannelDeleted) to "This channel was deleted",
            ServerFailedPublishing(ServerFailedPublishing.Reason.ModerationBlocked) to "Blocked by moderation.",
            ServerFailedPublishing(ServerFailedPublishing.Reason.HookRejected) to "Rejected by the app.",
            ServerFailedPublishing(ServerFailedPublishing.Reason.Expired) to "Not sent: the service was delayed.",
            ServerFailedPublishing(ServerFailedPublishing.Reason.InvalidMessage) to
                "The message no longer meets the app's limits. Edit it.",
            ServerFailedPublishing(ServerFailedPublishing.Reason.PendingNotSupported) to
                "This channel now needs approval. Schedule it again.",
            ServerFailedPublishing(ServerFailedPublishing.Reason.InternalError) to "Something went wrong. Try again.",
        )
    }

    @Test
    fun `a failed row tells why the server rejected the change`() {
        assertReasons(
            rejected(ServerRejectedScheduling.Reason.ScheduledLimitChannel) to
                "You reached the limit of scheduled messages in this channel.",
            rejected(ServerRejectedScheduling.Reason.ScheduledLimitUser) to
                "You reached your limit of scheduled messages.",
            rejected(ServerRejectedScheduling.Reason.ScheduledAtTooSoon) to
                "The scheduled time has passed. Reschedule it.",
            rejected(ServerRejectedScheduling.Reason.ScheduledAtTooFar) to
                "The scheduled time is further ahead than the app allows.",
            rejected(ServerRejectedScheduling.Reason.ScheduledMessagesDisabled) to
                "Scheduled messages are turned off.",
            rejected(ServerRejectedScheduling.Reason.ScheduledMessageSending) to "The message is being sent.",
        )
    }

    @Test
    fun `a failed row with a local or unknown reason shows a readable text`() {
        assertReasons(
            LocalDeviceFailedScheduling(LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed, null) to
                "Couldn't upload attachment",
            LocalDeviceFailedScheduling(LocalDeviceFailedScheduling.Reason.RequestFailed, null) to
                "Couldn't send. Try again",
            ServerFailedPublishing(ServerFailedPublishing.Reason.Unknown("new_reason")) to "Couldn't send. Try again",
            rejected(reason = null) to "Couldn't send. Try again",
        )
    }

    private fun rejected(reason: ServerRejectedScheduling.Reason?) =
        ServerRejectedScheduling(reason = reason, message = "raw server message")

    private fun assertReasons(vararg reasons: Pair<ScheduledMessageFailure, String>) {
        composeTestRule.setContent {
            ChatTheme {
                Column {
                    reasons.forEachIndexed { index, (failure, _) ->
                        ScheduledMessageItem(item = failedItem(index, failure), currentUser = null, onClick = {})
                    }
                }
            }
        }
        reasons.forEach { (_, text) ->
            val rows = composeTestRule.onAllNodes(hasText(text), useUnmergedTree = true).fetchSemanticsNodes()
            assertTrue("No row shows \"$text\"", rows.isNotEmpty())
        }
    }

    private fun failedItem(index: Int, failure: ScheduledMessageFailure): ScheduledMessageItemState {
        val channel = Channel(type = "messaging", id = "general", name = "General")
        val date = Date()
        val message = Message(id = "id-$index", cid = channel.cid, text = "Message $index")
        return ScheduledMessageItemState(
            scheduledMessage = ScheduledMessage(
                id = message.id,
                cid = channel.cid,
                channel = null,
                message = message,
                scheduledAt = date,
                status = ScheduledMessageStatus.Failed,
                failure = failure,
                createdAt = date,
                updatedAt = date,
            ),
            channel = channel,
            quotedMessage = null,
            actions = emptyList(),
        )
    }
}
