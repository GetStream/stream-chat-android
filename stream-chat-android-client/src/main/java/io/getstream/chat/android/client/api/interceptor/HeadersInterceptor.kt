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

package io.getstream.chat.android.client.api.interceptor

import android.content.Context
import android.os.Build
import io.getstream.chat.android.client.utils.AppInfoUtil
import okhttp3.Interceptor
import okhttp3.Response
import java.text.Normalizer

internal class HeadersInterceptor(
    context: Context,
    private val isAnonymous: () -> Boolean,
    private val appInfoUtil: AppInfoUtil,
    private val sdkTrackingHeaders: () -> String,
) : Interceptor {

    private val userAgent by lazy { buildUserAgent(context) }

    override fun intercept(chain: Interceptor.Chain): Response {
        val authType = if (isAnonymous()) "anonymous" else "jwt"
        val request = chain.request()
            .newBuilder()
            .addHeader("User-Agent", userAgent)
            .addHeader("Content-Type", "application/json")
            .addHeader("stream-auth-type", authType)
            .addHeader("X-Stream-Client", sdkTrackingHeaders())
            .addHeader("Cache-Control", "no-cache")
            .build()
        return chain.proceed(request)
    }

    private fun buildUserAgent(context: Context): String {
        with(context.packageManager) {
            val versionName = appInfoUtil.getAppVersionName()
            val versionCode = appInfoUtil.getAppVersionCode()
            val appName = appInfoUtil.getAppName()

            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val version = Build.VERSION.SDK_INT
            val versionRelease = Build.VERSION.RELEASE

            val installerName = appInfoUtil.getInstallerName()

            return (
                "$appName / $versionName($versionCode); $installerName; ($manufacturer; " +
                    "$model; SDK $version; Android $versionRelease)"
                )
                .sanitize()
        }
    }

    private fun String.sanitize(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("[^\\p{ASCII}]".toRegex(), "")
    }
}
