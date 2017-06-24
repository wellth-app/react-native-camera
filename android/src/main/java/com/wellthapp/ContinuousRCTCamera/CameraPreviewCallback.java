package com.wellthapp.ContinuousRCTCamera;

import android.hardware.Camera;
import android.os.Environment;

import com.facebook.react.bridge.Promise;
import com.lwansbrough.RCTCamera.ObjectIDGenerator;

import java.io.File;
import java.util.List;

public class CameraPreviewCallback implements Camera.PreviewCallback {

    public static File getFile() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "frame-" + ObjectIDGenerator.nextID() + ".jpg");
    }

    private final OnPreviewFrameAsyncTask asyncTask;


    public CameraPreviewCallback(final int captureInterval, final int positiveIdentificationTimeout, final List<String> acceptedTags, final Promise promise) {
        this.asyncTask = new OnPreviewFrameAsyncTask(captureInterval, positiveIdentificationTimeout, acceptedTags, promise);
        this.asyncTask.start();
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (!this.asyncTask.isRunning()) {
            this.asyncTask.start();
        }
        this.asyncTask.queue(data, camera);
    }

}
