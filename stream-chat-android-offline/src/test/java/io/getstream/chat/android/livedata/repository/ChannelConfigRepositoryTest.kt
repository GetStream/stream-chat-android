package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.livedata.model.config
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelConfigRepositoryTest : BaseDomainTest() {
    private val repo by lazy { chatDomainImpl.repos.configs }

    @Test
    fun testInsertAndRead() = runBlocking {
        repo.insert("messaging" to data.config1)
        val config = repo.select("messaging")
        Truth.assertThat(config).isEqualTo(config)
    }

    @Test
    fun testLoadAndRead() = runBlocking {
        repo.insert("messaging" to data.config1)
        repo.clearCache()
        var config = repo.select("messaging")
        Truth.assertThat(config).isNull()
        repo.load()
        config = repo.select("messaging")
        Truth.assertThat(config?.config).isEqualTo(data.config1)
    }

    @Test
    fun testUpdate() = runBlocking {
        repo.insert("messaging" to data.config1)
        data.config1.maxMessageLength = 200
        repo.insert("messaging" to data.config1)

        repo.clearCache()
        repo.load()

        val config = repo.select("messaging")?.config
        Truth.assertThat(config).isEqualTo(data.config1)
        Truth.assertThat(config!!.maxMessageLength).isEqualTo(200)
    }
}
