package com.dt.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class CommonHelper {
    // 打开设置
    public static void OpenSetting(Activity activity){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package",activity.getPackageName(),null));
        activity.startActivity(intent);
    }

}
