package com.habiti.app

import android.content.Context
import android.opengl.GLSurfaceView
import com.example.nativetools.NativeLib
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HabitCubeView(context: Context) : GLSurfaceView(context) {

    private val renderer = HabitCubeRenderer()
    private val nativeLib = NativeLib()

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateProgress(progress: Float) {
        renderer.updateProgress(progress)
    }

    private inner class HabitCubeRenderer : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            nativeLib.initRenderer()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            nativeLib.resizeRenderer(width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            nativeLib.drawFrame()
        }

        fun updateProgress(progress: Float) {
            nativeLib.updateProgress(progress)
        }
    }
}