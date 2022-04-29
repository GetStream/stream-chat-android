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

package io.getstream.chat.android.client.persistance.repository.factory

/**
 * Provider of repositories. This is singleton that holds a [RepositoryFactory] and ensures the SDK uses only one
 * instance of it.
 */
public class RepositoryProvider private constructor(
    private val repositoryFactory: RepositoryFactory,
) : RepositoryFactory by repositoryFactory {

    public companion object {

        private var instance: RepositoryProvider? = null

        public fun changeRepositoryFactory(repositoryFactory: RepositoryFactory) {
            instance = RepositoryProvider(repositoryFactory)
        }

        public fun get(): RepositoryProvider = instance ?: throw IllegalStateException(
            "RepositoryProvider is not correctly configured"
        )
    }
}
