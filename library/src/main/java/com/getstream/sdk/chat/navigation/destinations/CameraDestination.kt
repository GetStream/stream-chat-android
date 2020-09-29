package com.getstream.sdk.chat.navigation.destinations

import android.app.Activity
import android.content.Intent
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.utils.CaptureController
import com.getstream.sdk.chat.utils.Constant

class CameraDestination(context: Activity) : ChatDestination(context) {
    override fun navigate() {
        val takePictureIntent = CaptureController.getTakePictureIntent(context)
        val takeVideoIntent = CaptureController.getTakeVideoIntent(context)
        val chooserIntent = Intent.createChooser(
            takePictureIntent,
            context.getString(R.string.stream_input_camera_title)
        )
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takeVideoIntent))
        startForResult(chooserIntent, Constant.CAPTURE_IMAGE_REQUEST_CODE)
    }
}
