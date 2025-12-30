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

package io.getstream.chat.android.internal.offline.repository

import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.internal.offline.repository.domain.channelconfig.internal.ChannelConfigDao
import io.getstream.chat.android.internal.offline.repository.domain.channelconfig.internal.ChannelConfigEntity
import io.getstream.chat.android.internal.offline.repository.domain.channelconfig.internal.ChannelConfigInnerEntity
import io.getstream.chat.android.internal.offline.repository.domain.channelconfig.internal.DatabaseChannelConfigRepository
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannelConfig
import io.getstream.chat.android.randomConfig
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class ChannelConfigRepositoryTest {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private lateinit var dao: ChannelConfigDao
    private lateinit var sut: ChannelConfigRepository

    @Before
    fun before() {
        dao = mock()
        sut = DatabaseChannelConfigRepository(dao)
    }

    @Test
    fun `When insert channel config Should store this value in DB`() = runTest {
        sut.insertChannelConfig(randomChannelConfig(type = "messaging", config = randomConfig(name = "configName")))

        verify(dao).insert(
            argThat<ChannelConfigEntity> {
                channelConfigInnerEntity.channelType == "messaging" &&
                    channelConfigInnerEntity.name == "configName"
            },
        )
    }

    @Test
    fun `Given inserted channel When select config Should get config from cache`() = runTest {
        sut.insertChannelConfig(randomChannelConfig(type = "messaging", config = randomConfig(name = "configName")))

        val result = sut.selectChannelConfig("messaging")

        result.shouldNotBeNull()
        result.config.name shouldBeEqualTo "configName"
    }

    @Test
    fun `When insert configs Should store these values in DB`() = runTest {
        val config1 = randomChannelConfig(type = "messaging1", config = randomConfig(name = "configName1"))
        val config2 = randomChannelConfig(type = "messaging2", config = randomConfig(name = "configName2"))

        sut.insertChannelConfigs(listOf(config1, config2))

        verify(dao).insert(
            argThat<List<ChannelConfigEntity>> {
                size == 2 &&
                    any {
                        it.channelConfigInnerEntity.channelType == "messaging1" &&
                            it.channelConfigInnerEntity.name == "configName1"
                    } &&
                    any {
                        it.channelConfigInnerEntity.channelType == "messaging2" &&
                            it.channelConfigInnerEntity.name == "configName2"
                    }
            },
        )
    }

    @Test
    fun `Given config in cache When select Should return config`() = runTest {
        val config = randomChannelConfig(type = "messaging", config = randomConfig(name = "configName"))
        sut.insertChannelConfig(config)

        val result = sut.selectChannelConfig("messaging")

        result shouldBeEqualTo config
    }

    @Test
    fun `Given DB with saved config When cache configs Should load them fromDB`() = runTest {
        val firstConfigEntity = createChannelConfigEntity("type1", "name1")
        val secondConfigEntity = createChannelConfigEntity("type2", "name2")
        whenever(dao.selectAll()) doReturn listOf(firstConfigEntity, secondConfigEntity)

        sut.cacheChannelConfigs()

        sut.selectChannelConfig("type1")!!.config.name shouldBeEqualTo "name1"
        sut.selectChannelConfig("type2")!!.config.name shouldBeEqualTo "name2"
    }

    private fun createChannelConfigEntity(type: String, name: String): ChannelConfigEntity {
        return ChannelConfigEntity(
            ChannelConfigInnerEntity(
                channelType = type,
                name = name,
                createdAt = randomDate(),
                updatedAt = randomDate(),
                automod = randomString(),
                automodBehavior = randomString(),
                blocklistBehavior = randomString(),
                customEventsEnabled = randomBoolean(),
                isConnectEvents = randomBoolean(),
                isMutes = randomBoolean(),
                isReactionsEnabled = randomBoolean(),
                isReadEvents = randomBoolean(),
                deliveryEventsEnabled = randomBoolean(),
                isSearch = randomBoolean(),
                isThreadEnabled = randomBoolean(),
                isTypingEvents = randomBoolean(),
                maxMessageLength = randomInt(),
                messageRetention = randomString(),
                pushNotificationsEnabled = randomBoolean(),
                uploadsEnabled = randomBoolean(),
                urlEnrichmentEnabled = randomBoolean(),
                messageRemindersEnabled = randomBoolean(),
                markMessagesPending = randomBoolean(),
            ),
            emptyList(),
        )
    }
}
