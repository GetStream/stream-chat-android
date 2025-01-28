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
import io.getstream.chat.android.client.ChatClient
import okhttp3.Interceptor
import okhttp3.Response
import java.text.Normalizer

internal class HeadersInterceptor(
    context: Context,
    private val isAnonymous: () -> Boolean,
) : Interceptor {

    private val userAgent by lazy { buildUserAgent(context) }

    override fun intercept(chain: Interceptor.Chain): Response {
        val authType = if (isAnonymous()) "anonymous" else "jwt"
        val request = chain.request()
            .newBuilder()
            .addHeader("User-Agent", userAgent)
            .addHeader("Content-Type", "application/json")
            .addHeader("stream-auth-type", authType)
            .addHeader("X-Stream-Client", ChatClient.buildSdkTrackingHeaders())
            .addHeader("Cache-Control", "no-cache")
            .build()
        return chain.proceed(request)
    }

    private fun buildUserAgent(context: Context): String {
        with(context.packageManager) {
            val versionName = runCatching {
                getPackageInfo(context.packageName, 0).versionName
            }.getOrNull() ?: "nameNotFound"
            val versionCode = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    getPackageInfo(context.packageName, 0).longVersionCode.toString()
                } else {
                    getPackageInfo(context.packageName, 0).versionCode.toString()
                }
            }.getOrNull() ?: "versionCodeNotFound"

            val applicationInfo = context.applicationInfo
            val appName = if (applicationInfo != null) {
                val stringId = applicationInfo.labelRes
                if (stringId == 0) {
                    applicationInfo.nonLocalizedLabel?.toString() ?: "UnknownApp"
                } else {
                    context.getString(stringId) ?: "UnknownApp"
                }
            } else { "UnknownApp" }

            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val version = Build.VERSION.SDK_INT
            val versionRelease = Build.VERSION.RELEASE

            val installerName = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    getInstallSourceInfo(context.packageName).installingPackageName
                } else {
                    getInstallerPackageName(context.packageName)
                }
            }.getOrNull() ?: "StandAloneInstall"

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
