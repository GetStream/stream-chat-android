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

package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import io.getstream.chat.android.compose.sample.ui.login.UserLoginActivity
import java.io.Serializable

sealed class InitTestActivity : Serializable {
    abstract fun createIntent(context: Context): Intent

    data object UserLogin : InitTestActivity() {
        private fun readResolve(): Any = UserLogin
        override fun createIntent(context: Context): Intent =
            UserLoginActivity.createIntent(context)
    }

    data class Jwt(val baseUrl: String) : InitTestActivity() {
        override fun createIntent(context: Context): Intent {
            val intent = JwtTestActivity.createIntent(context)
            intent.putExtra("BASE_URL", baseUrl)
            return intent
        }
    }
}
