package com.getstream.sdk.chat.startup

import android.content.Context
import androidx.startup.Initializer
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Jetpack Startup Initializer for the TreeTenABP library.
 */
public class ThreeTenInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        AndroidThreeTen.init(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
