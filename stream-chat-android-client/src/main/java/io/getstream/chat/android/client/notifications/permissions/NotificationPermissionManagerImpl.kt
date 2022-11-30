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

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import io.getstream.chat.android.client.R
import io.getstream.log.StreamLog

@Suppress("ProtectedMemberInFinalClass", "ComplexCondition")
internal class NotificationPermissionManagerImpl(
    private val context: Context,
    private val requestPermissionOnAppLaunch: () -> Boolean,
    private val onPermissionStatus: (NotificationPermissionStatus) -> Unit,
) : NotificationPermissionManager, ActivityLifecycleCallbacks() {

    private val logger = StreamLog.getLogger("Chat:Notifications-PM")

    private val handler = Handler(Looper.getMainLooper())
    private val permissionContract = ActivityResultContracts.RequestPermission()

    private var started = false
    private var permissionRequested = false
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

    override fun start() {
        logger.d { "[start] no args" }
        handler.post {
            started = true
            currentActivity?.requestPermissionIfPossible()
        }
    }

    override fun stop() {
        logger.d { "[stop] no args" }
        handler.post {
            started = false
        }
    }

    override fun onActivityCreated(activity: Activity, bunlde: Bundle?) {
        logger.v { "[onActivityCreated] activity: $activity" }
        super.onActivityCreated(activity, bunlde)
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        logger.v { "[onActivityStarted] activity: $activity" }
        activity.registerPermissionCallback()
        super.onActivityStarted(activity)
        currentActivity = activity
    }

    override fun onFirstActivityStarted(activity: Activity) {
        super.onFirstActivityStarted(activity)
        logger.i { "[onFirstActivityStarted] activity: $activity" }
        activity.requestPermissionIfPossible()
    }

    override fun onActivityStopped(activity: Activity) {
        logger.v { "[onActivityStopped] activity: $activity" }
        activity.unregisterPermissionCallback()
        super.onActivityStopped(activity)
    }

    override fun onLastActivityStopped(activity: Activity) {
        super.onLastActivityStopped(activity)
        logger.i { "[onLastActivityStopped] activity: $activity" }
        permissionRequested = false
        currentActivity = null
    }

    private fun Activity.registerPermissionCallback() {
        if (this !is ComponentActivity) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        logger.i { "[registerPermissionCallback] activity: ${this::class.simpleName}" }
        val launcher = registerForActivityResult(permissionContract) { isGranted: Boolean ->
            logger.v { "[registerPermissionCallback] completed: $isGranted" }
            when (isGranted) {
                true -> onPermissionStatus(NotificationPermissionStatus.GRANTED)
                else -> onPermissionStatus(NotificationPermissionStatus.DENIED)
            }
        }
        logger.v { "[registerPermissionCallback] launcher: $launcher" }
        val contentLayout = findViewById<ViewGroup>(android.R.id.content)
        contentLayout.putActivityResultLauncher(launcher)
    }

    private fun Activity.unregisterPermissionCallback() {
        if (this !is ComponentActivity) return
        logger.i { "[unregisterPermissionCallback] activity: ${this::class.simpleName}" }
        val contentLayout = findViewById<ViewGroup>(android.R.id.content)
        val launcher = contentLayout.getActivityResultLauncher()
        logger.v { "[unregisterPermissionCallback] found launcher: $launcher" }
        launcher?.unregister()
    }

    private fun Activity.requestPermissionIfPossible() {
        val requestPermissionOnAppLaunch = requestPermissionOnAppLaunch()
        logger.i {
            "[requestPermissionIfPossible] started: $started, permissionRequested: $permissionRequested, " +
                "requestPermissionOnAppLaunch: $requestPermissionOnAppLaunch, "
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            started && !permissionRequested && requestPermissionOnAppLaunch
        ) {
            requestPermission()
            permissionRequested = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun Activity.requestPermission() {
        logger.d { "[requestPermission] no args" }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                logger.v { "[requestPermission] already granted" }
            }
            shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS) -> {
                logger.i { "[requestPermission] rationale requested" }
                onPermissionStatus(NotificationPermissionStatus.RATIONALE_NEEDED)
            }
            else -> {
                val contentLayout = findViewById<ViewGroup>(android.R.id.content)
                val launcher = contentLayout.getActivityResultLauncher()
                logger.i { "[requestPermission] launcher: $launcher" }
                launcher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                onPermissionStatus(NotificationPermissionStatus.REQUESTED)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun View.getActivityResultLauncher(): ActivityResultLauncher<String>? {
        return getTag(R.id.stream_post_notifications_permission) as? ActivityResultLauncher<String>
    }

    private fun View.putActivityResultLauncher(launcher: ActivityResultLauncher<String>) {
        return setTag(R.id.stream_post_notifications_permission, launcher)
    }
}
