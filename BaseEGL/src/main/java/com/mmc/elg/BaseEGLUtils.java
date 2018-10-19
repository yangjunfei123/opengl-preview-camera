package com.mmc.elg;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yangjunfei on 9/10/18
 */
public class BaseEGLUtils {

    public static int loadShader(String shaderProgram, int shaderType) {
        int shader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader, shaderProgram);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static int createProgram(Context context, String vertexFileName, String fragmentFileName) {
        int vertexShader = loadShader(loadAssetRes(context, vertexFileName), GLES20.GL_VERTEX_SHADER);
        int fragmentShader = loadShader(loadAssetRes(context, fragmentFileName), GLES20.GL_FRAGMENT_SHADER);

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
        return program;
    }

    public static String loadAssetRes(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            int length = is.available();
            byte[] data = new byte[length];
            is.read(data);
            is.close();
            return new String(data, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static int createOESTexture() {
//        int[] textureIds = new int[1];
//        GLES20.glGenTextures(1, textureIds, 0);
//
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureIds[0]);
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        return createTexutre(TEXTURE_TYPE_OES);
    }


    public static int create2DTexture(int width, int height) {
//        int[] texture2D = new int[1];
//        GLES20.glGenTextures(1, texture2D, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2D[0]);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        int texture2D = createTexutre(TEXTURE_TYPE_2D);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, null);
        return texture2D;
    }

    public static final int TEXTURE_TYPE_OES = 1;
    public static final int TEXTURE_TYPE_2D = 2;
    public static final int TEXTURE_TYPE_CUBE = 3;

    public static int createTexutre(int type) {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);

        int textureType = GLES20.GL_TEXTURE_2D;

        switch (type) {
            case TEXTURE_TYPE_OES:
                textureType = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                break;
            case TEXTURE_TYPE_2D:
                textureType = GLES20.GL_TEXTURE_2D;
                break;
            case TEXTURE_TYPE_CUBE:
                textureType = GLES20.GL_TEXTURE_CUBE_MAP;
                break;
        }

        GLES20.glBindTexture(textureType, textureIds[0]);
        GLES20.glTexParameteri(textureType, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(textureType, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(textureType, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(textureType, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        return textureIds[0];
    }

    public static void destroyTexture(int texture) {
        GLES20.glDeleteTextures(1, new int[]{texture}, 0);
    }

    public static void destroyTexture(int[] textures) {
        GLES20.glDeleteTextures(textures.length, textures, 0);
    }

    public static final int SCALE_TYPE_CENTER_CORP = 1;
    public static final int SCALE_TYPE_CENTER_INSIDE = 2;

    public static void adapterDisplayScreen(int sw, int sh, int imageW, int imageH, float[]  vertexs, int type) {
        if (type == SCALE_TYPE_CENTER_INSIDE) {
            adapterDisplayScreenCenterInside(sw, sh, imageW, imageH, vertexs);
        } else if (type == SCALE_TYPE_CENTER_CORP) {
            adapterDisplayScreenCenterCorp(sw, sh, imageW, imageH, vertexs);
        }
    }


    public static void adapterDisplayScreenCenterInside(int sw, int sh, int imageW, int imageH, float[]  vertexs) {
        float sWHScale = sw * 1.0f / sh;
        float imageWHScale = imageW * 1.0f / imageH;

        float realHScale = 1;


        if (sWHScale > imageWHScale) {
            float realW = sh * imageWHScale;
            realHScale = realW / sw;
        } else {
            float realH = sw / imageWHScale;
            realHScale = realH / sh;
        }

        for (int i = 0; i < vertexs.length; i++) {
            if (sWHScale > imageWHScale) {
                if (i % 2 == 0) {
                    vertexs[i] = vertexs[i] * realHScale;
                }
            } else {
                if (i % 2 != 0) {
                    vertexs[i] = vertexs[i] * realHScale;
                }
            }
        }
    }

    public static void adapterDisplayScreenCenterCorp(int sw, int sh, int imageW, int imageH, float[]  vertexs) {
        float sWHScale = sw * 1.0f / sh;
        float imageWHScale = imageW * 1.0f / imageH;

        float realHScale = 1;


        if (sWHScale > imageWHScale) {
            float realScale = sw * 1.0f / imageW;
            realHScale = imageH * realScale / sh;
        } else {
            float realScale = sh * 1.0f / imageH;
            realHScale = imageW * realScale / sw;
        }

        for (int i = 0; i < vertexs.length; i++) {
            if (sWHScale > imageWHScale) {

                if (i % 2 != 0) {
                    vertexs[i] = vertexs[i] * realHScale;
                }
            } else {
                if (i % 2 == 0) {
                    vertexs[i] = vertexs[i] * realHScale;
                }
            }
        }
    }


}
