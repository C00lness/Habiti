package com.habiti.habits.impl.cpp

import android.content.Context
import android.opengl.GLSurfaceView
import com.example.nativetools.NativeLib
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HabitCubeView(context: Context) : GLSurfaceView(context) {

    private val nativeLib = NativeLib()
    private val renderer = HabitCubeRenderer()
    private val rendererPtr: Long = nativeLib.createRenderer()

    init {
        NativeLib.setAssetManager(context.assets)
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        setZOrderOnTop(true)  // Поднимаем Surface поверх окна
        holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)  // Прозрачный формат
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
    }

    fun updateProgress(progress: Float) {
        nativeLib.updateRendererProgress(rendererPtr, progress)
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 👇 Очищаем C++ объект при уничтожении View
        nativeLib.destroyRenderer(rendererPtr)
    }
    private inner class HabitCubeRenderer : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            nativeLib.initRenderer(rendererPtr)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            nativeLib.resizeRenderer(rendererPtr, width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            nativeLib.drawRendererFrame(rendererPtr)
        }

//        fun updateProgress(progress: Float) {
//            nativeLib.updateProgress(progress)
//        }
    }
}