package com.getstream.sdk.chat

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.io.File
import java.io.IOException

public class StreamFileProvider : FileProvider() {

    @InternalStreamChatApi
    public companion object {

        private fun getFileProviderAuthority(context: Context): String {
            val compName = ComponentName(context, StreamFileProvider::class.java.name)
            val providerInfo = context.packageManager.getProviderInfo(compName, 0)
            return providerInfo.authority
        }

        public fun getUriForFile(context: Context, file: File): Uri =
            getUriForFile(context, getFileProviderAuthority(context), file)

        public fun writeImageToSharableFile(context: Context, bitmap: Bitmap): Uri? {
            return try {
                val file = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir,
                    "share_image_${System.currentTimeMillis()}.png"
                )
                file.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                    out.flush()
                }
                getUriForFile(context, file)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
}
