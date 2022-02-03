package io.getstream.chat.ui.sample.feature.component_browser.search

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.ui.sample.feature.HostActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchViewMock {

    @get:Rule
    val activityRule = ActivityScenarioRule(HostActivity::class.java)

    @Test
    fun mockTest() {
        Thread.sleep(4000)
    }

}
