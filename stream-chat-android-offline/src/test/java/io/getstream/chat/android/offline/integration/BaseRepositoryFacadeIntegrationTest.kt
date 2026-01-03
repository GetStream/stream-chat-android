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

package io.getstream.chat.android.offline.integration

import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.offline.createRoomDB
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.factory.internal.DatabaseRepositoryFactory
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule

@ExperimentalCoroutinesApi
internal open class BaseRepositoryFacadeIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private lateinit var chatDatabase: ChatDatabase

    protected val currentUser = randomUser()
    protected lateinit var repositoryFacade: RepositoryFacade

    @Before
    @CallSuper
    open fun setup() {
        chatDatabase = createRoomDB()
        repositoryFacade = RepositoryFacade.create(
            DatabaseRepositoryFactory(chatDatabase, currentUser, testCoroutines.scope, emptySet()),
            testCoroutines.scope,
        )
    }

    @After
    fun tearDown() {
        chatDatabase.close()
    }
}
