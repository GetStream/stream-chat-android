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

package io.getstream.chat.android.uitests.ui.compose.robot

import androidx.compose.ui.test.junit4.ComposeTestRule
import org.junit.rules.TestRule

/**
 * A base class for all user robots.
 *
 * @param composeTestRule A [TestRule] that provides the main entry point into testing.
 */
internal open class BaseComposeTestRobot(
    protected val composeTestRule: ComposeTestRule,
)
