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

package io.getstream.chat.android.client.interceptor.message

import android.content.Context
import android.net.ConnectivityManager
import io.getstream.chat.android.client.network.NetworkStateProvider

public class PrepareMessageInterceptorFactory {

    public fun create(context: Context): PrepareMessageInterceptor {
        val networkStateProvider =
            NetworkStateProvider(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

        return PrepareMessageInterceptorImpl(networkStateProvider)
    }
}
