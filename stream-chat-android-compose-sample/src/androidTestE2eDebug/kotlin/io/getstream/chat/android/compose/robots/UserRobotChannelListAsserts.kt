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

package io.getstream.chat.android.compose.robots

import androidx.test.uiautomator.By
import io.getstream.chat.android.compose.pages.ChannelInfoPage
import io.getstream.chat.android.compose.pages.ChannelListPage.ChannelList
import io.getstream.chat.android.compose.pages.ChannelListPage.ChannelList.Channel
import io.getstream.chat.android.compose.pages.ChannelListPage.ChannelMenu
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import io.getstream.chat.android.e2e.test.uiautomator.appContext
import io.getstream.chat.android.e2e.test.uiautomator.isDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitForText
import io.getstream.chat.android.e2e.test.uiautomator.waitToDisappear
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import io.getstream.chat.android.compose.R as ComposeR

fun UserRobot.assertChannelAvatar(): UserRobot {
    assertTrue(Channel.avatar.isDisplayed())
    return this
}

fun UserRobot.assertChannelUnreadCount(count: Int): UserRobot {
    // The badge exposes only a content description; its text is cleared from the
    // semantics tree by clearAndSetSemantics in UnreadCountIndicator.
    val expectedDescription = appContext.resources.getQuantityString(
        ComposeR.plurals.stream_compose_channel_item_unread,
        count,
        count,
    )
    assertTrue(By.desc(expectedDescription).waitDisplayed())
    return this
}

fun UserRobot.assertChannelActionsSheetForGroupChannel(): UserRobot {
    assertTrue(ChannelMenu.viewInfo.waitDisplayed())
    assertTrue(ChannelMenu.leaveGroup.isDisplayed())
    assertTrue(ChannelMenu.deleteGroup.isDisplayed())
    return this
}

fun UserRobot.assertGroupChannelInfoScreen(): UserRobot {
    assertTrue(ChannelInfoPage.groupTitle.waitDisplayed())
    assertTrue(ChannelInfoPage.pinnedMessagesOption.isDisplayed())
    return this
}

fun UserRobot.assertMessageInChannelPreview(text: String, fromCurrentUser: Boolean? = null): UserRobot {
    val expectedPreview = when (fromCurrentUser) {
        true -> "You: $text"
        false -> "${ParticipantRobot.name}: $text"
        null -> text
    }
    assertEquals(
        expectedPreview,
        Channel.messagePreview.waitForText(expectedPreview, mustBeEqual = false).trimEnd(),
    )
    return this
}

fun UserRobot.assertFailedMessageDeliveryStatusInPreview(): UserRobot {
    assertTrue(Channel.deliveryStatusIsFailed.waitDisplayed())
    return this
}

fun UserRobot.assertChannelIsMuted(isMuted: Boolean): UserRobot {
    if (isMuted) {
        assertTrue(Channel.mutedIcon.waitDisplayed())
    } else {
        assertFalse(Channel.mutedIcon.waitToDisappear().isDisplayed())
    }
    return this
}

fun UserRobot.assertChannelListIsEmpty(): UserRobot {
    assertFalse(ChannelList.channels.waitToDisappear().isDisplayed())
    return this
}

fun UserRobot.assertMessagePreviewTimestamp(isDisplayed: Boolean = true): UserRobot {
    if (isDisplayed) {
        assertTrue(Channel.timestamp.waitDisplayed())
    } else {
        assertFalse(Channel.timestamp.waitToDisappear().isDisplayed())
    }
    return this
}
