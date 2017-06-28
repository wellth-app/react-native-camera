package com.wellthapp.ContinuousRCTCamera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import static com.wellthapp.ContinuousRCTCamera.CameraPreviewCallback.getFile;

public class OnPreviewFrameAsyncTask extends AsyncTask<Void, Void, Void> {

    public static class CameraCaptureRequest {
        private byte[] bytes;
        private Camera camera;
        private final ContinuousCaptureOutputConfigurations continuousCaptureOutputConfigurations;

        public CameraCaptureRequest(final byte[] bytes, final Camera camera, final ContinuousCaptureOutputConfigurations continuousCaptureOutputConfigurations) {
            this.bytes = bytes;
            this.camera = camera;
            this.continuousCaptureOutputConfigurations = continuousCaptureOutputConfigurations;
        }
        public byte[] getBytes() {
            return this.bytes;
        }
        public Camera getCamera() {
            return this.camera;
        }

        public ContinuousCaptureOutputConfigurations getContinuousCaptureOutputConfigurations() {
            return this.continuousCaptureOutputConfigurations;
        }

    }

    private final ReactContext reactContext;
    private final LinkedBlockingQueue<CameraCaptureRequest> queue = new LinkedBlockingQueue<>();

    private volatile boolean isRunning = false;
    private volatile boolean proceed = true;

    public OnPreviewFrameAsyncTask(final ReactContext reactContext) {
        this.reactContext = reactContext;
    }

    public final void queue(final CameraCaptureRequest cameraCaptureRequest) {
        try {
            queue.put(cameraCaptureRequest);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        int frameCounter = 0;

        while(this.proceed) {

            // Unpack the picture bytes
            final CameraCaptureRequest cameraCaptureRequest;
            try {

                // Unpack the byte array wrapper
                cameraCaptureRequest = queue.take();
                final byte[] bytes = cameraCaptureRequest.getBytes();
                final Camera camera = cameraCaptureRequest.getCamera();
                final ContinuousCaptureOutputConfigurations configurations = cameraCaptureRequest.getContinuousCaptureOutputConfigurations();

                // Determine the camera settings
                final Camera.Parameters parameters = camera.getParameters();
                final int previewWidth = parameters.getPreviewSize().width;
                final int previewHeight = parameters.getPreviewSize().height;
                final int imageFormat = parameters.getPreviewFormat();
                final int frameRate = parameters.getPreviewFrameRate();

                // Do the actual save
                this.saveImageAndEmitEvent(bytes, previewWidth, previewHeight, configurations);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                frameCounter += 1;
            }
        }
        return null;
    }

    private void saveImageAndEmitEvent(final byte[] bytes, final int width, final int height, final ContinuousCaptureOutputConfigurations configurations) {
        final WritableMap event = Arguments.createMap();

        if (configurations != null) {
            final int numberOfConfigurations = configurations.getSize();
            for (int i = 0; i < numberOfConfigurations; i++) {
                final ContinuousCaptureOutputConfiguration configuration = configurations.getConfiguration(i);
                final File savedFile = this.saveImageWithConfiguration(bytes, width, height, configuration);
                event.putString(configuration.name, savedFile.getAbsolutePath());
            }
        }

        // Actually emit the event here
        this.reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(0, "ContinuousCaptureOutput", event);
    }

    public File saveImageWithConfiguration(final byte[] data, int width, int height, final ContinuousCaptureOutputConfiguration configuration) {
        if (configuration != null) {
            final int adjustedHeight = (int)(height * configuration.height);
            final int adjustedWidth = (int)(width * configuration.width);
            Rect rect = new Rect(0, 0, adjustedWidth, adjustedHeight);
            YuvImage img = new YuvImage(data, ImageFormat.NV21, adjustedWidth, adjustedHeight, null);
            OutputStream outStream = null;
            File file = getFile(configuration.name);
            try {
                outStream = new FileOutputStream(file);
                img.compressToJpeg(rect, ((int)configuration.quality * 100), outStream);
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        } else {
            Log.w("PreviewFrameAsyncTask", "The configuration for save image was null!");
            return null;
        }
    }

    public boolean isRunning() {
        synchronized (this) {
            return this.isRunning;
        }
    }

    public final void start() {
        synchronized (this) {
            this.isRunning = true;
        }
        this.execute();
    }

    public final void stop() {
        synchronized (this) {
            this.proceed = false;
            this.isRunning = false;
        }
        this.queue.clear();
        this.cancel(true);
    }

}
