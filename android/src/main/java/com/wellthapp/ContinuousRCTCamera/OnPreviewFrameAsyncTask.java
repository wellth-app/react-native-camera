package com.wellthapp.ContinuousRCTCamera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

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

    private final Promise promise;
    private final ClarifaiClient client = new ClarifaiBuilder("99iB8g9zmTF_LHw2OfgWrpmLvh16W6IVfcILX1Gx", "IgakOLs1ijRMcfUSVTripcWJx_bBE0DDHW0GouFC").buildSync();
    private final LinkedBlockingQueue<ByteArrayCameraTuple> queue = new LinkedBlockingQueue<>();
    private final int positiveIdentificationTimeout;
    private final List<String> acceptedTags;
    private float requiredConfidence = 0.8f;
    private int captureInterval;

    private volatile boolean isRunning = false;
    private volatile boolean proceed = true;
    private volatile boolean shouldCapture = false;

    public OnPreviewFrameAsyncTask(final int captureInterval, final int positiveIdentificationTimeout, final List<String> acceptedTags, final Promise promise) {
        this.captureInterval = captureInterval;
        this.positiveIdentificationTimeout = positiveIdentificationTimeout;
        this.acceptedTags = acceptedTags;
        this.promise = promise;
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

                // Make sure that we can't have an interval that is faster than the camera can
                // supply frames
                if (this.captureInterval < milisecondsPerFrame) {
                    this.captureInterval = milisecondsPerFrame;
                }

                // Calculate the number of frames that should pass between each trigger
                final int triggerEvery = this.captureInterval / milisecondsPerFrame;

                // If it's time to trigger...
                if (frameCounter % triggerEvery == 0) {
                    Log.d("RCTCamera", "Time to Clarifai!");

                    if(this.clarifaiImage(bytes)) {
                        this.saveImageAndResolvePromise(bytes, width, height, promise);
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                frameCounter += 1;
            }
        }
        return null;
    }

    private void saveImageAndResolvePromise(final byte[] bytes, final int width, final int height, final Promise promise) {
        final File savedImageFile = this.saveNV21Image(bytes, width, height, null);

        final WritableMap writableMap = Arguments.createMap();
        writableMap.putString("image", savedImageFile.getAbsolutePath());
        promise.resolve(writableMap);
    }

    public final void queue(final byte[] bytes, final Camera camera) {
        try {
            this.queue.put(new ByteArrayCameraTuple(bytes, camera));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public final void stop() {
        synchronized (this) {
            this.isRunning = true;
            this.proceed = false;
        }
        this.queue.clear();
    }

    public final boolean clarifaiImage(final byte[] bytes) {
        final ClarifaiImage clarifaiImage = ClarifaiImage.of(bytes);
        final ClarifaiResponse<List<ClarifaiOutput<Concept>>> response = this.client.getDefaultModels().generalModel().predict().withInputs(ClarifaiInput.forImage(clarifaiImage)).executeSync();
        final List<ClarifaiOutput<Concept>> predictionsList = response.get();

        if (response.isSuccessful()) {
            if (!predictionsList.isEmpty()) {
                final List<Concept> conceptList = predictionsList.get(0).data();

                ClarifaiHelper.dumpConceptList(conceptList);

                final Map<String, Float> conceptMap = ClarifaiHelper.toConceptMap(conceptList);

                for (final String acceptedTag : this.acceptedTags) {
                    if (conceptMap.containsKey(acceptedTag) && conceptMap.get(acceptedTag) >= this.requiredConfidence) {
                        return true;
                    }
                }

            } else {
                // Predictions list was empty
                return false;
            }
        } else {
            // Response was not successful
            return false;
        }

        return false;

    }


    public File saveNV21Image(final byte[] data, int width, int height, final File customFile) {
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
        return file;
    }

    public boolean isRunning() {
        synchronized (this) {
            return this.isRunning;
        }
    }

    public void start() {
        synchronized (this) {
            this.isRunning = true;
        }
        this.execute();
    }

}
