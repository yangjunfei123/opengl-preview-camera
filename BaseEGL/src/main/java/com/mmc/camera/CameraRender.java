package com.mmc.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import com.mmc.elg.BaseEGLUtils;
import com.mmc.elg.EGLHelper;
import com.mmc.elg.OesTo2DTexture;
import com.mmc.elg.TextureViewRender;

import java.nio.ByteBuffer;

/**
 * Created by yangjunfei on 16/10/18
 */
public class CameraRender implements EGLHelper.EGLRender {

    public static final String TAG = "CameraRender";


    private EGLHelper mEGLHelper;
    private TextureViewRender mTextureViewRender;
    private TextureView mTextureView;

    private OesTo2DTexture mOesToTexture2D;
    private int mCameraTextureId = 0;
    private SurfaceTexture mCameraPreviewTexture = null;

    private ICameraInterface mCameraInterface;
    private Context mContext;

    private ByteBuffer mByteBuffer;

    private OnCameraDataAvailableListener mCameraDataListener;


    public CameraRender(Context context, ICameraInterface cameraInterface , TextureView textureView) {
        mCameraInterface = cameraInterface;
        mTextureView = textureView;
        mContext = context;
    }

    @Override
    public void onEGLInit(EGLHelper eglHelper) {
        mEGLHelper = eglHelper;
        mCameraTextureId = BaseEGLUtils.createOESTexture();
        mCameraPreviewTexture = new SurfaceTexture(mCameraTextureId);
        mCameraPreviewTexture.setOnFrameAvailableListener(mOnCameraFrameAvailable);

        mTextureViewRender = new TextureViewRender(mContext, mEGLHelper, mTextureView);
        mOesToTexture2D = new OesTo2DTexture();
        mOesToTexture2D.init(mContext, mCameraInterface.getCameraWith(), mCameraInterface.getCameraHeight());

        int length = mCameraInterface.getCameraWith() * mCameraInterface.getCameraHeight() * 4;
        mByteBuffer = ByteBuffer.allocateDirect(length);

        mCameraInterface.setPreviewSurfaceTexture(mCameraPreviewTexture);
        mCameraInterface.startPreview();
    }

    private SurfaceTexture.OnFrameAvailableListener mOnCameraFrameAvailable = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mEGLHelper.requestRender();
        }
    };


    @Override
    public void render() {


        mCameraPreviewTexture.updateTexImage();
        float[] matrix = new float[16];
        mCameraPreviewTexture.getTransformMatrix(matrix);
        mOesToTexture2D.setMatrix(matrix);
        int texture = mOesToTexture2D.drawToTexture(mCameraTextureId, 0, false, false, mByteBuffer);

        if (mCameraDataListener != null) {
            mCameraDataListener.onCameraDataAvailable(mByteBuffer,  mCameraInterface.getCameraHeight(), mCameraInterface.getCameraWith());
        }

        mTextureViewRender.render(texture, mCameraInterface.getCameraHeight(), mCameraInterface.getCameraWith());
    }

    public void setCameraDataAvailableListener(OnCameraDataAvailableListener listener) {
        mCameraDataListener = listener;
    }

    @Override
    public void onEGLUninit() {

        if (mCameraInterface != null) {
            mCameraInterface.stopPreview();
            mCameraInterface = null;
        }

        if (mCameraPreviewTexture != null) {
            mCameraPreviewTexture.release();
        }

        if (mTextureViewRender != null) {
            mTextureViewRender.uninit();
            mTextureViewRender = null;
        }

        if (mOesToTexture2D != null) {
            mOesToTexture2D.uninit();
            mOesToTexture2D = null;
        }

        BaseEGLUtils.destroyTexture(mCameraTextureId);
        mCameraTextureId = 0;
    }

    interface OnCameraDataAvailableListener {
        void onCameraDataAvailable(ByteBuffer byteBuffer, int width, int height);
    }

}
