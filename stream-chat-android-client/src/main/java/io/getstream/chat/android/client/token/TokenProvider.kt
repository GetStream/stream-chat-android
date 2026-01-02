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

package io.getstream.chat.android.client.token

import androidx.annotation.WorkerThread

/**
 * Provides a token used to authenticate the user with Stream Chat API.
 * The SDK doesn't refresh the token internally and will call
 * [loadToken] function once the previous one has expired.
 *
 * Check out [docs](https://getstream.io/chat/docs/android/init_and_users/) for more info about tokens.
 */
public interface TokenProvider {

    /**
     * Loads the token for the current user.
     * The token will be loaded only if the token was not loaded yet or existing one has expired.
     * If the token cannot be loaded, returns an empty string and never throws an exception.
     *
     * @return The valid JWT token.
     */
    @WorkerThread
    public fun loadToken(): String
}
