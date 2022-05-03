package org.cocos2dx.lib;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Cocos2dxCameraView extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = Cocos2dxCameraView.class.getSimpleName();

    private CameraProxy cameraProxy;
    private SurfaceHolder surfaceHolder;

    public Cocos2dxCameraView(Cocos2dxActivity activity, CameraProxy cameraProxy) {
        super(activity);
        this.cameraProxy = cameraProxy;
        this.surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(cameraProxy != null){
            cameraProxy.OpenCamera();
            cameraProxy.StartPreview(surfaceHolder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if(surfaceHolder.getSurface() == null)
            return;

        if(cameraProxy!=null){
            cameraProxy.StopPreview();
            cameraProxy.StartPreview(surfaceHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(cameraProxy!=null)
            cameraProxy.DestoryCamera();
    }
}
