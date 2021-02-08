package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.livedata.model.ChannelConfig
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelConfigRepositoryTest : BaseDomainTest() {
    private val repoHelper by lazy { chatDomainImpl.repos }

    @Test
    fun testInsertAndRead() = runBlocking {
        repoHelper.insertChannelConfig(ChannelConfig("messaging", data.config1))
        val config = repoHelper.selectChannelConfig("messaging")
        Truth.assertThat(config).isEqualTo(config)
    }

    @Test
    fun testLoadAndRead() = runBlocking {
        repoHelper.insertChannelConfig(ChannelConfig("messaging", data.config1))
        repoHelper.clearChannelConfigsCache()
        var config = repoHelper.selectChannelConfig("messaging")
        Truth.assertThat(config).isNull()
        repoHelper.cacheChannelConfigs()
        config = repoHelper.selectChannelConfig("messaging")
        Truth.assertThat(config?.config).isEqualTo(data.config1)
    }

    @Test
    fun testUpdate() = runBlocking {
        repoHelper.insertChannelConfig(ChannelConfig("messaging", data.config1))
        data.config1.maxMessageLength = 200
        repoHelper.insertChannelConfig(ChannelConfig("messaging", data.config1))

        repoHelper.clearChannelConfigsCache()
        repoHelper.cacheChannelConfigs()

        val config = repoHelper.selectChannelConfig("messaging")?.config
        Truth.assertThat(config).isEqualTo(data.config1)
        Truth.assertThat(config!!.maxMessageLength).isEqualTo(200)
    }
}
