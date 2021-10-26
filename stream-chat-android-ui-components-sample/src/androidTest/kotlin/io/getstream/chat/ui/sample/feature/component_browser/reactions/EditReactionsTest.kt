package io.getstream.chat.ui.sample.feature.component_browser.reactions

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.ui.sample.feature.HostActivity
import io.getstream.chat.ui.sample.feature.component_browser.reactions.ComponentBrowserEditReactionsFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@InternalStreamChatApi
class EditReactionsTest : ScreenshotTest {

    @Test
    fun reactionsTestWithNoCustomization() {
        val fragmentScenario = launchFragmentInContainer<ComponentBrowserEditReactionsFragment>()
        compareScreenshot(fragmentScenario.waitForFragment())
    }
}
