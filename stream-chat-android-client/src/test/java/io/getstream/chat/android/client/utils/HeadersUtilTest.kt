/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.client.ChatClient
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
internal class HeadersUtilTest {

    private lateinit var context: Context
    private lateinit var packageManager: PackageManager

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        packageManager = mock(PackageManager::class.java)
    }

    @Test
    fun `buildSdkTrackingHeaders should include correct values`() {
        val headersUtil = HeadersUtil(context, appName = "Chat App", appVersion = "1.3.1-DEBUG")
        val header = headersUtil.buildSdkTrackingHeaders()

        assertTrue(header.contains(BuildConfig.STREAM_CHAT_VERSION))
        assertTrue(header.contains("|os=Android ${Build.VERSION.RELEASE}"))
        assertTrue(header.contains("|api_version=${Build.VERSION.SDK_INT}"))
        assertTrue(header.contains("|device_model=${Build.MANUFACTURER} ${Build.MODEL}"))
        assertTrue(header.contains("|offline_enabled=${ChatClient.OFFLINE_SUPPORT_ENABLED}"))
        assertTrue(header.contains("|app=Chat App"))
        assertTrue(header.contains("|app_version=1.3.1-DEBUG"))
    }

    @Test
    fun `buildUserAgent should format correctly`() {
        val headersUtil = HeadersUtil(context, appName = "Chat App", appVersion = "1.3.1-DEBUG")
        val userAgent = headersUtil.buildUserAgent()

        assertTrue(userAgent.contains("Chat App / 1.3.1-DEBUG"))
        assertTrue(userAgent.contains("(${Build.MANUFACTURER}; ${Build.MODEL}"))
        assertTrue(userAgent.contains("SDK ${Build.VERSION.SDK_INT}"))
        assertTrue(userAgent.contains("Android ${Build.VERSION.RELEASE}"))
    }

    @Test
    fun `if we cannot find app name then getAppName should return UnknownApp`() {
        val headersUtil = HeadersUtil(context, null, null)
        val header = headersUtil.buildSdkTrackingHeaders()
        assertTrue(header.contains("|app=UnknownApp"))
    }

    @Test
    fun `if we cannot find app version then getAppVersionName should return nameNotFound`() {
        val headersUtil = HeadersUtil(context, null, null)
        val header = headersUtil.buildSdkTrackingHeaders()
        assertTrue(header.contains("|app_version=nameNotFound"))
    }

    @Test
    fun `getAppVersionName should return correct version`() {
        val packageInfo = PackageInfo().apply { versionName = "1.2.3" }
        `when`(context.packageManager).thenReturn(packageManager)
        `when`(packageManager.getPackageInfo(context.packageName, 0)).thenReturn(packageInfo)

        val headersUtil = HeadersUtil(context, null, null)
        val headers = headersUtil.buildSdkTrackingHeaders()
        assertTrue(headers.contains("|app_version=1.2.3"))
    }

    @Test
    @Config(sdk = [28]) // Simulates API 28+ (P)
    fun `getAppVersionCode returns longVersionCode for API 28+`() {
        val packageInfo = PackageInfo().apply { longVersionCode = 12345L }

        `when`(context.packageManager).thenReturn(packageManager)
        `when`(packageManager.getPackageInfo(context.packageName, 0)).thenReturn(packageInfo)

        val headersUtil = HeadersUtil(context, "Chat App", "1.3.1-DEBUG")
        val result = headersUtil.buildUserAgent()

        assertTrue(result.contains("12345"))
    }
}
