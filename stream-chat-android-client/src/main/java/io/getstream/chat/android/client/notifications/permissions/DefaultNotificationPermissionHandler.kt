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

package io.getstream.chat.android.client.notifications.permissions

import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.Toast
import io.getstream.chat.android.client.R
import io.getstream.log.StreamLog

/**
 * Shows a [Toast] with a link to app settings on [android.Manifest.permission.POST_NOTIFICATIONS] permission denial.
 *
 * @see [io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory]
 */
@Suppress("ProtectedMemberInFinalClass")
public class DefaultNotificationPermissionHandler(
    private val context: Context,
) : NotificationPermissionHandler, ActivityLifecycleCallbacks() {

    private val logger = StreamLog.getLogger("Chat:Default-NPH")

    private var currentActivity: Activity? = null

    init {
        (context.applicationContext as? Application)?.also {
            it.registerActivityLifecycleCallbacks(this)
        }
    }

    protected fun finalize() {
        (context.applicationContext as? Application)?.also {
            it.unregisterActivityLifecycleCallbacks(this)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        super.onActivityStarted(activity)
        currentActivity = activity
    }

    override fun onLastActivityStopped(activity: Activity) {
        super.onLastActivityStopped(activity)
        currentActivity = null
    }

    override fun onPermissionDenied() {
        logger.i { "[onPermissionDenied] currentActivity: $currentActivity" }
        currentActivity?.showNotificationBlocked()
    }

    private fun Activity.showNotificationBlocked() {
        Toast.makeText(
            this, R.string.stream_ui_message_input_permission_notifications_message, Toast.LENGTH_LONG
        ).show()
    }
}
