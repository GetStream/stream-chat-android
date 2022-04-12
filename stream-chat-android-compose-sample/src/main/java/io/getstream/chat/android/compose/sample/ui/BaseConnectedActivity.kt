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

package io.getstream.chat.android.compose.sample.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Base Activity for a logged in user in authorized zone.
 */
open class BaseConnectedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The application was killed in the background. Starting from the launcher activity.
        if (savedInstanceState != null && lastNonConfigurationInstance == null) {
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
            finishAffinity()
        }
    }
}
