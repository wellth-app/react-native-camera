package com.lwansbrough.RCTCamera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContinuousCaptureOutputConfigurations {

    public static final ContinuousCaptureOutputConfigurations getDefault() {
        return new ContinuousCaptureOutputConfigurations(new ContinuousCaptureOutputConfiguration("thumbnail", .5d, .5d, .5d), new ContinuousCaptureOutputConfiguration("fullRes", 1d, 1d, 1d));
    }

    private final List<ContinuousCaptureOutputConfiguration> continuousCaptureOutputConfigurationList = new ArrayList<>();

    private ContinuousCaptureOutputConfigurations() {
        this.continuousCaptureOutputConfigurationList.clear();
    }

    public ContinuousCaptureOutputConfigurations(final ContinuousCaptureOutputConfiguration...configurations) {
        this.continuousCaptureOutputConfigurationList.clear();
        this.continuousCaptureOutputConfigurationList.addAll(Arrays.asList(configurations));
    }

    public ContinuousCaptureOutputConfiguration getConfiguration(final int index) {
        return (index < this.continuousCaptureOutputConfigurationList.size()) ? this.continuousCaptureOutputConfigurationList.get(index) : null;
    }

    public int getSize() {
        return this.continuousCaptureOutputConfigurationList.size();
    }

    public void clear() {
        this.continuousCaptureOutputConfigurationList.clear();
    }

    public void addConfiguration(final ContinuousCaptureOutputConfiguration configuration) {
        if (!this.continuousCaptureOutputConfigurationList.contains(configuration)) {
            this.continuousCaptureOutputConfigurationList.add(configuration);
        }
    }

}
