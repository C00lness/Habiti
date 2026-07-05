package com.example.nativetools;

import android.content.res.AssetManager;

public class NativeLib {
    static {
        System.loadLibrary("nativetools");
    }

    // Старые методы
    public native String stringFromJNI();
    public native int add(int a, int b);
    public native double calculateStability(int[] completions);
    public native double calculateCorrelation(double[] days, double[] completed);

    // Статический метод для AssetManager
    public static native void setAssetManager(AssetManager assetManager);

    // Методы для рендерера (ВСЕ ДОЛЖНЫ БЫТЬ public native)
    public native long createRenderer();
    public native void initRenderer(long ptr);
    public native void updateRendererProgress(long ptr, float progress);
    public native void drawRendererFrame(long ptr);
    public native void resizeRenderer(long ptr, int width, int height);
    public native void destroyRenderer(long ptr);
}