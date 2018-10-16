package com.mmc.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import java.io.IOException;


/**
 * Created by yangjunfei on 15/10/18
 */
public class CameraDevice implements ICameraInterface{

    private static final String TAG = "CameraDevice";

    private Camera mCamera = null;
    private int mCameraWidth = 640;
    private int mCameraHeight = 480;
    private int mFrameRate = 25;

    public CameraDevice(Context context) {
    }


    @Override
    public void open(int cameraId) {
        mCamera = Camera.open(cameraId);
    }

    @Override
    public void setPreviewSize(int w, int h) {
        mCameraWidth = w;
        mCameraHeight = h;
    }

    @Override
    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    @Override
    public void switchCamera() {

    }

    @Override
    public int getCameraWith() {
        return mCameraWidth;
    }

    @Override
    public int getCameraHeight() {
        return mCameraHeight;
    }

    @Override
    public void setPreviewSurfaceTexture(SurfaceTexture surfaceTexture) {
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startPreview() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mCameraWidth, mCameraHeight);
        parameters.setPreviewFpsRange(mFrameRate * 1000, mFrameRate * 1000);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    @Override
    public void stopPreview() {
        mCamera.stopPreview();
    }

    @Override
    public void close() {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}
