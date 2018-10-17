package com.mmc.camera;

import android.graphics.SurfaceTexture;
import android.view.TextureView;


/**
 * Created by yangjunfei on 15/10/18
 */
public interface ICameraInterface {

    int CAMERA_FRONT = 1;
    int CAMERA_BACK = 0;

    void open(int cameraId);

    void setFrameRate(int frameRate);

    void setPreviewSurfaceTexture(SurfaceTexture surfaceTexture);

    void setPreviewSize(int w, int h);

    void startPreview();

    void stopPreview();

    int getCameraWith();
    int getCameraHeight();

    boolean isCamera2Api();

    void switchCamera();

    void close();

}
