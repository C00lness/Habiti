#include <jni.h>
#include <android/log.h>

#define LOG_TAG "NativeTools"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// Существующая функция
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativetools_NativeLib_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    return env->NewStringUTF("Hello from C++!");
}

// 👇 НОВАЯ ФУНКЦИЯ
extern "C" JNIEXPORT jint JNICALL
Java_com_example_nativetools_NativeLib_add(
        JNIEnv* env,
        jobject /* this */,
        jint a,
        jint b) {

    LOGD("Native add called with %d + %d", a, b);
    int result = a + b;
    LOGD("Result: %d", result);
    return result;
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_nativetools_NativeLib_calculateStability(
        JNIEnv* env,
jobject /* this */,
jintArray completions) {

// Получаем длину массива
jsize length = env->GetArrayLength(completions);
if (length == 0) return 0.0;

// Получаем указатель на элементы массива
jint* arr = env->GetIntArrayElements(completions, nullptr);
if (arr == nullptr) return 0.0;

// Считаем сумму
jint sum = 0;
for (int i = 0; i < length; i++) {
sum += arr[i];
}

// Возвращаем элементы массива обратно (не меняли их)
env->ReleaseIntArrayElements(completions, arr, JNI_ABORT);

// Процент = (сумма * 100) / длина
return (jdouble)(sum * 100) / length;
}