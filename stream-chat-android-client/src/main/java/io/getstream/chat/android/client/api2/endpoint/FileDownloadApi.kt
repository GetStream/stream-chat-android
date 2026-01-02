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

package io.getstream.chat.android.client.api2.endpoint

import io.getstream.chat.android.client.api.AnonymousApi
import io.getstream.chat.android.client.call.RetrofitCall
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * An interface used to download files directly.
 */
@AnonymousApi
internal interface FileDownloadApi {

    /**
     * A method that downloads the file and does not
     * convert the body.
     *
     * @param fileUrl The url that contains the file we are downloading.
     *
     * @return Returns an unconverted body inside of a [ResponseBody] wrapped
     * in a [RetrofitCall].
     */
    @Streaming
    @GET
    fun downloadFile(@Url fileUrl: String): RetrofitCall<ResponseBody>
}
