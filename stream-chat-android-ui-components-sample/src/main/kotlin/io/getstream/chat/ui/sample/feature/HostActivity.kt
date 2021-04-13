package io.getstream.chat.ui.sample.feature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.ui.sample.R

class HostActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && lastNonConfigurationInstance == null) {
            // the application process was killed by the OS
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
            finishAffinity()
        }
    }
}
