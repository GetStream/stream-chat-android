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

package io.getstream.chat.android.uitests.snapshot.compose

import androidx.compose.runtime.Composable
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uitests.util.FakeImageLoader

/**
 * A wrapper for [ChatTheme] that provides a fake Coil [ImageLoader].
 */
@Composable
fun TestChatTheme(content: @Composable () -> Unit) {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    ChatTheme(
        imageLoaderFactory = { FakeImageLoader(context) },
        content = content
    )
}
