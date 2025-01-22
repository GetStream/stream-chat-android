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

import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.setup.state.internal.MutableClientState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.invoking
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.reflect.KClass

public class DependencyResolverTest {

    @Test
    public fun `Should throw an exception if plugin was not found`(): TestResult = runTest {
        val client = Fixture()
            .with(InitializationState.COMPLETE)
            .get()

        invoking {
            client.resolveDependency<PluginDependency, SomeDependency>()
        }
            .`should throw`(IllegalStateException::class)
            .`with message`("Plugin 'io.getstream.chat.android.client.DependencyResolverTest.PluginDependency' was not found. Did you init it within ChatClient?")

        invoking {
            client.resolveDependency<FactoryDependency, SomeDependency>()
        }
            .`should throw`(IllegalStateException::class)
            .`with message`("Factory 'io.getstream.chat.android.client.DependencyResolverTest.FactoryDependency' was not found. Did you init it within ChatClient?")
    }

    @Test
    public fun `Should throw an exception if dependency was not found`(): TestResult = runTest {
        val client = Fixture()
            .with(InitializationState.COMPLETE)
            .with(PluginDependency(emptyMap()))
            .with(FactoryDependency(emptyMap()))
            .get()

        invoking {
            client.resolveDependency<PluginDependency, SomeDependency>()
        }
            .`should throw`(IllegalStateException::class)
            .`with message`("Dependency 'io.getstream.chat.android.client.DependencyResolverTest.SomeDependency' was not resolved by plugin 'io.getstream.chat.android.client.DependencyResolverTest.PluginDependency'")

        invoking {
            client.resolveDependency<FactoryDependency, SomeDependency>()
        }
            .`should throw`(IllegalStateException::class)
            .`with message`("Dependency 'io.getstream.chat.android.client.DependencyResolverTest.SomeDependency' was not resolved by factory 'io.getstream.chat.android.client.DependencyResolverTest.FactoryDependency'")
    }

    /** This method use [initializationStatesArguments] as a source of arguments. */
    @ParameterizedTest
    @MethodSource("initializationStatesArguments")
    public fun `Should throw an exception if user is not connected`(
        initializationState: InitializationState,
    ): TestResult = runTest {
        val client = Fixture()
            .with(initializationState)
            .with(PluginDependency(emptyMap()))
            .with(FactoryDependency(emptyMap()))
            .get()

        invoking {
            client.resolveDependency<PluginDependency, SomeDependency>()
        }
            .`should throw`(IllegalStateException::class)
            .`with message`("ChatClient::connectUser() must be called before resolving any dependency")

        invoking {
            client.resolveDependency<FactoryDependency, SomeDependency>()
        }
            .`should throw`(IllegalStateException::class)
            .`with message`("Dependency 'io.getstream.chat.android.client.DependencyResolverTest.SomeDependency' was not resolved by factory 'io.getstream.chat.android.client.DependencyResolverTest.FactoryDependency'")
    }

    @Test
    public fun `Should return expected dependency`(): TestResult = runTest {
        val expectedDependency = SomeDependency()
        val client = Fixture()
            .with(InitializationState.COMPLETE)
            .with(PluginDependency(mapOf(SomeDependency::class to expectedDependency)))
            .with(FactoryDependency(mapOf(SomeDependency::class to expectedDependency)))
            .get()

        val pResult = client.resolveDependency<PluginDependency, SomeDependency>()
        val fResult = client.resolveDependency<FactoryDependency, SomeDependency>()

        pResult `should be` expectedDependency
        fResult `should be` expectedDependency
    }

    public companion object {

        @JvmField
        @RegisterExtension
        public val testCoroutines: TestCoroutineExtension = TestCoroutineExtension()

        @JvmStatic
        public fun initializationStatesArguments(): List<Arguments> =
            InitializationState.values()
                .filterNot { it == InitializationState.COMPLETE }
                .map { Arguments.of(it) }
    }

    private class Fixture {
        var plugins: MutableList<Plugin> = arrayListOf()
        var pluginFactories: MutableList<PluginFactory> = arrayListOf()
        val mutableClientState: MutableClientState = mock()
        val clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)

        fun with(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun with(factory: PluginFactory) = apply {
            pluginFactories.add(factory)
        }

        fun with(state: InitializationState) = apply {
            whenever(mutableClientState.initializationState).thenReturn(MutableStateFlow(state))
        }

        suspend fun get(): ChatClient = ChatClient(
            config = mock(),
            api = mock(),
            dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer),
            notifications = mock(),
            tokenManager = mock(),
            userCredentialStorage = mock(),
            userStateService = mock(),
            tokenUtils = mock(),
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = mock(),
            appSettingsManager = mock(),
            chatSocket = mock(),
            pluginFactories = pluginFactories,
            repositoryFactoryProvider = mock(),
            mutableClientState = mutableClientState,
            currentUserFetcher = mock(),
            audioPlayer = mock(),
        ).apply {
            this.plugins = this@Fixture.plugins
        }
    }

    private class PluginDependency(
        private val classes: Map<KClass<*>, Any>,
    ) : Plugin {
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

    private class FactoryDependency(
        private val classes: Map<KClass<*>, Any>,
    ) : PluginFactory {
        override fun get(user: User): Plugin {
            /** NO-OP */
            TODO("not implemented")
        }

        @InternalStreamChatApi
        override fun <T : Any> resolveDependency(klass: KClass<T>): T? =
            classes[klass] as? T
    }

    private class SomeDependency
}
