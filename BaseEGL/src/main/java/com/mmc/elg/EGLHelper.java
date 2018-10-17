package com.mmc.elg;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;


/**
 * Created by yangjunfei on 18/8/18
 */
public class EGLHelper {

    private static final String TAG = "EGLHelper";

    private static final int MSG_ID_INIT = 1;
    private static final int MSG_ID_RENDER = 2;
    private static final int MSG_ID_UNINIT = 3;

    private EGLRender mRenderCallback;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private BaseEGL mBaseEGL;

    private int mWindowWidth = 10;
    private int mWindowHeight = 10;

    public EGLHelper() {
    }

    public void init() {
        mHandlerThread = new HandlerThread("EGLHelper");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_ID_INIT:
                        doInit();
                        break;
                    case MSG_ID_RENDER:
                        render();
                        break;
                    case MSG_ID_UNINIT:
                        doUninit();
                        break;
                }
            }
        };

        mHandler.sendEmptyMessage(MSG_ID_INIT);

    }

    public BaseEGLContext getSharedEGLContext() {
        return mBaseEGL.getEGLContext();
    }

    public void setPBBufferOutputWindowSize(int w, int h) {
        mWindowWidth = w;
        mWindowHeight = h;
    }

    private int doInit() {
        mBaseEGL = BaseEGLFactory.createBaseEGL();
        mBaseEGL.init(null, BaseEGL.SURFACE_PB_BUFFER);
        mBaseEGL.createPbBufferSuface(mWindowWidth, mWindowHeight);
        mBaseEGL.makeCurrent();
        if (mRenderCallback != null) {
            mRenderCallback.onEGLInit(this);
        }

        Log.d(TAG, "init EGL fully.");
        return 0;
    }

    public void runOnEGL(Runnable runnable) {
        if (mHandler != null) {
            mHandler.post(runnable);
        }
    }

    public void requestRender() {
        mHandler.sendEmptyMessage(MSG_ID_RENDER);
    }

    public void setRender(EGLRender render) {
        mRenderCallback = render;
    }

    private Object lockObject = new Object();

    private void render() {
        if (mRenderCallback != null) {
            synchronized (lockObject) {
                mBaseEGL.makeCurrent();
                mRenderCallback.render();
                mBaseEGL.swapBuffers();
            }
        }
    }

    public void uninit() {
        Log.d(TAG, "uninit-->" + (mHandler != null) + ",  " + mHandlerThread.isAlive());
        if (mHandler != null && mHandlerThread.isAlive()) {
            mHandler.sendEmptyMessage(MSG_ID_UNINIT);
        }
    }

    private void doUninit() {
        mBaseEGL.makeCurrent();
        if (mRenderCallback != null) {
            mRenderCallback.onEGLUninit();
        }
        mBaseEGL.uninit();
        mHandler.removeMessages(MSG_ID_RENDER);

        Log.d(TAG, "doUninit finished.");
        mHandlerThread.quit();
        mHandlerThread = null;
    }

    public interface EGLRender {
        void onEGLInit(EGLHelper eglHelper);

        void render();

        void onEGLUninit();
    }

}
