/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.uitests.snapshot.compose

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uitests.util.FakeImageLoader
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.runner.RunWith

/**
 * A base class used for all the Compose snapshot tests.
 */
@RunWith(TestParameterInjector::class)
abstract class ComposeScreenshotTest : ScreenshotTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val composeRule = createComposeRule()

    @TestParameter
    private var isDarkMode: Boolean = false

    @get:Rule
    val testNameRule = TestName()

    /**
     * Renders a Composable [content] and records or verifies the screenshot.
     *
     * @param content The composable content that will be rendered.
     */
    fun runScreenshotTest(content: @Composable () -> Unit) {
        composeRule.setContent {
            ChatTheme(
                isInDarkMode = isDarkMode,
                imageLoaderFactory = { FakeImageLoader(context) },
                content = {
                    if (isDarkMode) {
                        Box(modifier = Modifier.background(Color.Black)) {
                            content()
                        }
                    } else {
                        content()
                    }
                },
            )
        }
        compareScreenshot(rule = composeRule, name = generateName())
    }

    /**
     * Generates a name for the screenshot.
     */
    private fun generateName(): String {
        return testNameRule.methodName
            .substringBefore('[')
            .let { if (isDarkMode) "${it}_dark" else it }
    }
}
