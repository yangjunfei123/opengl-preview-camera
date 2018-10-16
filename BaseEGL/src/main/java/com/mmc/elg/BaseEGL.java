package com.mmc.elg;

import android.graphics.SurfaceTexture;

import javax.microedition.khronos.egl.EGL10;

/**
 * Created by yangjunfei on 6/9/18
 */
public abstract class BaseEGL {

    private static final String TAG = "BaseEGL";

    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;
    //构造需要的配置列表
    public static  final int[] SURFACE_PB_BUFFER = {
            //颜色缓冲区所有颜色分量的位数
            EGL10.EGL_BUFFER_SIZE, 32,
            //颜色缓冲区R、G、B、A分量的位数
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE,8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL10.EGL_NONE
    };

    //构造需要的配置列表
    public static  final int[] SURFACE_WINDOW = {
            //颜色缓冲区所有颜色分量的位数
            EGL10.EGL_BUFFER_SIZE, 32,
            //颜色缓冲区R、G、B、A分量的位数
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE,8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
            EGL10.EGL_NONE
    };

    public BaseEGL() {
    }

    public abstract int init(BaseEGLContext sharedContext, int[] attributes);
    public abstract BaseEGLContext getEGLContext();
    public abstract void createEGLSuface(SurfaceTexture surfaceTexture);
    public abstract void createPbBufferSuface(int width, int height);
    public abstract boolean makeCurrent();
    public abstract boolean releaseCurrent();
    public abstract void swapBuffers();
    public abstract void uninit();


}
