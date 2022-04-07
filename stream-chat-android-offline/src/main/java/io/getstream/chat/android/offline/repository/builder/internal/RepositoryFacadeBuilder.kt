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

package io.getstream.chat.android.offline.repository.builder.internal

import android.content.Context
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistence.repository.factory.RepositoryFactory
import kotlinx.coroutines.CoroutineScope

internal class RepositoryFacadeBuilder {
    companion object {
        operator fun invoke(builderAction: RepositoryFacadeBuilder.() -> Unit): RepositoryFacadeBuilder {
            return RepositoryFacadeBuilder().apply(builderAction)
        }
    }

    private var context: Context? = null
    private var currentUser: User? = null
    private var coroutineScope: CoroutineScope? = null
    private var defaultConfig: Config? = null
    private var repositoryFactory: RepositoryFactory? = null

    fun context(context: Context): RepositoryFacadeBuilder = apply { this.context = context }
    fun currentUser(user: User): RepositoryFacadeBuilder = apply { this.currentUser = user }
    fun scope(scope: CoroutineScope): RepositoryFacadeBuilder = apply { this.coroutineScope = scope }
    fun defaultConfig(config: Config): RepositoryFacadeBuilder = apply { this.defaultConfig = config }
    fun repositoryFactory(repositoryFactory: RepositoryFactory): RepositoryFacadeBuilder = apply {
        this.repositoryFactory = repositoryFactory
    }

    fun build(): RepositoryFacade {
        val config = requireNotNull(defaultConfig)
        val scope = requireNotNull(coroutineScope)
        val factory = requireNotNull(repositoryFactory)

        return RepositoryFacade.create(factory, scope, config)
    }
}
