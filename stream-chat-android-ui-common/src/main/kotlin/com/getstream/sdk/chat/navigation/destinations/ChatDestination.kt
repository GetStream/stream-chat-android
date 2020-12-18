package com.getstream.sdk.chat.navigation.destinations

import android.app.Activity
import android.content.Context
import android.content.Intent

public abstract class ChatDestination(protected val context: Context) {
    public abstract fun navigate()

    protected fun start(intent: Intent) {
        context.startActivity(intent)
    }

    protected fun startForResult(intent: Intent, requestCode: Int) {
        check(context is Activity) {
            "startForResult can only be used if your destination uses an Activity as its Context"
        }
        context.startActivityForResult(intent, requestCode)
    }
}
