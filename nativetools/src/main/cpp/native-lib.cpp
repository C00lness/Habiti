#include <jni.h>
#include <android/log.h>
#include "renderer.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define LOG_TAG "NativeTools"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

AAssetManager* gAssetManager = nullptr;

// AssetManager
extern "C" JNIEXPORT void JNICALL
Java_com_example_nativetools_NativeLib_setAssetManager(
        JNIEnv* env,
        jclass clazz,
        jobject assetManager) {
    gAssetManager = AAssetManager_fromJava(env, assetManager);
    LOGD("✅ AssetManager set");
}

// Создание рендерера
extern "C" JNIEXPORT jlong JNICALL
Java_com_example_nativetools_NativeLib_createRenderer(
        JNIEnv* env,
        jobject /* this */) {
    Renderer* renderer = new Renderer();
    LOGD("✅ Renderer created: %p", renderer);
    return reinterpret_cast<jlong>(renderer);
}

// Инициализация рендерера (вызывается когда есть OpenGL контекст)
extern "C" JNIEXPORT void JNICALL
Java_com_example_nativetools_NativeLib_initRenderer(
        JNIEnv* env,
        jobject /* this */,
        jlong ptr) {
    Renderer* renderer = reinterpret_cast<Renderer*>(ptr);
    if (renderer) {
        renderer->init();
        LOGD("✅ Renderer init: %p", renderer);
    }
}

// Обновление прогресса
extern "C" JNIEXPORT void JNICALL
Java_com_example_nativetools_NativeLib_updateRendererProgress(
        JNIEnv* env,
        jobject /* this */,
        jlong ptr,
        jfloat progress) {
    Renderer* renderer = reinterpret_cast<Renderer*>(ptr);
    if (renderer) {
        renderer->updateProgress(progress);
        LOGD("📊 Progress updated: %.2f", progress);
    }
}

// Рисование кадра
extern "C" JNIEXPORT void JNICALL
Java_com_example_nativetools_NativeLib_drawRendererFrame(
        JNIEnv* env,
        jobject /* this */,
        jlong ptr) {
    Renderer* renderer = reinterpret_cast<Renderer*>(ptr);
    if (renderer) {
        renderer->drawFrame();
    }
}

// Изменение размера
extern "C" JNIEXPORT void JNICALL
Java_com_example_nativetools_NativeLib_resizeRenderer(
        JNIEnv* env,
        jobject /* this */,
        jlong ptr,
        jint width,
        jint height) {
    Renderer* renderer = reinterpret_cast<Renderer*>(ptr);
    if (renderer) {
        renderer->resize(width, height);
        LOGD("📐 Renderer resized: %dx%d", width, height);
    }
}

// Уничтожение рендерера
extern "C" JNIEXPORT void JNICALL
Java_com_example_nativetools_NativeLib_destroyRenderer(
        JNIEnv* env,
        jobject /* this */,
        jlong ptr) {
    Renderer* renderer = reinterpret_cast<Renderer*>(ptr);
    if (renderer) {
        LOGD("✅ Renderer destroyed: %p", renderer);
        delete renderer;
    }
}

// Остальные методы
extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_nativetools_NativeLib_calculateStability(
        JNIEnv *env,
        jobject /* this */,
        jintArray completions) {
    // Получаем длину массива
    jsize length = env->GetArrayLength(completions);
    if (length == 0) return 0.0;

// Получаем указатель на элементы массива
    jint *arr = env->GetIntArrayElements(completions, nullptr);
    if (arr == nullptr) return 0.0;

// Считаем сумму
    jint sum = 0;
    for (int i = 0; i < length; i++) {
        sum += arr[i];
    }

// Возвращаем элементы массива обратно (не меняли их)
    env->ReleaseIntArrayElements(completions, arr, JNI_ABORT);

// Процент = (сумма * 100) / длина
    return (jdouble) (sum * 100) / length;
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_example_nativetools_NativeLib_calculateCorrelation(
        JNIEnv* env,
        jobject /* this */,
        jdoubleArray xArray,
        jdoubleArray yArray) {
    jsize length = env->GetArrayLength(xArray);
    if (length == 0 || env->GetArrayLength(yArray) != length) {
        return 0.0;
    }

    // 👇 Получаем данные
    jdouble* x = env->GetDoubleArrayElements(xArray, nullptr);
    jdouble* y = env->GetDoubleArrayElements(yArray, nullptr);

    if (x == nullptr || y == nullptr) {
        if (x) env->ReleaseDoubleArrayElements(xArray, x, JNI_ABORT);
        if (y) env->ReleaseDoubleArrayElements(yArray, y, JNI_ABORT);
        return 0.0;
    }

    // 👇 Если данных мало — возвращаем ПРОЦЕНТ ВЫПОЛНЕНИЯ (стабильность)
    if (length < 3) {
        double sum = 0.0;
        for (int i = 0; i < length; i++) {
            sum += y[i];
        }
        double result = (sum / length) * 100.0; // 0..100%
        env->ReleaseDoubleArrayElements(xArray, x, JNI_ABORT);
        env->ReleaseDoubleArrayElements(yArray, y, JNI_ABORT);
        return result;
    }

    // 👇 Для 3+ дней — стандартная корреляция Пирсона
    double sumX = 0.0, sumY = 0.0;
    for (int i = 0; i < length; i++) {
        sumX += x[i];
        sumY += y[i];
    }
    double meanX = sumX / length;
    double meanY = sumY / length;

    double sumXY = 0.0, sumX2 = 0.0, sumY2 = 0.0;
    for (int i = 0; i < length; i++) {
        double dx = x[i] - meanX;
        double dy = y[i] - meanY;
        sumXY += dx * dy;
        sumX2 += dx * dx;
        sumY2 += dy * dy;
    }

    env->ReleaseDoubleArrayElements(xArray, x, JNI_ABORT);
    env->ReleaseDoubleArrayElements(yArray, y, JNI_ABORT);

    double denominator = sqrt(sumX2 * sumY2);
    if (denominator == 0.0) return 0.0;

    // Преобразуем корреляцию (-1..1) в проценты (0..100)
    double correlation = sumXY / denominator;
    return (correlation + 1.0) / 2.0 * 100.0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_example_nativetools_NativeLib_add(
        JNIEnv *env,
        jobject /* this */,
        jint a,
        jint b) {
    // ... ваш код
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativetools_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    return env->NewStringUTF("Hello from C++!");
}