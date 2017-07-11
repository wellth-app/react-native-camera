package com.lwansbrough.RCTCamera;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.uimanager.*;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.wellthapp.ContinuousRCTCamera.ContinuousCaptureOutputConfiguration;
import com.wellthapp.ContinuousRCTCamera.ContinuousCaptureOutputConfigurations;

import java.util.List;
import java.util.ArrayList;

public class RCTCameraViewManager extends ViewGroupManager<RCTCameraView> {

    private static final String REACT_CLASS = "RCTCamera";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public RCTCameraView createViewInstance(ThemedReactContext context) {
        return new RCTCameraView(context);
    }

    @ReactProp(name = "aspect")
    public void setAspect(RCTCameraView view, int aspect) {
        view.setAspect(aspect);
    }

    @ReactProp(name = "captureMode")
    public void setCaptureMode(RCTCameraView view, final int captureMode) {
        // Note that this in practice only performs any additional setup necessary for each mode;
        // the actual indication to capture a still or record a video when capture() is called is
        // still ultimately decided upon by what it in the options sent to capture().
        view.setCaptureMode(captureMode);
    }

    @ReactProp(name = "captureTarget")
    public void setCaptureTarget(RCTCameraView view, int captureTarget) {
        // No reason to handle this props value here since it's passed again to the RCTCameraModule capture method
    }

    @ReactProp(name = "type")
    public void setType(RCTCameraView view, int type) {
        view.setCameraType(type);
    }

    @ReactProp(name = "captureQuality")
    public void setCaptureQuality(RCTCameraView view, String captureQuality) {
        view.setCaptureQuality(captureQuality);
    }

    @ReactProp(name = "torchMode")
    public void setTorchMode(RCTCameraView view, int torchMode) {
        view.setTorchMode(torchMode);
    }

    @ReactProp(name = "flashMode")
    public void setFlashMode(RCTCameraView view, int flashMode) {
        view.setFlashMode(flashMode);
    }

    @ReactProp(name = "orientation")
    public void setOrientation(RCTCameraView view, int orientation) {
        view.setOrientation(orientation);
    }

    @ReactProp(name = "captureAudio")
    public void setCaptureAudio(RCTCameraView view, boolean captureAudio) {
        // TODO - implement video mode
    }

    @ReactProp(name = "barcodeScannerEnabled")
    public void setBarcodeScannerEnabled(RCTCameraView view, boolean barcodeScannerEnabled) {
        view.setBarcodeScannerEnabled(barcodeScannerEnabled);
    }

    @ReactProp(name = "barCodeTypes")
    public void setBarCodeTypes(RCTCameraView view, ReadableArray barCodeTypes) {
        if (barCodeTypes == null) {
            return;
        }
        List<String> result = new ArrayList<String>(barCodeTypes.size());
        for (int i = 0; i < barCodeTypes.size(); i++) {
            result.add(barCodeTypes.getString(i));
        }
        view.setBarCodeTypes(result);
    }

    @ReactProp(name = "readyForCapture")
    public void setReadyForCapture(RCTCameraView view, boolean shouldCapture) {
        view.setReadyForCapture(shouldCapture);
    }

    @ReactProp(name = "continuousCapture")
    public void setContinuousCapture(RCTCameraView view, boolean continuousCapture) {
        view.setContinuousCapture(continuousCapture);
    }

    @ReactProp(name = "continuousCaptureOutputConfiguration")
    public void continuousCaptureOutputConfiguration(RCTCameraView view, ReadableArray continuousCaptureOutputConfigurations) {

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

            view.setContinuousCaptureOutputConfigurations(result);

        }
    }

}
