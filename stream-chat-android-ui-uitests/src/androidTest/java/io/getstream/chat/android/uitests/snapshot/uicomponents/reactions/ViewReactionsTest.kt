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

package io.getstream.chat.android.uitests.snapshot.uicomponents.reactions

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.helper.SupportedReactions
import org.junit.Test

@InternalStreamChatApi
class ViewReactionsTest : ScreenshotTest {

    @Test
    fun viewReactionsWithNoCustomization() {
        val fragmentScenario = launchFragmentInContainer<ComponentBrowserViewReactionsFragment>()
        compareScreenshot(fragmentScenario.waitForFragment())
    }

    @Test
    fun viewReactionsWithFiveReactions() {
        testWithReactions(manyReactions())
    }

    @Test
    fun viewReactionsWithOnlyUnsupportedReactions() {
        testWithReactions(unsupportedReactions())
    }

    @Test
    fun viewReactionsWithSomeUnsupportedReactions() {
        testWithReactions(manyReactions() + unsupportedReactions())
    }

    private fun testWithReactions(reactions: Map<String, Int>) {
        val fragmentArgs = bundleOf(CUSTOM_REACTIONS to reactions)
        val fragmentScenario = launchFragmentInContainer<ComponentBrowserViewReactionsFragment>(fragmentArgs)

        compareScreenshot(fragmentScenario.waitForFragment())
    }

    private fun manyReactions(): Map<String, Int> = mutableMapOf(
        SupportedReactions.DefaultReactionTypes.LOVE to 10,
        SupportedReactions.DefaultReactionTypes.WUT to 20,
        SupportedReactions.DefaultReactionTypes.LOL to 20,
        SupportedReactions.DefaultReactionTypes.THUMBS_UP to 20,
        "sad" to 20,
    )

    private fun unsupportedReactions(): Map<String, Int> = mutableMapOf(
        "notSupported1" to 10,
        "notSupported2" to 10,
        "notSupported3" to 10,
        "notSupported4" to 10,
        "notSupported5" to 10,
    )
}
