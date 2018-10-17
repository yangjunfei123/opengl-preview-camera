package com.mmc.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by yangjunfei on 15/10/18
 */

@TargetApi(21)
public class Camera2Device implements ICameraInterface{

    private static final String TAG = "Camera2Device";

    private Context mContext;
    private SurfaceTexture mSufaceTexture;

    private Surface mMainSuface;
    private int mWidth = 640;
    private int mHeight = 480;
    private int mFrameRate = 25;

    private CameraDevice mCameraDevice;


    public Camera2Device(Context context) {
        mContext = context;
    }


    @Override
    public void open(int cameraId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (mContext.checkCallingOrSelfPermission("android.permission.CAMERA") != PERMISSION_GRANTED) {
            return;
        }

        try {

            int backOrFrontCamera = (cameraId == CAMERA_FRONT) ? CameraCharacteristics.LENS_FACING_FRONT :
                    CameraCharacteristics.LENS_FACING_BACK;

            CameraManager manager = (CameraManager)mContext.getSystemService(Context.CAMERA_SERVICE);
            String[] cameraList = manager.getCameraIdList();
            for (String camera : cameraList) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(camera);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == backOrFrontCamera) {

                    StreamConfigurationMap configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] sizes = configurationMap.getOutputSizes(SurfaceTexture.class);
                    for (Size size : sizes) {
                        Log.d(TAG, size.getWidth() + "x" + size.getHeight());
                    }

                    computePreviewSize(sizes);
                    manager.openCamera(camera, new CameraStateCallback(CameraStateCallback.CAMERA_CALLBACK_OPEN), null);
                    break;
                }
            }



        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPreviewSurfaceTexture(SurfaceTexture surfaceTexture) {
        mSufaceTexture = surfaceTexture;
    }

    @Override
    public void setPreviewSize(int w, int h) {
        mWidth = w;
        mHeight = h;
    }

    @Override
    public void startPreview() {
        try {

            while (mCameraDevice == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            final CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mSufaceTexture.setDefaultBufferSize(mWidth, mHeight);
            mMainSuface = new Surface(mSufaceTexture);
            builder.addTarget(mMainSuface);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range.create(mFrameRate, mFrameRate));
//            builder.set(CaptureRequest.);



            mCameraDevice.createCaptureSession(Arrays.asList(mMainSuface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        Log.d(TAG, "beginPreview-->onConfigured");
                        session.setRepeatingRequest(builder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static class Resolution {
        Size size;
        float scale;
        float area;

        public String toString() {
            return size.getWidth() + "x" + size.getHeight() + ", scale : " + scale + ", area : " + area;
        }
    }

    private void computePreviewSize(Size[] sizes) {
        float targetScale = (float) (mWidth * 1.0 / mHeight);
        float targetArea = mWidth * mHeight;

        List<Resolution>  resolutionList = new ArrayList<>(20);
        for (int i = 0; i < sizes.length; i++) {
            Resolution  r = new Resolution();
            r.scale = (float) (sizes[i].getWidth() * 1.0 / sizes[i].getHeight());
            r.area = sizes[i].getWidth() * sizes[i].getHeight();
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

        mWidth = targetResolution.size.getWidth();
        mHeight = targetResolution.size.getHeight();
        Log.d(TAG, targetResolution.toString());
    }

    @Override
    public void stopPreview() {
        close();
    }

    @Override
    public void switchCamera() {

    }

    @Override
    public void close() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }


    }

    @Override
    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    @Override
    public int getCameraWith() {
        return mWidth;
    }

    @Override
    public int getCameraHeight() {
        return mHeight;
    }

    @TargetApi(21)
    class CameraStateCallback extends android.hardware.camera2.CameraDevice.StateCallback {
        static final int CAMERA_CALLBACK_OPEN = 1;

        int callbackType = CAMERA_CALLBACK_OPEN;

        public CameraStateCallback(int type) {
            callbackType = type;
        }

        @Override
        public void onOpened(@NonNull android.hardware.camera2.CameraDevice camera) {
            Log.d(TAG, "onOpened");
            mCameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull android.hardware.camera2.CameraDevice camera) {
            Log.d(TAG, "onDisconnected");
        }

        @Override
        public void onError(@NonNull android.hardware.camera2.CameraDevice camera, int error) {
            Log.d(TAG, "onError" + error);
        }
    }

    @Override
    public boolean isCamera2Api() {
        return true;
    }
}
