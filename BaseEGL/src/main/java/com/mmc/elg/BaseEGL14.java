package com.mmc.elg;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;


/**
 * Created by yangjunfei on 7/9/18
 */
public class BaseEGL14 extends BaseEGL {

    private static final String TAG = "BaseEGL14";

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLConfig[] mEGLConfig = new EGLConfig[1];
    private EGLSurface mEglSurface;
    private BaseEGLContext mEGLContext = null;
    private EGLContext mEGL14Context;

    @Override
    public int init(BaseEGLContext context, int[] attributes) {

        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "eglGetDisplay failed. " + EGL14.eglGetError());
            return EGL14.eglGetError();
        }
        Log.d(TAG, "doInit---->2");
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            Log.d(TAG, "eglGetDisplay failed." + EGL14.eglGetError());
            return EGL14.eglGetError();
        }

        Log.d(TAG, "major : " + version[0] + ", minor : " + version[1]);

        Log.d(TAG, "doInit---->3");
        int[] configsNum = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attributes, 0, mEGLConfig,0,  mEGLConfig.length, configsNum, 0)) {
            Log.d(TAG, "eglChooseConfig failed, " + EGL14.eglGetError());
            return EGL14.eglGetError();
        }

        Log.d(TAG, "doInit---->5");
        //指定哪个版本的OpenGL ES上下文，本文为OpenGL ES 2.0
        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };

        Log.d(TAG, "doInit---->6");
        //创建上下文，EGL10.EGL_NO_CONTEXT表示不和别的上下文共享资源
        EGLContext sharedContext = context == null ? EGL14.EGL_NO_CONTEXT : context.getEGL14Context();
        mEGL14Context = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig[0], sharedContext, contextAttribs, 0);
        if (mEGL14Context == EGL14.EGL_NO_CONTEXT){
            Log.d(TAG, "eglCreateContext failed, " + EGL14.eglGetError());
            return EGL14.eglGetError();
        }
        mEGLContext = new BaseEGLContext(mEGL14Context);

        Log.d(TAG, "init EGL fully." + EGL14.eglGetError());
        return 0;
    }

    @Override
    public BaseEGLContext getEGLContext() {
        return mEGLContext;
    }

    @Override
    public void createEGLSuface(SurfaceTexture surfaceTexture) {
        int[] surfaceAttribs = {EGL14.EGL_NONE};
        mEglSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig[0], surfaceTexture, surfaceAttribs, 0);
        if (mEglSurface == EGL14.EGL_NO_SURFACE && EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
            Log.d(TAG, "createEGLSuface failed..");
        }
    }

    @Override
    public void createPbBufferSuface(int width, int height) {
        int[] surfaceAttribs = {EGL14.EGL_WIDTH, width, EGL14.EGL_HEIGHT, height, EGL14.EGL_NONE};
        mEglSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig[0],  surfaceAttribs, 0);
        if (mEglSurface == EGL14.EGL_NO_SURFACE && EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
            Log.d(TAG, "createEGLSuface failed..");
        }
    }

    @Override
    public boolean makeCurrent() {
        return EGL14.eglMakeCurrent(mEGLDisplay, mEglSurface, mEglSurface, mEGL14Context);
    }

    @Override
    public void swapBuffers() {
        boolean swapBuffer = EGL14.eglSwapBuffers(mEGLDisplay, mEglSurface);
    }

    @Override
    public boolean releaseCurrent() {
        return EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
    }

    @Override
    public void uninit() {
        if (mEglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(mEGLDisplay, mEglSurface);
        }
        if (mEGL14Context != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(mEGLDisplay, mEGL14Context);
        }

        mEglSurface = EGL14.EGL_NO_SURFACE;
        mEGL14Context = EGL14.EGL_NO_CONTEXT;
        mEGLConfig = null;
        mEGLContext = null;
    }

}
