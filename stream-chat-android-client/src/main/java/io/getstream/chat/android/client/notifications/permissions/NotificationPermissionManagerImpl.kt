package io.getstream.chat.android.client.notifications.permissions

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import io.getstream.logging.StreamLog

internal interface NotificationPermissionManager {

    fun start()

    fun stop()

}

internal class NotificationPermissionManagerImpl(
    private val context: Context,
    private val requestPermissionOnAppLaunch: () -> Boolean,
    private val onPermissionGranted: () -> Unit,
    private val onPermissionDenied: () -> Unit,
) : NotificationPermissionManager, ActivityLifecycleCallbacks() {

    private val logger = StreamLog.getLogger("Chat:Notifications-PM")

    private val handler = Handler(Looper.getMainLooper())
    private val permissionContract = ActivityResultContracts.RequestPermission()

    private var started = false
    private var permissionRequested = false
    private var activityCount = 0
    private var currentActivity: ComponentActivity? = null
    private var launcher: ActivityResultLauncher<String>? = null

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
            requestPermissionIfPossible()
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
        if (activity !is ComponentActivity) return
        currentActivity = activity
        launcher?.unregister()
        launcher = activity.registerForActivityResult(permissionContract) { isGranted: Boolean ->
            logger.v { "[requestPermission] completed: $isGranted" }
            when (isGranted) {
                true -> onPermissionGranted()
                else -> onPermissionDenied().also {
                    showNotificationBlocked(activity)
                }
            }
        }
    }

    override fun onActivityStarted(activity: Activity) {
        logger.v { "[onActivityStarted] activity: $activity" }
        super.onActivityStarted(activity)
        if (activityCount++ == 0) {
            onFirstActivityStarted(activity)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        logger.v { "[onActivityStopped] activity: $activity" }
        super.onActivityStopped(activity)
        launcher?.unregister()
        currentActivity = null
        if (--activityCount == 0) {
            onLastActivityStopped(activity)
        }
    }

    private fun onFirstActivityStarted(activity: Activity) {
        logger.i { "[onFirstActivityStarted] activity: $activity" }
        currentActivity = activity as? ComponentActivity
        requestPermissionIfPossible()
    }

    private fun onLastActivityStopped(activity: Activity) {
        logger.i { "[onLastActivityStopped] activity: $activity" }
        launcher?.unregister()
        currentActivity = null
        permissionRequested = false
    }

    private fun requestPermissionIfPossible() {
        logger.i { "[requestPermissionIfPossible] permissionRequested: $permissionRequested" }
        val activity = currentActivity ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && started && !permissionRequested && requestPermissionOnAppLaunch()) {
            requestPermission(activity)
            permissionRequested = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission(activity: ComponentActivity) {
        logger.d { "[requestPermission] no args" }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        when {
            ContextCompat.checkSelfPermission(
                activity, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                logger.v { "[requestPermission] already granted" }
                onPermissionGranted()
            }
            shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS) -> {
                logger.v { "[requestPermission] already denied" }
                onPermissionDenied()
                showNotificationBlocked(activity)
            }
            else -> {
                logger.v { "[requestPermission] launch" }
                launcher?.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showNotificationBlocked(activity: ComponentActivity) {
        Snackbar.make(
            activity.window.decorView.rootView,
            "Notification blocked",
            Snackbar.LENGTH_LONG
        ).setAction("Settings") {
            // Responds to click on the action
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }.show()
    }

}