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
import android.util.Log
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.chat.ui.sample.data.user.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class App : Application() {

    // done for simplicity, a DI framework should be used in the real app
    val chatInitializer = ChatInitializer(this)
    val userRepository = UserRepository(this)

    private var _totalUnreadCount: MutableStateFlow<Int>? = MutableStateFlow(0)

    val totalUnreadCount: StateFlow<Int>
        get() = _totalUnreadCount!!.debounce { 5000 }
            .stateIn(GlobalScope, SharingStarted.WhileSubscribed(), _totalUnreadCount!!.value)

    override fun onCreate() {
        super.onCreate()
        initializeToggleService()
        chatInitializer.init(getApiKey())
        instance = this
        DebugMetricsHelper.init()
        ApplicationConfigurator.configureApp(this)

        _totalUnreadCount!!.value = 50
        _totalUnreadCount!!.value = 60
        _totalUnreadCount!!.value = 70
        _totalUnreadCount!!.value = 80


        GlobalScope.launch {
            totalUnreadCount.collect {
                Log.e("test3", "collected value: $it")
            }
        }
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
        ToggleService.init(
            applicationContext,
            emptyMap(),
        )
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
