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

package io.getstream.chat.ui.sample.data.user

import io.getstream.chat.android.PrivacySettings

data class SampleUser(
    val apiKey: String,
    val id: String,
    val name: String,
    val token: String,
    val image: String,
    val language: String = "",
    val privacySettings: PrivacySettings? = null,
) {

    companion object {
        val None: SampleUser = SampleUser("", "", "", "", "https://getstream.io/random_png?id=none&name=none&size=200")
    }
}
