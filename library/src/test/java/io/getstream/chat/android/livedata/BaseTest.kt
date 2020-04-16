package io.getstream.chat.android.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.livedata.utils.TestDataHelper
import org.junit.Rule
import org.junit.runner.RunWith

open class BaseTest {

    var data = TestDataHelper()

    @get:Rule
    val rule = InstantTaskExecutorRule()
}