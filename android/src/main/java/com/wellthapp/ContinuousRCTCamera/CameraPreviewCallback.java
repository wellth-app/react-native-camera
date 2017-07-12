package com.wellthapp.ContinuousRCTCamera;

import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.lwansbrough.RCTCamera.ObjectIDGenerator;

import java.io.File;

public class CameraPreviewCallback implements Camera.PreviewCallback {

    public static final String TAG = "CameraPreviewCallback";

    public static File getFile() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "frame-" + ObjectIDGenerator.nextID() + ".jpg");
    }

    public static File getFile(final String name) {
        if (name != null) {
            return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name + ".jpg");
        } else {
            return getFile();
        }
    }

    private final OnPreviewFrameAsyncTask asyncTask;
    private volatile boolean readyForCapture = false;
    private volatile ContinuousCaptureOutputConfigurations continuousCaptureOutputConfigurations;

    public CameraPreviewCallback(final ReactContext reactContext) {
        this.asyncTask = new OnPreviewFrameAsyncTask(reactContext);
        this.asyncTask.start();
        this.continuousCaptureOutputConfigurations = ContinuousCaptureOutputConfigurations.getDefault();
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (this.getReadyForCapture()) {
            Log.d(TAG, "onPreviewFrame() --> readyForCapture == true");
            this.setReadyForCapture(false);
            if (!this.asyncTask.isRunning()) {
                this.asyncTask.start();
            }
            final OnPreviewFrameAsyncTask.CameraCaptureRequest cameraCaptureRequest = new OnPreviewFrameAsyncTask.CameraCaptureRequest(data, camera, this.getContinuousCaptureOutputConfigurations());
            this.asyncTask.queue(cameraCaptureRequest);
        }
    }

    public final void setContinuousCaptureOutputConfigurations(final ContinuousCaptureOutputConfigurations continuousCaptureOutputConfigurations) {
        this.continuousCaptureOutputConfigurations = continuousCaptureOutputConfigurations;
    }

    public final void setReadyForCapture(final boolean shouldCapture) {
        Log.d(TAG, "setReadyForCapture() --> Setting readyForCapture = " + shouldCapture);
        this.readyForCapture = shouldCapture;
        Log.d(TAG, "setReadyForCapture() --> Finished setting readyForCapture = " + shouldCapture);
    }

    public final boolean getReadyForCapture() {
        return this.readyForCapture;
    }

    public final ContinuousCaptureOutputConfigurations getContinuousCaptureOutputConfigurations() {
        return this.continuousCaptureOutputConfigurations;
    }

}
