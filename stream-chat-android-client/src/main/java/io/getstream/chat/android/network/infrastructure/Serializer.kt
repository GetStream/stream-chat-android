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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.infrastructure

import com.squareup.moshi.Moshi

internal object Serializer {
    @JvmStatic
    internal val moshiBuilder: Moshi.Builder = Moshi.Builder()
        .add(io.getstream.chat.android.network.models.CreateDeviceRequest.PushProvider.PushProviderAdapter())
        .add(io.getstream.chat.android.network.infrastructure.IsoDateAdapter())
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())

    @JvmStatic
    internal val moshi: Moshi by lazy {
        moshiBuilder.build()
    }
}
