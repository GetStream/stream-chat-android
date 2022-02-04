package io.getstream.chat.ui.uitests.reactions

import androidx.fragment.app.testing.launchFragmentInContainer
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import org.junit.Test

@InternalStreamChatApi
class EditReactionsTest : ScreenshotTest {

    // @Test
    fun reactionsTestWithNoCustomization() {
        val fragmentScenario = launchFragmentInContainer<ComponentBrowserEditReactionsFragment>()
        compareScreenshot(fragmentScenario.waitForFragment())
    }
}
