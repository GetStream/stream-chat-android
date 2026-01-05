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

package io.getstream.chat.android.uitests.ui.uicomponents.action

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher

/**
 * An actions that allows to wait for the desired view to appear.
 *
 * @param resId The View identifier.
 * @param intervalMillis The interval between View lookup attempts.
 * @param timeoutMillis The time period to wait for the View to appear.
 */
class WaitViewAction private constructor(
    private val resId: Int,
    private val intervalMillis: Long,
    private val timeoutMillis: Long,
) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return isRoot()
    }

    override fun getDescription(): String {
        return "Waiting for $resId"
    }

    override fun perform(uiController: UiController, view: View?) {
        uiController.loopMainThreadUntilIdle()
        val startTimeMillis = System.currentTimeMillis()
        val endTimeMillis = startTimeMillis + timeoutMillis
        val viewMatcher = withId(resId)
        do {
            for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                if (viewMatcher.matches(child)) {
                    return
                }
            }
            uiController.loopMainThreadForAtLeast(intervalMillis)
        } while (System.currentTimeMillis() < endTimeMillis)
        throw IllegalStateException("View waiting timed out")
    }

    companion object {
        /**
         * The default interval between View lookup attempts.
         */
        private const val DEFAULT_WAIT_VIEW_INTERVAL: Long = 50L

        /**
         * The default time period to wait for the View to appear.
         */
        private const val DEFAULT_WAIT_VIEW_TIMEOUT: Long = 5000L

        /**
         * Creates [WaitViewAction] with reasonable defaults.
         *
         * @param resId The View identifier.
         * @param intervalMillis The interval between View lookup attempts.
         * @param timeoutMillis The time period to wait for the View to appear.
         */
        fun waitForViewWithId(
            resId: Int,
            intervalMillis: Long = DEFAULT_WAIT_VIEW_INTERVAL,
            timeoutMillis: Long = DEFAULT_WAIT_VIEW_TIMEOUT,
        ): ViewInteraction {
            return onView(isRoot()).perform(WaitViewAction(resId, intervalMillis, timeoutMillis))
        }
    }
}
