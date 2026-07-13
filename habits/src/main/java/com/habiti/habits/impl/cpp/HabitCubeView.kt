package com.habiti.habits.impl.cpp

import android.content.Context
import android.opengl.GLSurfaceView
import com.example.nativetools.NativeLib
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HabitCubeView(context: Context) : GLSurfaceView(context) {

    private val nativeLib = NativeLib()
    private var rendererPtr: Long = 0
    private var modelPath: String = ""

    init {
        NativeLib.setAssetManager(context.assets)
        setEGLContextClientVersion(2)
        setRenderer(HabitCubeRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
        setZOrderOnTop(true)
        holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)
        setBackgroundColor(android.graphics.Color.TRANSPARENT)

        android.util.Log.d("HabitCubeView", "✅ View created")
    }

    fun loadModel(path: String) {
        android.util.Log.d("HabitCubeView", "📦 loadModel called: $path")
        modelPath = path
        if (rendererPtr != 0L) {
            android.util.Log.d("HabitCubeView", "📦 Loading model via native: $path")
            NativeLib.loadRendererModel(rendererPtr, path)
        } else {
            android.util.Log.d("HabitCubeView", "⚠️ rendererPtr is 0, model will be loaded later")
        }
    }

    fun setScale(scale: Float) {
        android.util.Log.d("HabitCubeView", "📐 setScale: $scale")
        if (rendererPtr != 0L) {
            NativeLib.setScale(rendererPtr, scale)
        }
    }

    fun updateProgress(progress: Float) {
        if (rendererPtr != 0L) {
            nativeLib.updateRendererProgress(rendererPtr, progress)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (rendererPtr != 0L) {
            nativeLib.destroyRenderer(rendererPtr)
            rendererPtr = 0
        }
    }

    private inner class HabitCubeRenderer : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            android.util.Log.d("HabitCubeView", "🟢 onSurfaceCreated")
            if (rendererPtr == 0L) {
                rendererPtr = nativeLib.createRenderer()
                android.util.Log.d("HabitCubeView", "🟢 Renderer created: $rendererPtr")
                nativeLib.initRenderer(rendererPtr)

                // Загружаем модель, если она была установлена
                if (modelPath.isNotEmpty()) {
                    android.util.Log.d("HabitCubeView", "📦 Loading model: $modelPath")
                    NativeLib.loadRendererModel(rendererPtr, modelPath)
                }
            }
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            android.util.Log.d("HabitCubeView", "📐 onSurfaceChanged: $width x $height")
            if (rendererPtr != 0L) {
                nativeLib.resizeRenderer(rendererPtr, width, height)
            }
        }

        override fun onDrawFrame(gl: GL10?) {
            if (rendererPtr != 0L) {
                nativeLib.drawRendererFrame(rendererPtr)
            }
        }
    }
}