package io.getstream.chat.android.client.utils

import androidx.test.platform.app.InstrumentationRegistry

class Utils {
    companion object {
        fun runOnUi(call: () -> Unit): ThenVerify {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                call()
            }
            return ThenVerify()
        }
    }

    class ThenVerify {
        fun andThen(call: () -> Unit) {
            call()
        }
    }
}
