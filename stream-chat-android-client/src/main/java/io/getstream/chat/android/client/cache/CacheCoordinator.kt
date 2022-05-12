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

package io.getstream.chat.android.client.cache

import io.getstream.chat.android.client.call.Call

/**
 * Interface for classes responsible for caching [Call]s
 */
public interface CacheCoordinator {

    /**
     * Caches the call and returns it. If another call is made too soon, the cached call is returns.
     */
    public fun <T : Any> cachedCall(hashCode: Int, forceRefresh: Boolean, call: Call<T>): Call<T>
}
