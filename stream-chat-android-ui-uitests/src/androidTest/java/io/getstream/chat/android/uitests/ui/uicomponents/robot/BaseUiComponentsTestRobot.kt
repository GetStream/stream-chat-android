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
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import io.getstream.chat.android.uitests.ui.uicomponents.action.WaitViewAction

/**
 * A base class for all user robots.
 */
internal open class BaseUiComponentsTestRobot {

    /**
     * Performs a click on a View with the specified ID.
     *
     * @param resId The layout ID of a view to click.
     */
    fun clickElementById(resId: Int): ViewInteraction {
        return onView(withId(resId)).perform(ViewActions.click())
    }

    /**
     * Waits for the View to appear and performs a click.
     *
     * @param resId The layout ID of a view to click.
     */
    fun clickElementByIdWithDelay(resId: Int): ViewInteraction {
        waitForViewWithId(resId)
        return clickElementById(resId)
    }

    /**
     * Assert that an element is displayed on the screen.
     *
     * @param resId The layout ID of a view to check.
     */
    fun assertElementWithIdIsDisplayed(resId: Int): ViewInteraction {
        waitForViewWithId(resId)
        return onView(withId(resId)).check(matches(isDisplayed()))
    }

    /**
     * Wait for the View to appear on the screen.
     *
     * @param resId The layout ID of a view to check.
     */
    fun waitForViewWithId(resId: Int) {
        WaitViewAction.waitForViewWithId(resId)
    }
}
