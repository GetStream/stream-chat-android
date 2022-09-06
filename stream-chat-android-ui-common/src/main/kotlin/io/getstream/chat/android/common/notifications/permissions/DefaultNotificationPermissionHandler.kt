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

package io.getstream.chat.android.common.notifications.permissions

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import io.getstream.chat.android.client.notifications.permissions.ActivityLifecycleCallbacks
import io.getstream.chat.android.client.notifications.permissions.NotificationPermissionHandler
import io.getstream.chat.android.ui.common.R

private const val SCHEME_PACKAGE = "package"

/**
 * Shows a [Snackbar] with a link to app settings on [android.Manifest.permission.POST_NOTIFICATIONS] permission denial.
 */
@Suppress("ProtectedMemberInFinalClass")
public class DefaultNotificationPermissionHandler(
    private val context: Context,
) : NotificationPermissionHandler, ActivityLifecycleCallbacks() {

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

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        currentActivity = null
    }

    override fun onPermissionDenied() {
        currentActivity?.showNotificationBlocked()
    }

    private fun Activity.showNotificationBlocked() {
        val contentLayout = findViewById<ViewGroup>(android.R.id.content)
        Snackbar.make(
            contentLayout,
            R.string.stream_ui_message_input_permission_notifications_message,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.stream_ui_message_input_permissions_setting_button) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.Builder()
                    .scheme(SCHEME_PACKAGE)
                    .opaquePart(packageName)
                    .build()
            }
            startActivity(intent)
        }.show()
    }
}
