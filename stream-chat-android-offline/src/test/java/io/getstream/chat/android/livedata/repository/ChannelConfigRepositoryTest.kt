package io.getstream.chat.android.livedata.repository

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.livedata.randomChannelConfig
import io.getstream.chat.android.livedata.randomConfig
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigDao
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigEntity
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigInnerEntity
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.ChannelConfigRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
internal class ChannelConfigRepositoryTest {

    private lateinit var dao: ChannelConfigDao
    private lateinit var sut: ChannelConfigRepository

    @Before
    fun before() {
        dao = mock()
        sut = ChannelConfigRepositoryImpl(dao)
    }

    @Test
    fun `When insert channel config Should store this value in DB`() = runBlockingTest {
        sut.insertChannelConfig(randomChannelConfig(type = "messaging", config = randomConfig(name = "configName")))

        verify(dao).insert(
            argThat<ChannelConfigEntity> {
                channelConfigInnerEntity.channelType == "messaging" &&
                    channelConfigInnerEntity.name == "configName"
            }
        )
    }

    @Test
    fun `Given inserted channel When select config Should get config from cache`() = runBlockingTest {
        sut.insertChannelConfig(randomChannelConfig(type = "messaging", config = randomConfig(name = "configName")))

        val result = sut.selectChannelConfig("messaging")

        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result!!.config.name).isEqualTo("configName")
    }

    @Test
    fun `When insert configs Should store these values in DB`() = runBlockingTest {
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
            }
        )
    }

    @Test
    fun `Given config in cache When select Should return config`() = runBlockingTest {
        val config = randomChannelConfig(type = "messaging", config = randomConfig(name = "configName"))
        sut.insertChannelConfig(config)

        val result = sut.selectChannelConfig("messaging")

        Truth.assertThat(result).isEqualTo(config)
    }

    @Test
    fun `Given DB with saved config When cache configs Should load them fromDB`() = runBlockingTest {
        val firstConfigEntity = createChannelConfigEntity("type1", "name1")
        val secondConfigEntity = createChannelConfigEntity("type2", "name2")
        whenever(dao.selectAll()) doReturn listOf(firstConfigEntity, secondConfigEntity)

        sut.cacheChannelConfigs()

        Truth.assertThat(sut.selectChannelConfig("type1")!!.config.name).isEqualTo("name1")
        Truth.assertThat(sut.selectChannelConfig("type2")!!.config.name).isEqualTo("name2")
    }

    private fun createChannelConfigEntity(type: String, name: String): ChannelConfigEntity {
        return ChannelConfigEntity(
            KFixture(JFixture())<ChannelConfigInnerEntity>().copy(
                channelType = type,
                name = name
            ),
            emptyList()
        )
    }
}
