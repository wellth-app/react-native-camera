package com.wellthapp.ContinuousRCTCamera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;

import static com.wellthapp.ContinuousRCTCamera.CameraPreviewCallback.getFile;

public class OnPreviewFrameAsyncTask extends AsyncTask<Void, Void, Void> {

    public static class ByteArrayCameraTuple {
        private byte[] bytes;
        private Camera camera;

        public ByteArrayCameraTuple(final byte[] bytes, final Camera camera) {
            this.bytes = bytes;
            this.camera = camera;
        }
        public byte[] getBytes() {
            return this.bytes;
        }
        public Camera getCamera() {
            return this.camera;
        }
    }

    private final ClarifaiClient client = new ClarifaiBuilder("appID", "appSecret").buildSync();
    private final LinkedBlockingQueue<ByteArrayCameraTuple> queue = new LinkedBlockingQueue<>();
    private final int captureInterval;
    private final int positiveIdentificationTimeout;
    private final List<String> acceptedTags;

    private boolean isRunning = false;
    private boolean proceed = true;

    public OnPreviewFrameAsyncTask(final int captureInterval, final int positiveIdentificationTimeout, final List<String> acceptedTags) {
        this.captureInterval = captureInterval;
        this.positiveIdentificationTimeout = positiveIdentificationTimeout;
        this.acceptedTags = acceptedTags;
    }

    @Override
    protected Void doInBackground(Void... params) {

        int frameCounter = 0;

        while(this.proceed) {

            // Unpack the picture bytes
            final ByteArrayCameraTuple byteArrayCameraTuple;
            try {
                byteArrayCameraTuple = queue.take();
                final byte[] bytes = byteArrayCameraTuple.getBytes();
                final Camera camera = byteArrayCameraTuple.getCamera();


                final Camera.Parameters parameters = camera.getParameters();
                final int width = parameters.getPreviewSize().width;
                final int height = parameters.getPreviewSize().height;
                final int imageFormat = parameters.getPreviewFormat();
                final int frameRate = parameters.getPreviewFrameRate();


                final int milisecondsPerFrame = 1000 / frameRate;


                if (this.captureInterval < milisecondsPerFrame) {
                    this.captureInterval = milisecondsPerFrame;
                }

                final int triggerEvery = this.captureInterval / milisecondsPerFrame;

                if (frameCounter % triggerEvery == 0) {
                    this.clarifaiImage(bytes);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                frameCounter += 1;
            }
        }
        return null;
    }

    public final void queue(final byte[] bytes, final Camera camera) {
        try {
            this.queue.put(new ByteArrayCameraTuple(bytes, camera));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public final void stop() {
        this.proceed = false;
        this.queue.clear();
    }

    public final void clarifaiImage(final byte[] bytes) {
        final ClarifaiImage clarifaiImage = ClarifaiImage.of(bytes);
        this.client.getDefaultModels().generalModel().predict().withInputs(ClarifaiInput.forImage(clarifaiImage)).executeSync();
    }

    public void saveNV21Image(final byte[] data, int width, int height, final File customFile) {
        Rect rect = new Rect(0, 0, width, height);
        YuvImage img = new YuvImage(data, ImageFormat.NV21, width, height, null);
        OutputStream outStream = null;
        File file = (customFile != null) ? customFile : getFile();
        try {
            outStream = new FileOutputStream(file);
            img.compressToJpeg(rect, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.isRunning = true;
        this.execute();
    }

}
