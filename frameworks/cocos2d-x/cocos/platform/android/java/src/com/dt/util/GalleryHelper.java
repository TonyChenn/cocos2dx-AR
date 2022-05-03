package com.dt.util;

import android.app.Activity;
import android.content.Intent;

public class GalleryHelper {
    // 单纯打开相册
    public static void openGallery(Activity activity){
        Intent intent =new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivity(intent);
    }
    // 保存到相册
    public static void saveToGallery(){

    }
    // 打开相册选择一张图片
    public static String openGalleryAndSelectSinglePic(){

        return "";
    }
}
