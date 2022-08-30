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

package io.getstream.chat.ui.sample.application

import android.app.Application
import android.os.Build
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.soloader.SoLoader
import io.getstream.chat.android.client.di.networkFlipper

object ApplicationConfigurator {

    const val HUAWEI_APP_ID = "104598359"
    const val XIAOMI_APP_ID = "2882303761520059340"
    const val XIAOMI_APP_KEY = "5792005994340"

    fun configureApp(application: Application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SoLoader.init(application, false)

            if (FlipperUtils.shouldEnableFlipper(application)) {
                AndroidFlipperClient.getInstance(application).apply {
                    addPlugin(InspectorFlipperPlugin(application, DescriptorMapping.withDefaults()))
                    addPlugin(DatabasesFlipperPlugin(application))
                    addPlugin(networkFlipper)
                }.start()
            }
        }
    }
}
