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

package io.getstream.chat.android.client.api2

import com.squareup.moshi.Moshi
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class MoshiUrlQueryPayloadFactory(private val moshi: Moshi) : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation?>,
        retrofit: Retrofit,
    ): Converter<*, String>? {
        return if (annotations.filterIsInstance<UrlQueryPayload>().isNotEmpty()) {
            UrlQueryPayloadConverted(moshi, type)
        } else {
            super.stringConverter(type, annotations, retrofit)
        }
    }

    private class UrlQueryPayloadConverted(
        private val moshi: Moshi,
        private val type: Type,
    ) : Converter<Any, String> {
        override fun convert(value: Any): String {
            return moshi.adapter<Any>(type).toJson(value)
        }
    }
}
