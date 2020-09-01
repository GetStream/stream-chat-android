package com.getstream.sdk.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class DocumentTreeAccessContract : ActivityResultContract<Unit, Uri>() {

    override fun createIntent(
        context: Context,
        input: Unit?
    ) = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.data.takeIf { resultCode == Activity.RESULT_OK }
}
