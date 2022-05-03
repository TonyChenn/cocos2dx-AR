package org.cocos2dx.lib;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraProxy {
    private static final String TAG = CameraProxy.class.getSimpleName();

    private final Activity activity;
    private Camera mCamera;
    private Camera.Parameters parameters;

    // 相机ID(默认后置相机)
    private int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

    // 旋转监听
    private final OrientationEventListener orientationEventListener;


    public CameraProxy(Activity activity){
        this.activity = activity;

        orientationEventListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.i(TAG, "onOrientationChanged: " + orientation);
                setDispalyRotation();
            }
        };
    }

    // 0 后摄  1 前摄
    public boolean HasCamera(){
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    // 打开相机
    public void OpenCamera(){
        try{
            if(mCamera != null){
                DestoryCamera();
            }

            mCamera = Camera.open(cameraID);
            Camera.getCameraInfo(cameraID, cameraInfo);
            initCamera();
            orientationEventListener.enable();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // 获取相机
    public Camera GetCamera(){
        return mCamera;
    }

    // 切换前后相机
    public void SwitchCamera(){
        cameraID = cameraID == 0 ? 1 : 0;
        DestoryCamera();
        OpenCamera();
    }

    // 销毁相机
    public void DestoryCamera(){
        if(mCamera != null){
            try{
                orientationEventListener.disable();
                Log.i(TAG, "销毁相机");
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    // 开始预览
    public void StartPreview(SurfaceHolder holder){
        if(mCamera != null){
            try{
                Log.d(TAG, "开始预览");
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }catch (IOException ex){
                ex.printStackTrace();
            }
            Log.d(TAG, parameters.getPictureSize().width+" <-:-> "+parameters.getPictureSize().height);
        }
    }

    // 预览手动放大缩小
    public void HandleZoom(boolean zoomIn){
        if(parameters.isZoomSupported()){
            int maxZoom = parameters.getMaxZoom();
            int curZoom = parameters.getZoom();

            // 放大
            if(zoomIn){
                if(curZoom < maxZoom) ++ curZoom;
            }else {
                // 缩小
                if(curZoom > 0) --curZoom;
            }
            parameters.setZoom(curZoom);
            mCamera.setParameters(parameters);
        }
    }

    // 结束预览
    public void StopPreview(){
        if(mCamera != null){
            Log.d(TAG, "StopPreview");
            mCamera.stopPreview();
        }
    }

    // 是否是前置相机
    public boolean IsFrontCamera(){
        return cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    // 拍照
    public void TakePicture(Camera.PreviewCallback callback){
        if(mCamera != null){
            mCamera.setOneShotPreviewCallback(callback);
        }
    }




    // 初始化相机
    private void initCamera(){
        try {
            parameters = mCamera.getParameters();

            // 设置自动聚焦
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            if(parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            //
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setPreviewFormat(ImageFormat.NV21);
            parameters.setExposureCompensation(0); // 设置曝光强度

            // 拿到屏幕尺寸
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            // 要保证宽>高
            int screenWidth = Math.max(metrics.widthPixels, metrics.heightPixels);
            int screenHeight = Math.min(metrics.widthPixels, metrics.heightPixels);


            // 设置预览大小
            Point previousSize = findBestPreviewSizeValue(parameters.getSupportedPreviewSizes(), screenWidth, screenHeight);
            assert previousSize != null;
            parameters.setPreviewSize(previousSize.x, previousSize.y);


            mCamera.setParameters(parameters);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    // 设置屏幕旋转
    private void setDispalyRotation() {
        int roation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (roation) {
            case Surface.ROTATION_0: degree = 0; break;
            case Surface.ROTATION_90: degree = 90; break;
            case Surface.ROTATION_180: degree = 180; break;
            case Surface.ROTATION_270: degree = 270; break;
        }
        int result;
        if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (cameraInfo.orientation + degree) % 360;
            result = (360 - result) % 360;
        }else {
            result = (cameraInfo.orientation - degree + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    // 找到一个最合适的预览大小
    private static Point findBestPreviewSizeValue(List<Camera.Size> sizes, int screenWidth, int screenHeight) {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (Camera.Size size : sizes) {
            int dimPosition = size.width;
            if (dimPosition < 0) {
                continue;
            }

            int newDiff = Math.abs(size.width - screenWidth) + Math.abs(size.height - screenHeight);
            if (newDiff == 0) {
                bestX = size.width;
                bestY = size.height;
                break;
            } else if (newDiff < diff) {
                bestX = size.width;
                bestY = size.height;
                diff = newDiff;
            }
        }
        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }
}