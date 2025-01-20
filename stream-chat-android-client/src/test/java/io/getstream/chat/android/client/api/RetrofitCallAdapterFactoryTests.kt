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

package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser2.MoshiChatParser
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.fail
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
internal class RetrofitCallAdapterFactoryTests {

    @Rule
    @JvmField
    val server = MockWebServer()

    private val factory: CallAdapter.Factory =
        RetrofitCallAdapterFactory.create(
            MoshiChatParser(
                EventMapping(
                    DomainMapping(
                        { "" },
                        NoOpChannelTransformer,
                        NoOpMessageTransformer,
                    ),
                ),
                DtoMapping(
                    NoOpMessageTransformer,
                ),
            ),
            CoroutineScope(Dispatchers.IO),
        )
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
            fail("Assertion failed")
        } catch (e: IllegalArgumentException) {
            e.message shouldBeEqualTo "Call return type must be parameterized as Call<Foo>"
        }
    }

    @Test
    fun `When returning raw response type Then adapter should have the same response type`() {
        val type: Type = typeOf<RetrofitCall<String>>().javaType
        val callAdapter = factory[type, emptyArray(), retrofit]

        callAdapter.shouldNotBeNull()
        callAdapter.responseType() shouldBeEqualTo typeOf<String>().javaType
    }

    @Test
    fun `When returning generic response type Then adapter should have the same response type`() {
        val type = typeOf<RetrofitCall<List<String>>>().javaType
        val callAdapter = factory[type, emptyArray(), retrofit]

        callAdapter.shouldNotBeNull()

        callAdapter.responseType() shouldBeEqualTo typeOf<List<String>>().javaType
    }

    @Test
    fun `When returning different type Then should return null`() {
        val adapter = factory[String::class.java, emptyArray(), retrofit]
        adapter.shouldBeNull()
    }
}
