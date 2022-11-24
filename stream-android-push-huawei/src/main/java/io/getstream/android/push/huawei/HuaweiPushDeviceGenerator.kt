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

package io.getstream.android.push.huawei

import android.content.Context
import android.util.Log
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.api.ConnectionResult.SUCCESS
import com.huawei.hms.api.HuaweiApiAvailability
import io.getstream.android.push.PushDeviceGenerator
import io.getstream.android.push.PushDevice
import io.getstream.android.push.PushProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Generator responsible for providing information needed to register Huawei push notifications provider
 */
public class HuaweiPushDeviceGenerator(
    context: Context,
    private val appId: String,
    private val providerName: String? = null
) : PushDeviceGenerator {
    private val hmsInstanceId: HmsInstanceId = HmsInstanceId.getInstance(context)

    override fun isValidForThisDevice(context: Context): Boolean =
        (HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context) == SUCCESS).also {
            Log.i(TAG, "Is Huawei available on on this device -> $it")
        }

    override fun onPushDeviceGeneratorSelected() {
        HuaweiMessagingDelegate.fallbackProviderName = providerName
    }

    override fun asyncGeneratePushDevice(onPushDeviceGenerated: (pushDevice: PushDevice) -> Unit) {
        Log.i(TAG, "Getting Huawei token")

        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            hmsInstanceId.getToken(appId, "HCM")
                .takeUnless { it.isNullOrBlank() }
                ?.run {
                    Log.i(TAG, "Huawei returned token successfully")
                    onPushDeviceGenerated(
                        PushDevice(
                            token = this,
                            pushProvider = PushProvider.HUAWEI,
                            providerName = providerName,
                        )
                    )
                }
                ?: Log.i(TAG, "Error: Huawei didn't returned token")
        }
    }

    private companion object {
        private const val TAG = "Push:Huawei"
    }
}
