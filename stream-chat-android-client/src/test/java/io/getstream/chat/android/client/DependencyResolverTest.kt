/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.errorhandler.ErrorHandler
import io.getstream.chat.android.client.plugin.DependencyResolver
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.User
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.invoking
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.reflect.KClass

public class DependencyResolverTest {

    @Test
    public fun `Should throw an exception if plugin was not found`(): TestResult = runTest {
        val client = Fixture()
            .get()

        invoking {
            client.resolveDependency<PluginDependency, SomeDependency>()
        }
            .`should throw`(IllegalStateException::class)
            .`with message`("Plugin 'io.getstream.chat.android.client.DependencyResolverTest.PluginDependency' was not found. Did you init it within ChatClient?")
    }

    @Test
    public fun `Should throw an exception if dependency was not found`(): TestResult = runTest {
        val client = Fixture()
            .with(PluginDependency(emptyMap()))
            .get()

        invoking {
            client.resolveDependency<PluginDependency, SomeDependency>()
        }
            .`should throw`(IllegalStateException::class)
            .`with message`("Dependency 'io.getstream.chat.android.client.DependencyResolverTest.SomeDependency' was not resolved from plugin 'io.getstream.chat.android.client.DependencyResolverTest.PluginDependency'")
    }

    @Test
    public fun `Should return expected dependency`(): TestResult = runTest {
        val expectedDependency = SomeDependency()
        val client = Fixture()
            .with(PluginDependency(mapOf(SomeDependency::class to expectedDependency)))
            .get()

        val result = client.resolveDependency<PluginDependency, SomeDependency>()

        result `should be` expectedDependency
    }

    private class Fixture {
        var plugins: List<Plugin> = emptyList()

        fun with(plugin: Plugin) = apply {
            plugins = plugins + plugin
        }

        suspend fun get(): ChatClient = ChatClient(
            config = mock(),
            api = mock(),
            notifications = mock(),
            tokenManager = mock(),
            userCredentialStorage = mock(),
            userStateService = mock(),
            tokenUtils = mock(),
            clientScope = mock(),
            userScope = mock(),
            retryPolicy = mock(),
            appSettingsManager = mock(),
            chatSocket = mock(),
            pluginFactories = mock(),
            repositoryFactoryProvider = mock(),
            clientState = mock(),
        ).apply {
            this.plugins = this@Fixture.plugins
        }
    }

    private class PluginDependency(
        private val classes: Map<KClass<*>, Any>,
    ) : Plugin, DependencyResolver {
        override val errorHandler: ErrorHandler? = null
        override fun onUserSet(user: User) {
            /** NO-OP */
        }

        override fun onUserDisconnected() {
            /** NO-OP */
        }

        @InternalStreamChatApi
        override fun <T : Any> resolveDependency(klass: KClass<T>): T? =
            classes[klass] as? T
    }

    private class SomeDependency
}
