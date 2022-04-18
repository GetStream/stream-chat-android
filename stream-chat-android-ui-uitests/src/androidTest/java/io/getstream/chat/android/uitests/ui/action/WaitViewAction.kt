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

package io.getstream.chat.android.uitests.ui.action

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher

class WaitViewAction private constructor(
    private val viewId: Int,
    private val millis: Long,
) : ViewAction {

    companion object {
        fun waitId(viewId: Int, millis: Long = 5000): ViewInteraction {
            return onView(isRoot()).perform(WaitViewAction(viewId, millis))
        }
    }

    override fun getConstraints(): Matcher<View> {
        return isRoot()
    }

    override fun getDescription(): String {
        return "Waiting for $viewId"
    }

    override fun perform(uiController: UiController, view: View?) {
        uiController.loopMainThreadUntilIdle()
        val startTime = System.currentTimeMillis()
        val endTime = startTime + millis
        val viewMatcher = withId(viewId)
        do {
            for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                if (viewMatcher.matches(child)) {
                    return
                }
            }
            uiController.loopMainThreadForAtLeast(50)
        } while (System.currentTimeMillis() < endTime)
        throw IllegalStateException("View waiting timed out")
    }
}
