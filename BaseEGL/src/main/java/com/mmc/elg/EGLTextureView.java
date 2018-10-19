package com.mmc.elg;

import android.graphics.SurfaceTexture;
import android.view.TextureView;

/**
 * Created by yangjunfei on 19/10/18
 */
public class EGLTextureView {

    private TextureView mTextureView;
    private EGLHelper.EGLRender mRender;
    private EGLHelper mEGLHelper;

    public EGLTextureView(TextureView textureView, EGLHelper.EGLRender render) {
        mTextureView = textureView;
        mRender = render;
    }

    public void init() {
        if (mTextureView.isAvailable()) {
            initEGL();
        }

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                initEGL();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void initEGL() {
        mEGLHelper = new EGLHelper(mTextureView.getSurfaceTexture());
        mEGLHelper.setRender(mRender);
        mEGLHelper.init();
    }

    public void uninit() {
        mEGLHelper.uninit();
    }
}
