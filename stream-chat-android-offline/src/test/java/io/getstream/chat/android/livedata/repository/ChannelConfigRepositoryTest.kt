package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.livedata.model.ChannelConfig
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelConfigRepositoryTest : BaseDomainTest2() {
    private val repositoryFacade by lazy { chatDomainImpl.repos }

    @Test
    fun testInsertAndRead() = runBlocking {
        repositoryFacade.insertChannelConfig(ChannelConfig("messaging", data.config1))
        val config = repositoryFacade.selectChannelConfig("messaging")
        Truth.assertThat(config).isEqualTo(config)
    }

    @Test
    fun testLoadAndRead() = runBlocking {
        repositoryFacade.insertChannelConfig(ChannelConfig("messaging", data.config1))
        repositoryFacade.clearChannelConfigsCache()
        var config = repositoryFacade.selectChannelConfig("messaging")
        Truth.assertThat(config).isNull()
        repositoryFacade.cacheChannelConfigs()
        config = repositoryFacade.selectChannelConfig("messaging")
        Truth.assertThat(config?.config).isEqualTo(data.config1)
    }

    @Test
    fun testUpdate() = runBlocking {
        val config1 = data.config1
        repositoryFacade.insertChannelConfig(ChannelConfig("messaging", config1))
        val config2 = data.config1.copy(maxMessageLength = 200)
        repositoryFacade.insertChannelConfig(ChannelConfig("messaging", config2))

        repositoryFacade.clearChannelConfigsCache()
        repositoryFacade.cacheChannelConfigs()

        val config = repositoryFacade.selectChannelConfig("messaging")?.config
        Truth.assertThat(config).isEqualTo(config2)
        Truth.assertThat(config!!.maxMessageLength).isEqualTo(200)
    }
}
