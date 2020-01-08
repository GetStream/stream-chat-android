package com.getstream.sdk.chat.navigation.destinations;

import android.content.Intent;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.navigation.destinations.ChatDestination;
import com.getstream.sdk.chat.utils.CaptureController;
import com.getstream.sdk.chat.utils.Constant;

import android.app.Activity;

public class CameraDestination extends ChatDestination {

    public CameraDestination(Activity context) {
        super(context);
    }

    @Override
    public void navigate() {

        Intent takePictureIntent = CaptureController.getTakePictureIntent(context);
        Intent takeVideoIntent = CaptureController.getTakeVideoIntent(context);
        Intent chooserIntent = Intent.createChooser(takePictureIntent, context.getString(R.string.stream_input_camera_title));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeVideoIntent});

        startForResult(chooserIntent, Constant.CAPTURE_IMAGE_REQUEST_CODE);
    }
}