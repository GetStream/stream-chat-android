#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

$DIR/emulator_start.sh

./gradlew stream-chat-android-ui-uitests:executeScreenshotTests -Pandroid.testInstrumentationRunnerArguments.filter=io.getstream.chat.android.uitests.util.SnapshotTestFilter

adb -s emulator-5554 emu kill