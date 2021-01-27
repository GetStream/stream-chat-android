package io.getstream.chat.android.client.parser2

import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Test

internal class DtoTestDataTest {

    @Test
    fun `Check test data is valid JSON`() {
        val moshi = Moshi.Builder().build()
        val anyAdapter = moshi.adapter(Any::class.java)

        anyAdapter.fromJson(DtoTestData.downstreamJson)
        anyAdapter.fromJson(DtoTestData.downstreamJsonWithoutExtraData)
        anyAdapter.fromJson(DtoTestData.upstreamJson)
        anyAdapter.fromJson(DtoTestData.upstreamJsonWithoutExtraData)
    }
}
