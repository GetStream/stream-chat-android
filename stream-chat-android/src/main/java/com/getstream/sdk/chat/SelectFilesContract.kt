package com.getstream.sdk.chat

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class SelectFilesContract : ActivityResultContract<Unit, List<Uri>>() {

    override fun createIntent(
        context: Context,
        input: Unit?
    ): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = "*/*"
        addCategory(Intent.CATEGORY_OPENABLE)
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return intent?.data.takeIf { resultCode == Activity.RESULT_OK }?.let { listOf(it) }
            ?: parseMultipleResults(intent?.clipData?.takeIf { resultCode == Activity.RESULT_OK })
    }

    private fun parseMultipleResults(clipData: ClipData?): List<Uri> {
        return clipData?.let {
            val list = mutableListOf<Uri>()
            for (i in 0 until it.itemCount) {
                list += it.getItemAt(i).uri
            }
            return list
        } ?: emptyList()
    }
}
