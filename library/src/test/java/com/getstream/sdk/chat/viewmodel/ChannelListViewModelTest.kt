package com.getstream.sdk.chat.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class ChannelListViewModelTest {
    @get:Rule
    var liveDataRule = InstantTaskExecutorRule()

    @Test
    fun `dummy test`() {
        assertFalse(2+2 == 5)
    }

}