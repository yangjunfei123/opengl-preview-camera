package com.mmc.elg;

import android.opengl.EGLContext;

/**
 * Created by yangjunfei on 7/9/18
 */
public class BaseEGLContext {

    private EGLContext mEGL14Context;

    private javax.microedition.khronos.egl.EGLContext mEGL10Context;


    public BaseEGLContext(EGLContext context) {
        mEGL14Context = context;
    }

    public BaseEGLContext(javax.microedition.khronos.egl.EGLContext context) {
        mEGL10Context = context;
    }

    public EGLContext getEGL14Context() {
        return mEGL14Context;
    }

    public void setEGL14Context(EGLContext eGL14Context) {
        this.mEGL14Context = eGL14Context;
    }

    public javax.microedition.khronos.egl.EGLContext getEGL10Context() {
        return mEGL10Context;
    }

    public void setEGL10Context(javax.microedition.khronos.egl.EGLContext eGL10Context) {
        this.mEGL10Context = eGL10Context;
    }
}
