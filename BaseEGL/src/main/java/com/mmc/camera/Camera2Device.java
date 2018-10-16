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

import java.util.Arrays;

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
}
