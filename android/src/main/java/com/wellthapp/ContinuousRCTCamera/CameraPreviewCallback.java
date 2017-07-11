package com.wellthapp.ContinuousRCTCamera;

import android.hardware.Camera;
import android.os.Environment;

import com.facebook.react.bridge.ReactContext;
import com.lwansbrough.RCTCamera.ObjectIDGenerator;

import java.io.File;

public class CameraPreviewCallback implements Camera.PreviewCallback {

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
    private volatile boolean shouldCapture = false;
    private volatile ContinuousCaptureOutputConfigurations continuousCaptureOutputConfigurations;

    public CameraPreviewCallback(final ReactContext reactContext) {
        this.asyncTask = new OnPreviewFrameAsyncTask(reactContext);
        this.asyncTask.start();
        this.continuousCaptureOutputConfigurations = ContinuousCaptureOutputConfigurations.getDefault();
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (this.getShouldCapture()) {
            if (!this.asyncTask.isRunning()) {
                this.asyncTask.start();
            }
            final OnPreviewFrameAsyncTask.CameraCaptureRequest cameraCaptureRequest = new OnPreviewFrameAsyncTask.CameraCaptureRequest(data, camera, this.getContinuousCaptureOutputConfigurations());
            this.asyncTask.queue(cameraCaptureRequest);
        }
    }

    public final void setContinuousCaptureOutputConfigurations(final ContinuousCaptureOutputConfigurations continuousCaptureOutputConfigurations) {
        synchronized (this) {
            this.continuousCaptureOutputConfigurations = continuousCaptureOutputConfigurations;
        }
    }

    public final void setReadyForCapture(final boolean shouldCapture) {
        synchronized (this) {
            this.shouldCapture = shouldCapture;
        }
    }

    public final boolean getShouldCapture() {
        synchronized (this) {
            return this.shouldCapture;
        }
    }

    public final ContinuousCaptureOutputConfigurations getContinuousCaptureOutputConfigurations() {
        synchronized (this) {
            return this.continuousCaptureOutputConfigurations;
        }
    }

}
