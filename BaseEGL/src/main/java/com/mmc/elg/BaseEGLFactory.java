package com.mmc.elg;


import android.util.Log;

/**
 * Created by yangjunfei on 7/9/18
 */
public class BaseEGLFactory {

    private static final String TAG = "BaseEGLFactory";

    private static final int EGLExt_SDK_VERSION = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
    private static final int CURRENT_SDK_VERSION = android.os.Build.VERSION.SDK_INT;

    public static boolean isEGL14Supported() {
        Log.d(TAG, "SDK version: " + CURRENT_SDK_VERSION + ". isEGL14Supported: "
                + (CURRENT_SDK_VERSION >= EGLExt_SDK_VERSION));
        return (CURRENT_SDK_VERSION >= EGLExt_SDK_VERSION);
    }

    public static BaseEGL createBaseEGL() {
        if (isEGL14Supported()) {
            return new BaseEGL14();
        }
        return new BaseEGL10();
    }
}
