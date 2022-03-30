package io.getstream.logging

import io.getstream.logging.kotlin.KotlinStreamLogger
import org.junit.BeforeClass
import org.junit.Test

private const val TAG = "TestStreamLog"

public class TestStreamLog {

    @BeforeClass
    public fun setUp() {
        StreamLog.init(KotlinStreamLogger())
    }

    @Test
    public fun testLog() {
        StreamLog.d(TAG, "[testLog] param1: %d, param2: %s", 1, "2")
    }
}
