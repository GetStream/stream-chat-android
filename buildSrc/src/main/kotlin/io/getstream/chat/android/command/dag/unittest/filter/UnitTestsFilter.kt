package io.getstream.chat.android.command.dag.unittest.filter

import io.getstream.chat.android.command.dag.generateGradleCommand

fun List<String>.unitTestCommand(): String = filterUnitTestableModules().generateGradleCommand { "testDebug" }

private fun List<String>.filterUnitTestableModules(): List<String> =
    filter(optInUnitTestModules()::contains)

private fun optInUnitTestModules() = listOf(
    "stream-chat-android",
    "stream-chat-android-client",
    "stream-chat-android-offline",
    "stream-chat-android-ui-common",
    "stream-chat-android-ui-components",
)
