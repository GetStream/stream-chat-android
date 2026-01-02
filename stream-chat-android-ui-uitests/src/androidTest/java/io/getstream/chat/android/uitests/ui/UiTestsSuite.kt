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

package io.getstream.chat.android.uitests.ui

import io.getstream.chat.android.uitests.ui.compose.test.ComposeChannelsTest
import io.getstream.chat.android.uitests.ui.compose.test.ComposeMessagesTest
import io.getstream.chat.android.uitests.ui.uicomponents.test.UiComponentsChannelsTest
import io.getstream.chat.android.uitests.ui.uicomponents.test.UiComponentsMessagesTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@Suite.SuiteClasses(
    ComposeChannelsTest::class,
    ComposeMessagesTest::class,
    UiComponentsChannelsTest::class,
    UiComponentsMessagesTest::class,
)
@RunWith(Suite::class)
internal class UiTestsSuite
