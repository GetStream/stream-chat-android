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

package io.getstream.chat.ui.sample.application

import android.app.Application
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.chat.ui.sample.data.user.UserRepository

class App : Application() {

    // done for simplicity, a DI framework should be used in the real app
    val chatInitializer = ChatInitializer(this)
    val userRepository = UserRepository(this)

    override fun onCreate() {
        super.onCreate()
        chatInitializer.init(getApiKey())
        instance = this
        DebugMetricsHelper.init()
        Coil.setImageLoader(
            ImageLoader.Builder(this).componentRegistry {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder(this@App))
                } else {
                    add(GifDecoder())
                }
            }.build()
        )
        ApplicationConfigurator.configureApp(this)
        initializeToggleService()
    }

    private fun getApiKey(): String {
        val user = userRepository.getUser()
        return if (user != SampleUser.None) {
            user.apiKey
        } else {
            AppConfig.apiKey
        }
    }

    @OptIn(InternalStreamChatApi::class)
    private fun initializeToggleService() {
        ToggleService.init(applicationContext, emptyMap())
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
