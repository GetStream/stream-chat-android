#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

$DIR/start_emulator.sh

./gradlew stream-chat-android-ui-uitests:executeScreenshotTests -Precord -Pandroid.testInstrumentationRunnerArguments.filter=io.getstream.chat.android.uitests.util.SnapshotTestFilter

$DIR/stop_emulator.sh