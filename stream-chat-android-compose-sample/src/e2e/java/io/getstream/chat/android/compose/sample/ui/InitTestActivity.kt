package io.getstream.chat.android.compose.sample.ui

import android.content.Context
import android.content.Intent
import io.getstream.chat.android.compose.sample.ui.login.UserLoginActivity
import java.io.Serializable

sealed class InitTestActivity : Serializable {
    abstract fun createIntent(context: Context): Intent

    data object UserLogin : InitTestActivity() {
        private fun readResolve(): Any = UserLogin
        override fun createIntent(context: Context): Intent =
            UserLoginActivity.createIntent(context)
    }

    data object Jwt : InitTestActivity() {
        private fun readResolve(): Any = Jwt
        override fun createIntent(context: Context): Intent =
            JwtTestActivity.createIntent(context)
    }
}