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

package io.getstream.chat.android.uitests.ui.uicomponents.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import io.getstream.chat.android.uitests.R

/**
 * A factory function for [MessagesRobotUiComponents].
 */
internal fun messagesRobot(block: MessagesRobotUiComponents.() -> Unit) = MessagesRobotUiComponents().apply { block() }

/**
 * A robot that simulates user behavior on the messages screen.
 */
internal class MessagesRobotUiComponents : BaseUiComponentsTestRobot() {

    /**
     * Types certain text in the message composer.
     *
     * @param text The text what will be typed in the message input.
     */
    fun typeMessageText(text: String) {
        waitForViewWithId(R.id.messageEditText)
        onView(withId(R.id.messageEditText)).perform(typeText(text), closeSoftKeyboard())
    }

    /**
     * Clicks the "send message" button.
     */
    fun clickSendButton() {
        waitForViewWithId(R.id.sendMessageButton)
        clickElementById(R.id.sendMessageButton)
    }

    /**
     * Assert that any channel is displayed on the screen.
     */
    fun assertMessageIsDisplayed() {
        assertElementWithIdIsDisplayed(R.id.messageItemView)
    }
}
