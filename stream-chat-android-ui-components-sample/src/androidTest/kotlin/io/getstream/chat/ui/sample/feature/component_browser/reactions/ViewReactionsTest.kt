package io.getstream.chat.ui.sample.feature.component_browser.reactions

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.SupportedReactions
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


        Thread.sleep(5000)
        compareScreenshot(fragmentScenario.waitForFragment())
    }

    private fun manyReactions(): Map<String, Int>{
        return mutableMapOf(
            SupportedReactions.DefaultReactionTypes.LOVE to 10,
            SupportedReactions.DefaultReactionTypes.WUT to 20,
            SupportedReactions.DefaultReactionTypes.LOL to 20,
            SupportedReactions.DefaultReactionTypes.THUMBS_UP to 20,
            "sad"  to 20,
        )
    }

    private fun unsupportedReactions(): Map<String, Int>{
        return mutableMapOf(
            "notSupported1" to 10,
            "notSupported2" to 10,
            "notSupported3" to 10,
            "notSupported4" to 10,
            "notSupported5" to 10,
        )
    }
}
