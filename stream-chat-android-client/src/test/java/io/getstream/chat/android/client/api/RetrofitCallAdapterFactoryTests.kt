package io.getstream.chat.android.client.api

import com.google.common.reflect.TypeToken
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser.GsonChatParser
import junit.framework.Assert.fail
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class RetrofitCallAdapterFactoryTests {

    @Rule
    @JvmField
    val server = MockWebServer()

    private val factory: CallAdapter.Factory = RetrofitCallAdapterFactory.create(GsonChatParser())
    private lateinit var retrofit: Retrofit

    @Before
    fun setUp() {
        retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addCallAdapterFactory(factory)
            .build()
    }

    @Test
    fun `When returning raw call Then should throw an exception`() {
        try {
            factory[RetrofitCall::class.java, emptyArray(), retrofit]
            fail()
        } catch (e: IllegalArgumentException) {
            assertThat(e)
                .hasMessage("Call return type must be parameterized as Call<Foo>")
        }
    }

    @Test
    fun `When returning raw response type Then adapter should have the same response type`() {
        val typeToken: Type = object : TypeToken<RetrofitCall<String>>() {}.type
        val callAdapter = factory[typeToken, emptyArray(), retrofit]

        assertThat(callAdapter).isNotNull
        assertThat(callAdapter!!.responseType())
            .isEqualTo(object : TypeToken<String>() {}.type)
    }

    @Test
    fun `When returning generic response type Then adapter should have the same response type`() {
        val typeToken = object : TypeToken<RetrofitCall<List<String>>>() {}.type
        val callAdapter = factory[typeToken, emptyArray(), retrofit]

        assertThat(callAdapter!!.responseType())
            .isEqualTo(object : TypeToken<List<String>>() {}.type)
    }

    @Test
    fun `When returning different type Then should return null`() {
        val adapter = factory[String::class.java, emptyArray(), retrofit]
        assertThat(adapter).isNull()
    }
}
