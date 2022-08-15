package io.getstream.chat.android.client.notifications.permissions

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal abstract class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, bunlde: Bundle?) { /* no-op */ }

    override fun onActivityStarted(activity: Activity) { /* no-op */ }

    override fun onActivityResumed(activity: Activity) { /* no-op */ }

    override fun onActivityPaused(activity: Activity) { /* no-op */ }

    override fun onActivityStopped(activity: Activity) { /* no-op */ }

    override fun onActivitySaveInstanceState(activity: Activity, bunlde: Bundle) { /* no-op */ }

    override fun onActivityDestroyed(activity: Activity) { /* no-op */ }
}