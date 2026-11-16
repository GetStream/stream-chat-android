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

package io.getstream.chat.android.client.di

import io.getstream.chat.android.client.parser2.withErrorLogging
import io.getstream.chat.android.network.infrastructure.QueryConverterFactory
import io.getstream.chat.android.network.infrastructure.Serializer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Adds the converters the v2 endpoints need. Without [QueryConverterFactory], Retrofit sends a `JsonQuery` or a date
 * query parameter through `toString()`.
 */
internal fun Retrofit.Builder.addV2Converters(): Retrofit.Builder = this
    .addConverterFactory(QueryConverterFactory(Serializer.moshi))
    .addConverterFactory(MoshiConverterFactory.create(Serializer.moshi).withErrorLogging())
