package com.lwansbrough.RCTCamera;

public class ContinuousCaptureOutputConfiguration {

    public final String name;
    public final double height;
    public final double width;
    public final double quality;

    public ContinuousCaptureOutputConfiguration(final String name, final double height, final double width, final double quality) {
        this.name = name;
        this.height = height;
        this.width = width;
        this.quality = quality;
    }

}
