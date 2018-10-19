package com.neil.openglexample;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;

import com.mmc.camera.CameraFactory;
import com.mmc.camera.CameraRender;
import com.mmc.camera.ICameraInterface;
import com.mmc.elg.EGLHelper;
import com.mmc.elg.EGLTextureView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private EGLHelper eglHelper = null;
    ICameraInterface cameraInterface;
    public void init() {
        eglHelper = new EGLHelper();
        TextureView textureView = findViewById(R.id.texture_view);
        cameraInterface = CameraFactory.getCameraInterface(this);
        cameraInterface.setPreviewSize(1280, 720);
        cameraInterface.open(ICameraInterface.CAMERA_FRONT);

        CameraRender cameraRender = new CameraRender(this, cameraInterface, textureView);
        eglHelper.setRender(cameraRender);
        eglHelper.init();


        TextureView textureView1 = findViewById(R.id.texture_view_2);

        ICameraInterface cameraInterface1 = CameraFactory.getCameraInterface(this);
        cameraInterface1.setPreviewSize(1280, 720);
        cameraInterface1.open(ICameraInterface.CAMERA_BACK);

        EGLHelper eglHelper1 = new EGLHelper();
        CameraRender cameraRender1 = new CameraRender(this, cameraInterface1, textureView1);
        eglHelper1.setRender(cameraRender1);
        eglHelper1.init();

//        EGLTextureView eglTextureView = new EGLTextureView(textureView1, new CubeRender());
//        eglTextureView.init();

    }


    @Override
    protected void onDestroy() {
        cameraInterface.close();
        eglHelper.uninit();
        super.onDestroy();
    }
}
