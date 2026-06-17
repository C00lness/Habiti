package com.example.nativetools;

public class NativeLib {

    // Used to load the 'nativetools' library on application startup.
    static {
        System.loadLibrary("nativetools");
    }

    /**
     * A native method that is implemented by the 'nativetools' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native int add(int a, int b);
    public native double calculateStability(int[] completions);
    public native double calculateCorrelation(double[] days, double[] completed);
}