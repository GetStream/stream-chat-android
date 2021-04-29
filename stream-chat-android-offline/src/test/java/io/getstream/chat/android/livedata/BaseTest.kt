package io.getstream.chat.android.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.getstream.chat.android.offline.utils.TestDataHelper
import org.junit.Rule

internal open class BaseTest {

    var data = TestDataHelper()

    @get:Rule
    val rule = InstantTaskExecutorRule()
}
