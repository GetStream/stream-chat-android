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

package io.getstream.chat.android.offline.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.User
import kotlinx.coroutines.delay
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowNetworkCapabilities
import java.util.concurrent.atomic.AtomicBoolean

internal suspend fun waitForSetUser(
    client: ChatClient,
    user: User,
    token: String,
    timeMillis: Long = 5000,
) {
    val attemptDuration = 10L
    var attempt = timeMillis / attemptDuration

    var success = AtomicBoolean(false)

    mockNetworkConnectionStatus()

    client.connectUser(user, token).enqueue {
        success.set(true)
    }
    while (!success.get() && --attempt > 0) {
        delay(attemptDuration)
        // trigger the event loop to run
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }
}

private fun mockNetworkConnectionStatus() {
    val connectivityManager = getApplicationContext<Context>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = ShadowNetworkCapabilities.newInstance()
    shadowOf(networkCapabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    shadowOf(networkCapabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    shadowOf(connectivityManager).setNetworkCapabilities(connectivityManager.activeNetwork, networkCapabilities)
}
