/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ai.compose.sample.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Network module for providing network-related dependencies.
 * This module encapsulates the creation of Retrofit, OkHttp, and Moshi instances.
 */
public class NetworkModule {

    fun createMoshi(): Moshi =
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

    /**
     * @param enableLogging Whether to enable HTTP request/response logging (default: true in debug builds)
     */
    fun createOkHttpClient(enableLogging: Boolean = true): OkHttpClient =
        OkHttpClient.Builder()
            .apply {
                if (enableLogging) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        },
                    )
                }
            }
            .build()

    fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit {
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) {
            baseUrl
        } else {
            "$baseUrl/"
        }
        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
