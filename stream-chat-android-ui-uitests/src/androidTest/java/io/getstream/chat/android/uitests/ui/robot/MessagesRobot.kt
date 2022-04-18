/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.uitests.ui.robot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import io.getstream.chat.android.uitests.R

/**
 * A factory function for [MessagesRobot].
 */
fun messagesRobot(func: MessagesRobot.() -> Unit) = MessagesRobot().apply { func() }

/**
 * A robot that simulates user behavior on the messages screen.
 */
class MessagesRobot : BaseTestRobot() {

    /**
     * Send a text message on the message list screen.
     */
    fun sendTextMessage(message: String) {
        onView(withId(R.id.messageEditText))
            .perform(typeText(message), closeSoftKeyboard())
    }
}
