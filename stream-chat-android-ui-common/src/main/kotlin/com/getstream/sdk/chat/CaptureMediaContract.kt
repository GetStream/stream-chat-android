package com.getstream.sdk.chat

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

@InternalStreamChatApi
public class CaptureMediaContract : ActivityResultContract<Unit, File?>() {

    private var pictureFile: File? = null
    private var videoFile: File? = null

    override fun createIntent(context: Context, input: Unit?): Intent {
        val takePictureIntents =
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir, createFileName("STREAM_IMG", "jpg")).let {
                pictureFile = it
                createIntentList(context, MediaStore.ACTION_IMAGE_CAPTURE, it)
            }
        val recordVideoIntents =
            File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.cacheDir, createFileName("STREAM_VID", "mp4")).let {
                videoFile = it
                createIntentList(context, MediaStore.ACTION_VIDEO_CAPTURE, it)
            }
        val intents = takePictureIntents + recordVideoIntents
        val initialIntent = intents.lastOrNull() ?: Intent()
        return Intent.createChooser(initialIntent, context.getString(R.string.stream_ui_message_input_capture_media))
            .apply {
                putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    (intents - initialIntent).toTypedArray()
                )
            }
    }

    private fun createFileName(prefix: String, extension: String) =
        "${prefix}_${dateFormat.format(Date().time)}.$extension"

    private fun createIntentList(
        context: Context,
        action: String,
        destinationFile: File
    ): List<Intent> {
        val destinationUri = StreamFileUtil.getUriForFile(
            context,
            destinationFile
        )
        val actionIntent = Intent(action)
        return context.packageManager.queryIntentActivities(
            actionIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        ).map {
            Intent(actionIntent).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, destinationUri)
                flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                component = ComponentName(it.activityInfo.packageName, it.activityInfo.name)
                `package` = it.activityInfo.packageName
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): File? =
        (pictureFile.takeIfCaptured() ?: videoFile.takeIfCaptured())
            .takeIf { resultCode == Activity.RESULT_OK }
}

private fun File?.takeIfCaptured(): File? = this?.takeIf { it.exists() && it.length() > 0 }
