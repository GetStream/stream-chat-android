/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.uitests.util

import com.karumi.shot.ScreenshotTest
import org.junit.runner.Description
import org.junit.runner.manipulation.Filter

/**
 * A Junit [Filter] that allows us to run only snapshot tests. Example:
 *
 * ./gradlew stream-chat-android-ui-uitests:executeScreenshotTests -Precord -Pandroid.testInstrumentationRunnerArguments.filter=io.getstream.chat.android.uitests.util.SnapshotTestFilter
 */
class SnapshotTestFilter : Filter() {
    override fun shouldRun(description: Description): Boolean = ScreenshotTest::class.java.isAssignableFrom(description.testClass)

    override fun describe(): String = "All snapshot tests."
}

/**
 * A Junit [Filter] that allows us to run all non snapshot tests. Example:
 *
 * ./gradlew stream-chat-android-ui-uitests:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.filter=io.getstream.chat.android.uitests.util.NonSnapshotTestFilter
 */
class NonSnapshotTestFilter : Filter() {
    override fun shouldRun(description: Description): Boolean = !ScreenshotTest::class.java.isAssignableFrom(description.testClass)

    override fun describe(): String = "All tests other than snapshot."
}
