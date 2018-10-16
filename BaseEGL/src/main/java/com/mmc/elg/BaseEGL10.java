package com.mmc.elg;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by yangjunfei on 7/9/18
 */
public class BaseEGL10 extends BaseEGL{
    private static final String TAG = "BaseEGL";

    private EGL10 mEGL;
    private EGLDisplay mEGLDisplay = EGL10.EGL_NO_DISPLAY;
    private EGLConfig[] mEGLConfig = new EGLConfig[1];
    private EGLSurface mEglSurface;
    private EGLContext mEGLContext = EGL10.EGL_NO_CONTEXT;

    private BaseEGLContext mBaseEGLContext;

    public BaseEGL10() {
    }

    public int init(BaseEGLContext sharedContext, int[] attributes) {


        mEGL = (EGL10) EGLContext.getEGL();

        mEGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
            Log.d(TAG, "eglGetDisplay failed. " + mEGL.eglGetError());
            return mEGL.eglGetError();
        }

        int[] version = new int[2];
        if (!mEGL.eglInitialize(mEGLDisplay, version)) {
            Log.d(TAG, "eglGetDisplay failed." + mEGL.eglGetError());
            return mEGL.eglGetError();
        }

        Log.d(TAG, "major : " + version[0] + ", minor : " + version[1]);

        int[] configsNum = new int[1];
        if (!mEGL.eglChooseConfig(mEGLDisplay, attributes, mEGLConfig, mEGLConfig.length, configsNum)) {
            Log.d(TAG, "eglChooseConfig failed, " + mEGL.eglGetError());
            return mEGL.eglGetError();
        }

        //指定哪个版本的OpenGL ES上下文，本文为OpenGL ES 2.0
        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };

        //创建上下文，EGL10.EGL_NO_CONTEXT表示不和别的上下文共享资源

        EGLContext eglSharedContext = (sharedContext == null) ? EGL10.EGL_NO_CONTEXT : sharedContext.getEGL10Context();
        mEGLContext = mEGL.eglCreateContext(mEGLDisplay, mEGLConfig[0], eglSharedContext, contextAttribs);
        if (mEGLContext == EGL10.EGL_NO_CONTEXT){
            Log.d(TAG, "eglCreateContext failed, " + mEGL.eglGetError());
            return mEGL.eglGetError();
        }

        mBaseEGLContext = new BaseEGLContext(mEGLContext);
        Log.d(TAG, "init EGL fully." + mEGL.eglGetError());
        return 0;
    }

    public BaseEGLContext getEGLContext() {
        return mBaseEGLContext;
    }

    public void createEGLSuface(SurfaceTexture surfaceTexture) {
        int[] surfaceAttribs = {EGL10.EGL_NONE};
        mEglSurface = mEGL.eglCreateWindowSurface(mEGLDisplay, mEGLConfig[0], surfaceTexture, surfaceAttribs);
    }

    public void createPbBufferSuface(int width, int height) {
        final int[] attributesForPBuffer = {
                EGL10.EGL_WIDTH, width,
                EGL10.EGL_HEIGHT, height,
                EGL14.EGL_NONE};
        mEglSurface = mEGL.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig[0], attributesForPBuffer);
    }

    public boolean makeCurrent() {
        return mEGL.eglMakeCurrent(mEGLDisplay, mEglSurface, mEglSurface, mEGLContext);
    }

    @Override
    public boolean releaseCurrent() {
        return mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
    }

    public void swapBuffers() {
        mEGL.eglSwapBuffers(mEGLDisplay, mEglSurface);
    }

    public void uninit() {
        if (mEglSurface != null) {
            mEGL.eglDestroySurface(mEGLDisplay, mEglSurface);
        }
        if (mEGLContext != null) {
            mEGL.eglDestroyContext(mEGLDisplay, mEGLContext);
        }
        mEGLDisplay = null;
        mEglSurface = null;
        mEGLContext = null;
        mEGL = null;
    }
}
