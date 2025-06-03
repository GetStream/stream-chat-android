/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * A composable that allows you to create a compound [ChatComponentFactory] by providing a
 * factory function that takes the current [ChatComponentFactory] as an argument.
 *
 * This is useful for creating custom components or modifying existing ones
 * without directly altering the original factory.
 *
 * @param keys Optional keys to control recomposition. If any of the keys change,
 * the [factory] function will be called again to create a new compound [ChatComponentFactory].
 * @param factory A function that takes the current [ChatComponentFactory] and returns a new [ChatComponentFactory].
 * @param content The composable content that will use the compound [ChatComponentFactory].
 */
@ExperimentalStreamChatApi
@Composable
public fun CompoundComponentFactory(
    vararg keys: Any?,
    factory: (currentComponentFactory: ChatComponentFactory) -> ChatComponentFactory,
    content: @Composable () -> Unit,
) {
    val currentComponentFactory = LocalComponentFactory.current
    val compoundComponentFactory = remember(keys) { factory(currentComponentFactory) }
    CompositionLocalProvider(
        value = LocalComponentFactory provides compoundComponentFactory,
        content = content,
    )
}
