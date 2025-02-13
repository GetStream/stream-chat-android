package io.getstream.chat.android.compose.tests

import io.getstream.chat.android.compose.sample.ui.InitTestActivity
import org.junit.Test

class AuthTests : StreamTestCase() {
    override fun initTestActivity(): InitTestActivity = InitTestActivity.Jwt

    @Test
    fun testAuth() {

    }
}