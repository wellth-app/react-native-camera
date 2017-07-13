package com.lwansbrough.RCTCamera;

import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.uimanager.*;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages an {@link RCTCameraView}.
 */
public final class RCTCameraViewManager extends ViewGroupManager<RCTCameraView> {

    public static final String TAG = "RCTCameraViewManager";

    private static final String REACT_CLASS = "RCTCamera";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public RCTCameraView createViewInstance(ThemedReactContext context) {
        return new RCTCameraView(context);
    }

    // ### Define the view props below ###

    @ReactProp(name = "aspect")
    public final void setAspect(final RCTCameraView view, final int aspect) {
        if (view != null) {
            view.setAspect(aspect);
        } else {
            Log.w(TAG, "Unable to set aspect because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "captureMode")
    public final void setCaptureMode(final RCTCameraView view, final int captureMode) {
        // Note that this in practice only performs any additional setup necessary for each mode;
        // the actual indication to capture a still or record a video when capture() is called is
        // still ultimately decided upon by what it in the options sent to capture().
        if (view != null) {
            view.setCaptureMode(captureMode);
        } else {
            Log.w(TAG, "Unable to set capture mode because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "captureTarget")
    public final void setCaptureTarget(final RCTCameraView view, final int captureTarget) {
        // No reason to handle this props value here since it's passed again to the RCTCameraModule capture method
    }

    @ReactProp(name = "type")
    public final void setType(final RCTCameraView view, final int type) {
        if (view != null) {
            view.setCameraType(type);
        } else {
            Log.w(TAG, "Unable to set type because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "captureQuality")
    public final void setCaptureQuality(final RCTCameraView view, final String captureQuality) {
        if (view != null) {
            view.setCaptureQuality(captureQuality);
        } else {
            Log.w(TAG, "Unable to set capture quality because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "torchMode")
    public final void setTorchMode(final RCTCameraView view, final int torchMode) {
        if (view != null) {
            view.setTorchMode(torchMode);
        } else {
            Log.w(TAG, "Unable to set capture quality because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "flashMode")
    public final void setFlashMode(final RCTCameraView view, final int flashMode) {
        if (view != null) {
            view.setFlashMode(flashMode);
        } else {
            Log.w(TAG, "Unable to set flash mode because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "orientation")
    public final void setOrientation(final RCTCameraView view, final int orientation) {
        if (view != null) {
            view.setOrientation(orientation);
        } else {
            Log.w(TAG, "Unable to set orientation because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "captureAudio")
    public final void setCaptureAudio(final RCTCameraView view, final boolean captureAudio) {
        // TODO: Implement stub
    }

    @ReactProp(name = "barcodeScannerEnabled")
    public final void setBarcodeScannerEnabled(final RCTCameraView view, final boolean barcodeScannerEnabled) {
        if (view != null) {
            view.setBarcodeScannerEnabled(barcodeScannerEnabled);
        } else {
            Log.w(TAG, "Unable to set barcode scanner enabled/disabled because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "barCodeTypes")
    public final void setBarCodeTypes(final RCTCameraView view, final ReadableArray barCodeTypes) {
        if (barCodeTypes == null) {
            return;
        }
        List<String> result = new ArrayList<String>(barCodeTypes.size());
        for (int i = 0; i < barCodeTypes.size(); i++) {
            result.add(barCodeTypes.getString(i));
        }
        if (view != null) {
            view.setBarCodeTypes(result);
        } else {
            Log.w(TAG, "Unable to set barcode types because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "readyForCapture")
    public final void setReadyForCapture(final RCTCameraView view, final boolean shouldCapture) {
        Log.d(TAG, "FIRST STEP: Setting readyForCapture = " + shouldCapture);
//        if (view != null) {
//            view.setReadyForCapture(shouldCapture);
//        } else {
//            Log.w(TAG, "Unable to set readyForCapture because the RCTCameraView was null!");
//        }
        RCTCamera.cameraPreviewCallback.setReadyForCapture(shouldCapture);
    }

    @ReactProp(name = "continuousCapture")
    public final void setContinuousCapture(final RCTCameraView view, final boolean continuousCapture) {
        if (view != null) {
            view.setContinuousCapture(continuousCapture);
        } else {
            Log.w(TAG, "Unable to set continuousCapture because the RCTCameraView was null!");
        }
    }

    @ReactProp(name = "continuousCaptureOutputConfiguration")
    public final void continuousCaptureOutputConfiguration(final RCTCameraView view, final ReadableArray continuousCaptureOutputConfigurations) {

        final ContinuousCaptureOutputConfigurations result = new ContinuousCaptureOutputConfigurations();

        if (continuousCaptureOutputConfigurations != null) {
            final int arraySize = continuousCaptureOutputConfigurations.size();
            for (int i = 0; i < arraySize; i++) {
                // Get the type of the current object so we know how to pull it out
                final ReadableType itemType = continuousCaptureOutputConfigurations.getType(i);

                // Really we only care about Map types
                if (itemType.equals(ReadableType.Map)) {
                    final ReadableMap item = continuousCaptureOutputConfigurations.getMap(i);
                    final String name = item.getString("name");
                    final double height = item.getDouble("height");
                    final double width = item.getDouble("width");
                    final double quality = item.getDouble("quality");
                    result.addConfiguration(new ContinuousCaptureOutputConfiguration(name, height, width, quality));
                }
            }

            if (view != null) {
                view.setContinuousCaptureOutputConfigurations(result);
            } else {
                Log.w(TAG, "Unable to set continuousCaptureOutputConfiguration because the RCTCameraView was null!");
            }

        }
    }

}
