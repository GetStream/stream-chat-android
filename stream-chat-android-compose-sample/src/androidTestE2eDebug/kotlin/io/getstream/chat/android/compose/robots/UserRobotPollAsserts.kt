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
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message.Poll
import io.getstream.chat.android.e2e.test.uiautomator.isDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitToDisappear
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

fun UserRobot.assertPollMessage(question: String): UserRobot {
    assertTrue(Poll.question(question).waitDisplayed())
    assertTrue(Poll.singleVoteSubtitle.waitDisplayed())
    return this
}

/**
 * Asserts that the option row is shown; when [isChecked] is given, the row also has to reach
 * that vote state, so the assertion waits out the round trip of a vote or its removal.
 */
fun UserRobot.assertPollOption(option: String, isChecked: Boolean? = null): UserRobot {
    val selector = when (isChecked) {
        null -> Poll.option(option)
        else -> Poll.option(option).checked(isChecked)
    }
    assertTrue(selector.waitDisplayed())
    return this
}

fun UserRobot.assertPollOptionVoteCount(option: String, count: Int): UserRobot {
    assertTrue(Poll.optionWithVoteCount(option, count).waitDisplayed())
    return this
}

fun UserRobot.assertPollResults(voterName: String): UserRobot {
    assertTrue(Poll.resultsTitle.waitDisplayed())
    assertTrue(By.text(voterName).waitDisplayed())
    return this
}

fun UserRobot.assertPollClosed(): UserRobot {
    assertTrue(Poll.closedSubtitle.waitDisplayed())
    assertFalse(Poll.endPollButton.waitToDisappear().isDisplayed())
    return this
}
