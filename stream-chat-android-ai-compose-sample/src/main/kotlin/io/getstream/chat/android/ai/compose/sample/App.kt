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

package io.getstream.chat.android.ai.compose.sample

import android.app.Application
import android.os.StrictMode
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.User
import io.getstream.log.AndroidStreamLogger
import io.getstream.log.streamLog

class App : Application() {

    lateinit var chatDependencies: ChatDependencies
        private set

    override fun onCreate() {
        setupStrictMode()
        super.onCreate()

        chatDependencies = ChatDependencies(
            baseUrl = "http://10.0.2.2:3000", // Android emulator localhost
            enableLogging = BuildConfig.DEBUG,
        )

        initializeStreamChat()
    }

    /**
     * initialize a global instance of the [ChatClient].
     * The ChatClient is the main entry point for all low-level operations on chat. e.g,
     * connect/disconnect user to the server, send/update/pin message, etc.
     */
    private fun initializeStreamChat() {
        AndroidStreamLogger.installOnDebuggableApp(this)

        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING
        val chatClient = ChatClient.Builder("uun7ywwamhs9", applicationContext)
            .logLevel(logLevel)
            .build()

        val user = User(
            id = "stream-user",
            name = "Stream User",
        )

        // https://getstream.io/chat/docs/php/token_generator/
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJ1c2VyX2lkIjoic3RyZWFtLXVzZXIifQ." +
            "ZG2h53Sne0kyCq5iI40ExcS0MCqDa9q-Dbc-iJ2niYU"
        chatClient.connectUser(user, token)
            .enqueue { result ->
                if (result.isFailure) {
                    streamLog { "Can't connect user. Please check the app README.md" }
                }
            }
    }
}

private fun setupStrictMode() {
    StrictMode.ThreadPolicy.Builder().detectAll()
        .penaltyLog()
        .build()
        .apply {
            StrictMode.setThreadPolicy(this)
        }

    StrictMode.VmPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .build()
        .apply {
            StrictMode.setVmPolicy(this)
        }
}
