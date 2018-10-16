package com.mmc.elg;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;

/**
 * Created by yangjunfei on 12/10/18
 */
public class BitmapHandler {

    private int mTextureId;
    private EGLTextureRotation mRotation;
    private int mWidth;
    private int mHeight;

    public void init(Context context, int width, int height) {
        mTextureId = BaseEGLUtils.create2DTexture(width, height);
        mRotation = new EGLTextureRotation();
        mRotation.init(context, width, height);
        mWidth = width;
        mHeight = height;

    }


    public int handle(ByteBuffer inputByteBuffer) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                mWidth,
                mHeight,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                inputByteBuffer);
        return mRotation.rotate(mTextureId, 0, false, true, inputByteBuffer);
    }

    public void uninit() {
        mRotation.uninit();
        mRotation = null;
        BaseEGLUtils.destroyTexture(mTextureId);
    }


}
