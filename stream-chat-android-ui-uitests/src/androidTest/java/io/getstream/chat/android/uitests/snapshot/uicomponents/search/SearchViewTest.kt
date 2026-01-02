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

package io.getstream.chat.android.uitests.snapshot.uicomponents.search

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.uitests.R
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Test

@InternalStreamChatApi
class SearchViewTest : ScreenshotTest {

    @Test
    fun testWithNoCustomizations() {
        val fragmentScenario = launchFragmentInContainer<ComponentBrowserSearchViewFragment>()
        compareScreenshot(fragmentScenario.waitForFragment())
    }

    @Test
    fun eraseContentShouldWork() {
        launchFragmentInContainer<ComponentBrowserSearchViewFragment>()

        val text = "lalalala"

        onView(getElementFromMatchAtPosition(withId(R.id.inputField), 0)).perform(typeText(text))
        onView(getElementFromMatchAtPosition(withId(R.id.inputField), 0)).check(matches(withText(text)))

        onView(getElementFromMatchAtPosition(withId(R.id.clearInputButton), 0)).perform(click())

        onView(getElementFromMatchAtPosition(withId(R.id.inputField), 0)).check(matches(withText("")))
    }

    @Test
    fun testVeryLongSearch() {
        val fragmentScenario = launchFragmentInContainer<ComponentBrowserSearchViewFragment>()

        val text = "lalalala"

        repeat(10) {
            onView(getElementFromMatchAtPosition(withId(R.id.inputField), 0)).perform(typeText(text))
        }

        compareScreenshot(fragmentScenario.waitForFragment())
    }
}

private fun getElementFromMatchAtPosition(matcher: Matcher<View>, position: Int): Matcher<View> {
    return object : BaseMatcher<View>() {
        var counter = 0
        override fun matches(item: Any?): Boolean {
            if (matcher.matches(item)) {
                if (counter == position) {
                    counter++
                    return true
                }
                counter++
            }
            return false
        }

        override fun describeTo(description: Description) {
            description.appendText("Element at hierarchy position $position")
        }
    }
}
