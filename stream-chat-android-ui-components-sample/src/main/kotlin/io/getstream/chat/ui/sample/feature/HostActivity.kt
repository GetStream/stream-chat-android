package io.getstream.chat.ui.sample.feature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.ui.sample.R

const val EXTRA_CHANNEL_ID = "extra_channel_id"
const val EXTRA_CHANNEL_TYPE = "extra_channel_type"
const val EXTRA_MESSAGE_ID = "extra_message_id"
class HostActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && lastNonConfigurationInstance == null) {
            // the application process was killed by the OS
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
            finishAffinity()
        }
    }

    companion object {

        fun createLaunchIntent(
            context: Context,
            messageId: String,
            channelType: String,
            channelId: String,

            ) = Intent(context, HostActivity::class.java).apply {
            putExtra(EXTRA_CHANNEL_ID, channelId)
            putExtra(EXTRA_CHANNEL_TYPE, channelType)
            putExtra(EXTRA_MESSAGE_ID, messageId)

        }
    }
}
