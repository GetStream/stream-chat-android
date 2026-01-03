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

package io.getstream.chat.android.client.plugin.factory

import io.getstream.chat.android.client.plugin.DependencyResolver
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.models.User

/**
 * Interface used to add new plugins to the SDK. Use this to provide a [Plugin] that will be used to cause side effects
 * in certain API calls.
 */
public interface PluginFactory : DependencyResolver {

    /**
     * Creates a [Plugin]
     *
     * @return The [Plugin] instance.
     */
    public fun get(user: User): Plugin
}
