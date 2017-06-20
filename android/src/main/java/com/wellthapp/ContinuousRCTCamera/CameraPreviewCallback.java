package com.wellthapp.ContinuousRCTCamera;

import android.hardware.Camera;
import android.os.Environment;

import com.lwansbrough.RCTCamera.ObjectIDGenerator;

import java.io.File;
import java.util.List;

public class CameraPreviewCallback implements Camera.PreviewCallback {

    public static File getFile() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "frame-" + ObjectIDGenerator.nextID() + ".jpg");
    }

    private final OnPreviewFrameAsyncTask asyncTask;



    public CameraPreviewCallback(final int captureInterval, final int positiveIdentificationTimeout, final List<String> acceptedTags) {
        this.asyncTask = new OnPreviewFrameAsyncTask(captureInterval, positiveIdentificationTimeout, acceptedTags);
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        this.asyncTask.queue(data, camera);
    }

}
