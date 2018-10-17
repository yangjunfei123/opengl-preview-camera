package com.mmc.camera;

import android.content.Context;
import android.os.Build;

/**
 * Created by yangjunfei on 15/10/18
 */
public class CameraFactory {

    public static ICameraInterface getCameraInterface(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return new Camera2Device(context);
        } else {
            return new CameraDevice(context);
        }
    }

}
