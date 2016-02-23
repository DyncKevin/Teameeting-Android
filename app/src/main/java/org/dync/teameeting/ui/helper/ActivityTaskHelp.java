package org.dync.teameeting.ui.helper;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

/**
 * Created by Xiao_Bailong on 2016/2/23.
 */
public class ActivityTaskHelp {
    /**
     * 判断程序是否在前台运行
     * 1. 如果不在前台运行， 发送通知
     * 2. 在前台运行，
     */
    public static boolean isPackageNameonResume(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.e("e", "pkg:" + cn.getPackageName());
        if (cn.getPackageName().equals(packageName)) {
            return true;
        }
        return false;
    }


}
