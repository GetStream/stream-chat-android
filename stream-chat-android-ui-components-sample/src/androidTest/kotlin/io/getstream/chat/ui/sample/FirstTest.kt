package io.getstream.chat.ui.sample

import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.getstream.chat.ui.sample.feature.HostActivity
import org.junit.Rule
import org.junit.Test

class FirstTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<HostActivity> = ActivityScenarioRule(HostActivity::class.java)

    @Test
    fun firstTest() {
        Thread.sleep(10000)
    }
}
