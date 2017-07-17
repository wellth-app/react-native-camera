package com.lwansbrough.RCTCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import static com.lwansbrough.RCTCamera.CameraPreviewCallback.getFile;

public class OnPreviewFrameAsyncTask extends AsyncTask<Void, Void, WritableMap> {

    public static final String TAG = "OnPreviewFrameAsyncTask";

    public static class CameraCaptureRequest {
        private byte[] bytes;
        private Camera camera;
        private final ContinuousCaptureOutputConfigurations continuousCaptureOutputConfigurations;

        public CameraCaptureRequest(final byte[] bytes, final Camera camera, final ContinuousCaptureOutputConfigurations continuousCaptureOutputConfigurations) {
            this.bytes = bytes;
            this.camera = camera;
            this.continuousCaptureOutputConfigurations = continuousCaptureOutputConfigurations;
            Log.d(TAG, "CameraCaptureRequest() --> Initialized a camera capture request!");

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
    private final Handler handler;

    private volatile boolean isRunning = false;
    private volatile boolean proceed = true;

    public OnPreviewFrameAsyncTask(final ReactContext reactContext) {
        this.reactContext = reactContext;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public final void queue(final CameraCaptureRequest cameraCaptureRequest) {
        if (cameraCaptureRequest != null) {
            Log.d(TAG, "Capturing from a non-null request");
        } else {
            Log.d(TAG, "Capturing request was null!");
        }
        try {
            if (this.queue.size() == 0) {
                Log.d(TAG, "Putting the capture request in the queue!");
                queue.put(cameraCaptureRequest);
                Log.d(TAG, "Capture request is now in the queue!");
            } else {
                Log.d(TAG, "Didn't put the capture request in the queue!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected WritableMap doInBackground(Void... params) {

        while(this.proceed) {

            // Unpack the picture bytes
            final CameraCaptureRequest cameraCaptureRequest;
            try {

                // Unpack the byte array wrapper
                Log.d(TAG, "doInBackground() --> Prepping to take a camera capture request from the queue...");
                cameraCaptureRequest = queue.take();
                Log.d(TAG, "doInBackground() --> Took a camera capture request from the queue...");
                final byte[] bytes = cameraCaptureRequest.getBytes();
                final Camera camera = cameraCaptureRequest.getCamera();
                final ContinuousCaptureOutputConfigurations configurations = cameraCaptureRequest.getContinuousCaptureOutputConfigurations();

                // Determine the camera settings
                final Camera.Parameters parameters = camera.getParameters();
                final int previewWidth = parameters.getPreviewSize().width;
                final int previewHeight = parameters.getPreviewSize().height;

                // Do the actual save
                this.emitEvent(this.saveImageAndEmitEvent(bytes, previewWidth, previewHeight, configurations));

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final WritableMap writableMap) {
        this.emitEvent(writableMap);
        Log.d(TAG, "Emitted event on main thread");
    }

    private void emitEvent(final WritableMap writableMap) {
        Log.d(TAG, "Emitting event on main thread the new way");
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("ContinuousCaptureOutput", writableMap);
            }
        });
    }

    private WritableMap saveImageAndEmitEvent(final byte[] bytes, final int width, final int height, final ContinuousCaptureOutputConfigurations configurations) {

        Log.d(TAG, "saveImageAndEmitEvent() --> Saving image and emitting event with width = " + width + " and height = " + height);

        final WritableMap outputMap = Arguments.createMap();
        final WritableMap outputMap2 = Arguments.createMap();

        // Build output
        if (configurations != null) {
            final int numberOfConfigurations = configurations.getSize();
            for (int i = 0; i < numberOfConfigurations; i++) {
                final ContinuousCaptureOutputConfiguration configuration = configurations.getConfiguration(i);
                final File savedFile = this.saveImageWithConfiguration(bytes, width, height, configuration);
                Log.d(TAG, "Adding configuration with name == " + configuration.name + " && file = " + savedFile.getAbsolutePath());
                outputMap2.putString(configuration.name, savedFile.getAbsolutePath());
            }
            outputMap.putMap("output", outputMap2);
        }

        Log.d(TAG, "WritableMap == " + outputMap.toString());

        // Emit the event here
//        this.reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(0, "ContinuousCaptureOutput", outputMap);
//        RCTCameraModule.emitEvent(outputMap);

//        Log.d(TAG, "Emitted the writable map the new way!");


        return outputMap;

    }

    private ByteArrayOutputStream toByteArrayOutputStream(final byte[] data, final int width, final int height, final int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        Rect rect = new Rect(0, 0, width, height);
        yuvImage.compressToJpeg(rect, quality, out);
        return out;
//        byte[] imageBytes = out.toByteArray();
//        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public File saveImageWithConfiguration(final byte[] data, int width, int height, final ContinuousCaptureOutputConfiguration configuration) {
        if (configuration != null) {
            final int adjustedHeight = (int) (height * configuration.height);
            final int adjustedWidth = (int) (width * configuration.width);
            final double adjustedQuality = configuration.quality * 100d;

            // Get the image byte array output stream
            final ByteArrayOutputStream byteArrayOutputStream = toByteArrayOutputStream(data, width, height, (int)adjustedQuality);
            final File file = getFile(configuration.name);
            final OutputStream outputStream = new FileOutputStream(file);

            if (width == adjustedWidth && height == adjustedHeight) {
                // In this case, the image isn't scaled.  Save it as-is.
                try {
                    byteArrayOutputStream.writeTo(outputStream);
                    byteArrayOutputStream.flush();
                    outputStream.flush();
                    outputStream.close();
                    byteArrayOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // In this case, the image is scaled and we need to do some manipulation.
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Bitmap resizedBitmapImage = Bitmap.createScaledBitmap(bitmapImage, adjustedWidth, adjustedHeight, true);
                resizedBitmapImage.compress(Bitmap.CompressFormat.JPEG, (int)adjustedQuality, outputStream);
                outputStream.flush();
                outputStream.close();
            }
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
