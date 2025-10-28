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

package io.getstream.chat.android.client.token

internal class FakeTokenManager(
    private var token: String,
    private val loadSyncToken: String = token,
) : TokenManager {

    private var expired = false

    override fun loadSync(): String = loadSyncToken.also {
        token = loadSyncToken
    }

    override fun getToken(): String = token

    override fun ensureTokenLoaded() {
        if (expired) {
            loadSync()
        }
        expired = false
    }

    override fun setTokenProvider(provider: CacheableTokenProvider) {
        // empty
    }

    override fun hasTokenProvider(): Boolean = true

    override fun hasToken(): Boolean = true

    override fun expireToken() {
        expired = true
    }
}
