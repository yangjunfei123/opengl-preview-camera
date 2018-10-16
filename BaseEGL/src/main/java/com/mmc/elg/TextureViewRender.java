package com.mmc.elg;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;
import android.view.TextureView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by yangjunfei on 16/10/18
 */
public class TextureViewRender {

    private static final String TAG = "TextureViewRender";

    private static  float[] VERTEX = {   // in counterclockwise order:
            1, 1,
            -1, 1,
            -1, -1,
            1, -1,
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

    private int mPositionHandler = 0;
    private int mMaxtrixHandler = 0;
    private int mTexCoordHandler = 0;
    private int mTexSampleHandle = 0;
    private int mProgram = 0;


    private Context mContext;
    private EGLHelper mEGLHelper;
    private TextureView mTextureView;


    private BaseEGL mRenderEGL;

    private int mViewWidth;
    private int mViewHeight;

    public TextureViewRender(Context context, EGLHelper eglHelper, TextureView textureView) {
        mContext = context;
        mEGLHelper = eglHelper;
        mTextureView = textureView;

        if (mTextureView.isAvailable()) {
            initRender();
        }

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mEGLHelper.runOnEGL(new Runnable() {
                    @Override
                    public void run() {
                        initRender();
                    }
                });

                Log.d(TAG,  "onSurfaceTextureAvailable-->" + width + "x" + height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.d(TAG,  "onSurfaceTextureSizeChanged-->" + width + "x" + height);
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

    private void initRender() {

        if (mRenderEGL != null) {
            uninit();
        }

        mRenderEGL = BaseEGLFactory.createBaseEGL();
        mRenderEGL.init(mEGLHelper.getSharedEGLContext(), BaseEGL.SURFACE_WINDOW);
        mRenderEGL.createEGLSuface(mTextureView.getSurfaceTexture());

        mProgram = BaseEGLUtils.createProgram(mContext, "egl/vertex_shader", "egl/fragment_shader");
        mPositionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition");

        mMaxtrixHandler = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mTexCoordHandler = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        mTexSampleHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

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

    }

    public void render(int textureId, int width, int height) {

        if (!mTextureView.isAvailable()) {
            Log.d(TAG, "mTextureView is unavailable...");
            return;
        }

        mViewWidth = mTextureView.getWidth();
        mViewHeight = mTextureView.getHeight();


        if (mRenderEGL == null) {
            return;
        }

        mRenderEGL.makeCurrent();
        GLES20.glViewport(0, 0, mViewWidth, mViewHeight);

//        if (!first) {
//            first = true;
//            BaseEGLUtils.adapterDisplayScreen(mViewWidth, mViewHeight, width, height, VERTEX, BaseEGLUtils.SCALE_TYPE_CENTER_CORP);
//        }
//        mVertexBuffer.put(VERTEX);

        GLES20.glUseProgram(mProgram);
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandler, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandler);

        mTexVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mTexCoordHandler, 2, GLES20.GL_FLOAT, false, 0, mTexVertexBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandler);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        mVertexOrderBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_ORDER.length, GLES20.GL_UNSIGNED_SHORT, mVertexOrderBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandler);
        GLES20.glDisableVertexAttribArray(mTexCoordHandler);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        mRenderEGL.swapBuffers();
    }

    public void uninit() {

        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }

        if (mRenderEGL != null) {
            mRenderEGL.uninit();
            mRenderEGL = null;
        }
    }
}
