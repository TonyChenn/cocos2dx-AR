package org.cocos2dx.lib;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class Cocos2dxCameraHelper {
    private final String TAG = Cocos2dxCameraHelper.class.getSimpleName();

    private static Handler sHandler;
    private static Cocos2dxActivity sCocos2dxActivity;
    private static FrameLayout sParent;
    private static CameraProxy sCameraProxy;
    private static Cocos2dxCameraView sCameraView;
    private static int savePicCallback = 0;

    private final static int CameraTaskCreate     = 0;
    private final static int CameraTaskRemove     = 1;
    private final static int CameraTaskOpen       = 2;
    private final static int CameraTaskRelease    = 3;
    private final static int CameraTaskSnap       = 4;
    private final static int CameraTaskSwitch     = 5;
    private final static int CameraTaskBringFront = 6;

    Cocos2dxCameraHelper(FrameLayout layout){
        sParent = layout;

        sHandler = new CameraHandler(this);
        Cocos2dxCameraHelper.sCocos2dxActivity = (Cocos2dxActivity) Cocos2dxActivity.getContext();
        sCameraProxy = new CameraProxy(sCocos2dxActivity);
    }

// region API
    public static void createCameraView(){
        Message msg = new Message();
        msg.what = CameraTaskCreate;
        sHandler.sendMessage(msg);
    }
    public static void removeCameraView(){
        Message msg = new Message();
        msg.what = CameraTaskRemove;
        sHandler.sendMessage(msg);
    }
    public static void openCamera(){
        Message msg = new Message();
        msg.what = CameraTaskOpen;
        sHandler.sendMessage(msg);
    }
    public static void releaseCamera(){
        Message msg = new Message();
        msg.what = CameraTaskRelease;
        sHandler.sendMessage(msg);
    }

    public static void snapPic(String picPath, int callback){
        Message msg = new Message();
        msg.arg1 = callback;
        msg.obj = picPath;
        msg.what = CameraTaskRemove;
        sHandler.sendMessage(msg);
    }
    public static void switchCamera(){
        Message msg = new Message();
        msg.what = CameraTaskSwitch;
        sHandler.sendMessage(msg);
    }
    private static void setOnTop(boolean top){
        Message msg = new Message();
        msg.what = CameraTaskBringFront;
        msg.arg1 = top ? 1 : 0;
        sHandler.sendMessage(msg);
    }
// endregion API

// region API implement
    // 默认CameraView在Cocos2dxGLView下方
    private void _createCameraView(){
        sCameraView = new Cocos2dxCameraView(sCocos2dxActivity, sCameraProxy);
        FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        sParent.addView(sCameraView, lParams);
        _setOnTop(false);
    }
    private void _removeCameraView() {
        if (sCameraView != null) {
            sParent.removeView(sCameraView);
        }
    }
    private void _openCamera(){
        if(sCameraProxy.HasCamera()){

            _createCameraView();
        }
        else {
            new AlertDialog.Builder(sCocos2dxActivity)
                    .setTitle("提示")
                    .setMessage("设备无相机")
                    .setNegativeButton("取消",null)
                    .setPositiveButton("确定", null)
                    .show();
        }
    }
    private void _releaseCamera(){
        if(sCameraView!=null){
            _removeCameraView();
            sCameraProxy.DestoryCamera();
        }
    }

    private void _snapPic(String picPath, int callback){
        if(sCameraView != null){
            savePicCallback = callback;

            sCameraProxy.TakePicture(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    Bitmap bmp = getBmpPicData(bytes, camera);

                    // 前置相机需旋转270度
                    if(sCameraProxy.IsFrontCamera()){
                        Matrix m = new Matrix();
                        m.postScale(-1, 1);
                        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
                    }
                    saveBitmap(bmp, picPath);
                }
            });
        }
    }
    // Yuv data 转bmp
    private Bitmap getBmpPicData(byte[] bytes, Camera camera){
        Camera.Size size = camera.getParameters().getPreviewSize();
        YuvImage image = new YuvImage(bytes, ImageFormat.NV21,size.width, size.height,null);
        if(image != null){
            Bitmap bmp = null;
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0,0, size.width, size.height),80, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(),0, stream.size());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }
        return null;
    }
    // 保存图片(/Android/data/packname/caches/)目录下
    private void saveBitmap(Bitmap bmp, String savePath) {
        try{
            File img = new File(savePath);
            if(!img.exists()){
                img.getParentFile().mkdir();
                img.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(img);
            // 图片压缩
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, fs);
            fs.flush();
            fs.close();
            Log.i(TAG,"图片保存成功: "+savePath);
            savePicCallback("true");
        }catch (IOException ex){
            ex.printStackTrace();
            savePicCallback("false");
        }
    }
    private void savePicCallback(String success){
        sCocos2dxActivity.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                if(savePicCallback != 0){
                    Cocos2dxLuaJavaBridge.callLuaFunctionWithString(savePicCallback, success);
                    Cocos2dxLuaJavaBridge.releaseLuaFunction(savePicCallback);
                    savePicCallback = 0;
                }
            }
        });
    }

    private void  _switchCamera(){
        if(sCameraView != null){
            sCameraProxy.SwitchCamera();
        }
    }
    private void _setOnTop(boolean top){
        sCameraView.setZOrderOnTop(top);
        sCameraView.setZOrderMediaOverlay(top);
        sCocos2dxActivity.getGLSurfaceView().setZOrderOnTop(!top);
        sCocos2dxActivity.getGLSurfaceView().setZOrderMediaOverlay(!top);
    }
// endregion API implement

    static class CameraHandler extends Handler{
        WeakReference<Cocos2dxCameraHelper> mReference;

        CameraHandler(Cocos2dxCameraHelper helper){
            mReference = new WeakReference<Cocos2dxCameraHelper>(helper);
        }

        @Override
        public void handleMessage(Message msg) {
            Cocos2dxCameraHelper helper = mReference.get();
            switch (msg.what){
                case CameraTaskCreate:
                    helper._createCameraView();
                    break;
                case CameraTaskRemove:
                    helper._removeCameraView();
                    break;
                case CameraTaskOpen:
                    helper._openCamera();
                    break;
                case CameraTaskRelease:
                    helper._releaseCamera();
                    break;
                case CameraTaskSnap:
                    helper._snapPic((String) msg.obj, msg.arg1);
                    break;
                case CameraTaskSwitch:
                    helper._switchCamera();
                    break;
                case CameraTaskBringFront:
                    helper._setOnTop(msg.arg1 == 1);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
