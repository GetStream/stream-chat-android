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

package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomAppSettings
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AppSettingManagerTest {

    @Test
    fun `Given loadAppSettings is not called, When calling getAppSettings, then default appSettings are returned`() {
        // given
        val api = mock<ChatApi>()
        val appSettingManager = AppSettingManager(api)
        // when
        val appSettings = appSettingManager.getAppSettings()
        // then
        val defaultAppSettings = AppSettingManager.createDefaultAppSettings()
        appSettings shouldBeEqualTo defaultAppSettings
    }

    @Test
    fun `Given loadAppSettings is successful, When calling getAppSettings, Then appSettings are returned`() = runTest {
        // given
        val appSettings = randomAppSettings()
        val api = mock<ChatApi>()
        whenever(api.appSettings()).thenReturn(RetroSuccess(appSettings).toRetrofitCall())
        val appSettingManager = AppSettingManager(api)
        appSettingManager.loadAppSettings()
        // when
        val result = appSettingManager.getAppSettings()
        // then
        result shouldBeEqualTo appSettings
    }

    @Test
    fun `Given loadAppSettings fails, When calling getAppSettings, Then default appSettings are returned`() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val api = mock<ChatApi>()
        whenever(api.appSettings()).thenReturn(RetroError<AppSettings>(errorCode).toRetrofitCall())
        val appSettingManager = AppSettingManager(api)
        appSettingManager.loadAppSettings()
        // when
        val result = appSettingManager.getAppSettings()
        // then
        val defaultAppSettings = AppSettingManager.createDefaultAppSettings()
        result shouldBeEqualTo defaultAppSettings
    }

    @Test
    fun `Given appSettings already loaded, When calling getAppSettings, Then appSettings are returned and call is not performed`() {
        // given
        val appSettings = randomAppSettings()
        val api = mock<ChatApi>()
        whenever(api.appSettings()).thenReturn(RetroSuccess(appSettings).toRetrofitCall())
        val appSettingManager = AppSettingManager(api)
        // Call loadAppSettings twice
        appSettingManager.loadAppSettings()
        // when
        appSettingManager.loadAppSettings()
        val result = appSettingManager.getAppSettings()
        // then
        result shouldBeEqualTo appSettings
        verify(api, times(1)).appSettings()
    }

    @Test
    fun `Given appSettings already loaded, When calling clear, Then the appSettings are cleared`() = runTest {
        // given
        val appSettings = randomAppSettings()
        val api = mock<ChatApi>()
        whenever(api.appSettings()).thenReturn(RetroSuccess(appSettings).toRetrofitCall())
        val appSettingManager = AppSettingManager(api)
        appSettingManager.loadAppSettings()
        // when
        appSettingManager.clear()
        val result = appSettingManager.getAppSettings()
        // then
        val defaultAppSettings = AppSettingManager.createDefaultAppSettings()
        result shouldBeEqualTo defaultAppSettings
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }
}
