package com.example.nativetools;

public class NativeLib {
    static {
        System.loadLibrary("nativetools");
    }

    public native String stringFromJNI();
    public native int add(int a, int b);
    public native double calculateStability(int[] completions);
    public native double calculateCorrelation(double[] days, double[] completed);

    public native void initRenderer();
    public native void updateProgress(float progress);
    public native void drawFrame();
    public native void resizeRenderer(int width, int height);
}