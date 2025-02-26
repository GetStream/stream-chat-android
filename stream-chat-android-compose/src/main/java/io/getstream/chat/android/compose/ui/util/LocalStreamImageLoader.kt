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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.imageLoader

/**
 * A [CompositionLocal] that returns the current [ImageLoader] for the composition.
 * If a local [ImageLoader] has not been provided, it returns the singleton instance of [ImageLoader] in [Coil].
 */
public val LocalStreamImageLoader: StreamImageLoaderProvidableCompositionLocal =
    StreamImageLoaderProvidableCompositionLocal()

/**
 * A provider of [CompositionLocal] that returns the current [ImageLoader] for the composition.
 */
@JvmInline
public value class StreamImageLoaderProvidableCompositionLocal internal constructor(
    private val delegate: ProvidableCompositionLocal<ImageLoader?> = staticCompositionLocalOf { null },
) {

    public val current: ImageLoader
        @Composable
        @ReadOnlyComposable
        get() = delegate.current ?: LocalContext.current.imageLoader

    public infix fun provides(value: ImageLoader): ProvidedValue<ImageLoader?> = delegate provides value
}
