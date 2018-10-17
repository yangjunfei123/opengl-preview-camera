package com.neil.openglexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;

import com.mmc.camera.CameraFactory;
import com.mmc.camera.CameraRender;
import com.mmc.camera.ICameraInterface;
import com.mmc.elg.EGLHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void init() {
        EGLHelper eglHelper = new EGLHelper();
        TextureView textureView = findViewById(R.id.texture_view);
        ICameraInterface cameraInterface = CameraFactory.getCameraInterface(this);
        cameraInterface.setPreviewSize(1280, 720);
        cameraInterface.open(ICameraInterface.CAMERA_FRONT);

        CameraRender cameraRender = new CameraRender(this, cameraInterface, textureView);
        eglHelper.setRender(cameraRender);
        eglHelper.init();
    }



}
