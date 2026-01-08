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

package io.getstream.chat.android.client.user.storage

import io.getstream.chat.android.client.user.CredentialConfig

/**
 * Storage for [CredentialConfig].
 * SDK needs to store user credentials to restore SDK with user connected state. It is required for push notifications
 * for example. When a device receives push notification app with SDK might be killed or not run completely. SDK handles
 * it and restore state using data from [CredentialConfig].
 */
public interface UserCredentialStorage {
    /**
     * Save [credentialConfig] to this storage.
     */
    public fun put(credentialConfig: CredentialConfig)

    /**
     * Obtain [CredentialConfig] if it was stored before.
     */
    public fun get(): CredentialConfig?

    /**
     * Clear current storage.
     */
    public fun clear()
}
