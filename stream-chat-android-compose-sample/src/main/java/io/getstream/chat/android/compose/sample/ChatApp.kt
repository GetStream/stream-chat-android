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

package io.getstream.chat.android.compose.sample

import android.app.Application
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.compose.sample.data.PredefinedUserCredentials
import io.getstream.chat.android.compose.sample.data.UserCredentialsRepository

class ChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Done for simplicity, use a DI framework instead
        credentialsRepository = UserCredentialsRepository(this)
        dateFormatter = DateFormatter.from(this)

        // Initialize Stream SDK
        ChatHelper.initializeSdk(this, getApiKey())
    }

    private fun getApiKey(): String {
        return credentialsRepository.loadApiKey() ?: PredefinedUserCredentials.API_KEY
    }

    companion object {
        lateinit var credentialsRepository: UserCredentialsRepository
            private set

        lateinit var dateFormatter: DateFormatter
            private set
    }
}
