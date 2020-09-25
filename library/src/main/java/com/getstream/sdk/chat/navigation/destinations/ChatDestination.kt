package com.getstream.sdk.chat.navigation.destinations

import android.app.Activity
import android.content.Context
import android.content.Intent

abstract class ChatDestination(protected var context: Context) {
    abstract fun navigate()

    protected fun start(intent: Intent) {
        context.startActivity(intent)
    }

    protected fun startForResult(intent: Intent, requestCode: Int) {
        (context as Activity).startActivityForResult(intent, requestCode)
    }
}
