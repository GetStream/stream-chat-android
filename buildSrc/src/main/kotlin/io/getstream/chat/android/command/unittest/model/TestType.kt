package io.getstream.chat.android.command.unittest.model

enum class TestType(val testCommand: String) {
    JAVA_LIBRARY_TEST("test"),
    ANDROID_LIBRARY_TEST("testDebugUnitTest"),
    COMPOSE_LIBRARY_TEST("verifyPaparazziDebug"),
    JACOCO_TEST_COVERAGE("testCoverage"),
}
