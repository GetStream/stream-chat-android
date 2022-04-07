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
 
package io.getstream.chat.android.ui.common

import android.content.Context
import android.os.Build
import androidx.startup.Initializer
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.getstream.sdk.chat.coil.StreamCoil
import com.getstream.sdk.chat.coil.StreamImageLoaderFactory
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.header.VersionPrefixHeader
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.internal.AvatarFetcher

/**
 * Jetpack Startup Initializer for Stream's Chat UI Components.
 */
public class ChatUIInitializer : Initializer<ChatUI> {
    override fun create(context: Context): ChatUI {
        ChatClient.VERSION_PREFIX_HEADER = VersionPrefixHeader.UI_COMPONENTS
        ChatUI.appContext = context

        val imageLoaderFactory = StreamImageLoaderFactory(context) {
            componentRegistry {
                // duplicated as we can not extend component
                // registry of existing image loader builder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder(context))
                } else {
                    add(GifDecoder())
                }

                add(AvatarFetcher())
            }
        }
        StreamCoil.setImageLoader(imageLoaderFactory)

        return ChatUI
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
