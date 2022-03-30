package io.getstream.logging.android.file.impl

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal abstract class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, bunlde: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, bunlde: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
