package com.mmc.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


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
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        computePreviewSize(sizeList);
        parameters.setPreviewSize(mCameraWidth, mCameraHeight);
        parameters.setPreviewFpsRange(mFrameRate * 1000, mFrameRate * 1000);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    private static class Resolution {
        Camera.Size size;
        float scale;
        float area;

        public String toString() {
            return size.width + "x" + size.height + ", scale : " + scale + ", area : " + area;
        }
    }

    private void computePreviewSize(List<Camera.Size> cameraSizes) {

        Camera.Size[] sizes = new Camera.Size[cameraSizes.size()];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = cameraSizes.get(i);
        }

        float targetScale = (float) (mCameraWidth * 1.0 / mCameraHeight);
        float targetArea = mCameraWidth * mCameraHeight;

        List<Resolution>  resolutionList = new ArrayList<>(20);
        for (int i = 0; i < sizes.length; i++) {
            Resolution r = new Resolution();
            r.scale = (float) (sizes[i].width * 1.0 / sizes[i].height);
            r.area = sizes[i].width * sizes[i].height;
            r.size = sizes[i];
            resolutionList.add(r);
        }

        Collections.sort(resolutionList, new Comparator<Resolution>() {
            @Override
            public int compare(Resolution o1, Resolution o2) {

                if (o1.area < o2.area) {
                    return -1;
                }

                if (o1.area == o2.area) {
                    return 0;
                }

                return 1;
            }
        });

        Resolution targetResolution = null;
        for (int i = 0; i < resolutionList.size(); i++) {
            Resolution r = resolutionList.get(i);
            if (i == 0 && targetArea <= r.area) {
                targetResolution = r;
                break;
            }

            if (i == resolutionList.size() - 1 && targetArea >= r.area) {
                targetResolution = r;
                break;
            }
            Resolution next = null;
            if (i+1 < resolutionList.size()) {
                next = resolutionList.get(i + 1);
            }
            if (r.area < targetArea)
                if (next == null) {
                    targetResolution = r;
                    break;
                }

            if (targetArea < next.area) {
                targetResolution = (targetArea - r.area) < (next.area - targetArea) ? r : next;
                break;
            }

        }

        mCameraWidth = targetResolution.size.width;
        mCameraHeight = targetResolution.size.height;
        Log.d(TAG, targetResolution.toString());
    }

    @Override
    public void stopPreview() {
        mCamera.stopPreview();
    }

    @Override
    public boolean isCamera2Api() {
        return false;
    }

    @Override
    public void close() {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}
