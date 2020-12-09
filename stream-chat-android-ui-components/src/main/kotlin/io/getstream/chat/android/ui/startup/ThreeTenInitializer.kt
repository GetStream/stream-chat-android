package io.getstream.chat.android.ui.startup

import android.content.Context
import androidx.startup.Initializer
import com.jakewharton.threetenabp.AndroidThreeTen

@Suppress("unused")
internal class ThreeTenInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        AndroidThreeTen.init(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
