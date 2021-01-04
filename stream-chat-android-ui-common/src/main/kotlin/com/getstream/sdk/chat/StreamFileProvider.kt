package com.getstream.sdk.chat

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

public class StreamFileProvider : FileProvider() {

    public companion object {

        private fun getFileProviderAuthority(context: Context): String {
            val compName = ComponentName(context, StreamFileProvider::class.java.name)
            val providerInfo = context.packageManager.getProviderInfo(compName, 0)
            return providerInfo.authority
        }

        public fun getUriForFile(context: Context, file: File): Uri =
            getUriForFile(context, getFileProviderAuthority(context), file)
    }
}
