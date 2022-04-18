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

package io.getstream.chat.android.uitests.ui.robot

import io.getstream.chat.android.uitests.R

/**
 * A factory function for [LoginRobot].
 */
fun loginRobot(func: LoginRobot.() -> Unit) = LoginRobot().apply { func() }

/**
 * A robot that simulates user behavior on the login screen.
 */
class LoginRobot : BaseTestRobot() {

    /**
     * Clicks on the login button that should take the user to the channels screen.
     */
    fun clickLoginButton() {
        clickElementById(R.id.loginButton)
    }
}
