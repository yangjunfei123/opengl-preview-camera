package com.mmc.elg;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by yangjunfei on 16/10/18
 */
public class OesTo2DTexture {
    private static final float[] VERTEX = {   // in counterclockwise order:
            1, 1,
            -1, 1,
            -1, -1,
            1, -1
    };

    private static final float[] VERTEX_90 = {   // in counterclockwise order:
            1, -1,
            1, 1,
            -1, 1,
            -1, -1
    };
    private static final float[] VERTEX_180 = {   // in counterclockwise order:
            -1, -1,
            1, -1,
            1, 1,
            -1, 1,
    };
    private static final float[] VERTEX_270 = {   // in counterclockwise order:
            -1, 1,
            -1, -1,
            1, -1,
            1, 1
    };

    private static final short[] VERTEX_ORDER  = {
            0, 1, 2, 0, 2, 3,
    };

    private static final float[] TEX_VERTEX = {
            1f, 0,
            0, 0,
            0, 1f,
            1f, 1f
    };

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexVertexBuffer;
    private ShortBuffer mVertexOrderBuffer;

    private int[] mFrameBufferIds = null;
    private int[] mTextureIds = null;
    private float[] mMatrix = null;

    private Context mContext;
    private int mWidth = 0;
    private int mHeight = 0;


    private int mProgram = 0;
    int mPositionHandler = 0;
    int mMaxtrixHandler = 0;
    int mTexCoordHandler = 0;
    int mInputImageTexture = 0;

    public OesTo2DTexture() {
    }

    public void init(Context context, int width, int height) {
        mWidth = width;
        mHeight = height;

        mContext = context;

        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(VERTEX);
        mVertexBuffer.position(0);

        mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_VERTEX);
        mTexVertexBuffer.position(0);

        mVertexOrderBuffer = ByteBuffer.allocateDirect(VERTEX_ORDER.length * 4).order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_ORDER);
        mVertexOrderBuffer.position(0);



        mProgram = BaseEGLUtils.createProgram(mContext, "egl/camera_vertex_shader", "egl/camera_fragment_shader");
        mPositionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition");

        mMaxtrixHandler = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mTexCoordHandler = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");

        mInputImageTexture = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");

        mFrameBufferIds = new int[2];
        mTextureIds = new int[2];

        /**
         * 绑定竖屏的framebuffer
         * */
        GLES20.glGenFramebuffers(mFrameBufferIds.length, mFrameBufferIds, 0);
        mTextureIds[0] = BaseEGLUtils.create2DTexture(width, height);
        bindFrameBuffer(mFrameBufferIds[0], mTextureIds[0]);

        /**
         * 绑定横屏的framebuffer
         * */
        mTextureIds[1] = BaseEGLUtils.create2DTexture(mHeight, mWidth);
        bindFrameBuffer(mFrameBufferIds[1], mTextureIds[1]);
    }

    private void bindFrameBuffer(int frameBufferId, int textureId) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void setMatrix(float[] matrix) {
        mMatrix = matrix;
    }


    private float[] getRotatedTextureCoord(int angle, boolean flipx, boolean flipY) {
        angle = angle % 360;

        float[] temp = null;
        float[] target = new float[VERTEX.length];
        if (angle == 90) {
            temp = VERTEX_90;
        } else if (angle == 180) {
            temp = VERTEX_180;
        } else if (angle == 270) {
            temp = VERTEX_270;
        } else {
            temp = VERTEX;
        }
        System.arraycopy(temp, 0, target, 0, target.length);

        if (flipx) {
            for (int i = 0; i < target.length; i++) {
                if (i % 2 == 0) {
                    target[i] = 0 - target[i];
                }
            }
        }

        if (flipY) {
            for (int i = 0; i < target.length; i++) {
                if (i % 2 != 0) {
                    target[i] = 0 - target[i];
                }
            }
        }
        return target;
    }

    public int drawToTexture(int textureId, int rotation, boolean flipx, boolean flipY, ByteBuffer byteBuffer) {

        float[] vertex = getRotatedTextureCoord(rotation, flipx, flipY);
        mVertexBuffer.put(vertex);

        int width = mWidth;
        int height = mHeight;
        int frameBufferId = mFrameBufferIds[0];
        int targetId = mTextureIds[0];
        if ((rotation % 90 == 90) || (rotation % 90 == 270)) {
            width = mHeight;
            height = mWidth;
            frameBufferId = mFrameBufferIds[1];
            targetId = mTextureIds[1];
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glViewport(0, 0, width, height);

        GLES20.glUseProgram(mProgram);
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandler, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        mTexVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mTexCoordHandler, 2, GLES20.GL_FLOAT, false, 0, mTexVertexBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandler);

        if (mMatrix != null) {
            GLES20.glUniformMatrix4fv(mMaxtrixHandler, 1, false, mMatrix, 0);
        }
        int useTexutreId = GLES20.GL_TEXTURE1;

        GLES20.glActiveTexture(useTexutreId);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(mInputImageTexture, 0);

        mVertexOrderBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_ORDER.length, GLES20.GL_UNSIGNED_SHORT, mVertexOrderBuffer);

        if (byteBuffer != null) {
            byteBuffer.position(0);
            GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
        }

        GLES20.glDisableVertexAttribArray(mPositionHandler);
        GLES20.glDisableVertexAttribArray(mTexCoordHandler);

        GLES20.glActiveTexture(useTexutreId);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return targetId;
    }

    public void uninit() {
        GLES20.glDeleteTextures(mTextureIds.length, mTextureIds, 0);
        GLES20.glGenFramebuffers(mFrameBufferIds.length, mFrameBufferIds, 0);
        GLES20.glDeleteProgram(mProgram);
    }
}
