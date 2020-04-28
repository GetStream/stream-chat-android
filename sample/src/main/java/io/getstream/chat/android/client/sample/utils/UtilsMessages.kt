package io.getstream.chat.android.client.sample.utils

import android.widget.Toast
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.sample.App

object UtilsMessages {

    fun show(error: ChatError) {
        error.printStackTrace()
        show(error.message.toString())
    }

    fun show(msg: String) {
        Toast.makeText(App.instance, msg, Toast.LENGTH_SHORT).show()
    }
}