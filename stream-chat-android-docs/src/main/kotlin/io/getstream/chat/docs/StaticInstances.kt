package io.getstream.chat.docs

import android.app.Activity
import io.getstream.chat.android.models.User

object StaticInstances {
    const val TAG = "Stream"
}

object TokenService {
    fun getToken(user: User): String = ""
}

object MainActivity : Activity()
