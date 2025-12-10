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
import android.os.StrictMode
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.chat.ui.sample.data.user.UserRepository

class App : Application() {

    // done for simplicity, a DI framework should be used in the real app
    val chatInitializer = ChatInitializer(context = this, autoTranslationEnabled = false)
    val userRepository = UserRepository(context = this)

    override fun onCreate() {
        super.onCreate()
        chatInitializer.init(getApiKey())
        instance = this
        enableStrictMode()
    }

    private fun enableStrictMode() {
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
        StrictMode.setThreadPolicy(threadPolicy)

        val vmPolicy = StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
        StrictMode.setVmPolicy(vmPolicy)
    }

    private fun getApiKey(): String {
        val user = userRepository.getUser()
        return if (user != SampleUser.None) {
            user.apiKey
        } else {
            AppConfig.apiKey
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
