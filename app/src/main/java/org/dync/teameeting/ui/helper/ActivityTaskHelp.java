package org.dync.teameeting.ui.helper;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by Xiao_Bailong on 2016/2/23.
 */
public class ActivityTaskHelp {
    /**
     * Determine whether the program is running in the foreground
     */
    public static boolean isPackageNameonResume(Context context, String packageName) {
        if (packageName == null) {
            packageName = context.getPackageName();
        }
        Log.e("ActivityTaskHelp", "isPackageNameonResume: " + packageName);
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.e("e", "pkg:" + cn.getPackageName());
        if (cn.getPackageName().equals(packageName)) {
            return true;
        }
        return false;
    }

    public static boolean isActivityNameRun(Context context, String className) {
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        Log.e("e", "pkg:" + cn.getPackageName());
        if (cn.getClassName().equals(className)) {
            return true;
        }
        return false;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isBackgroundRunning(Context context) {
        String processName = context.getPackageName();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);

        if (activityManager == null) return false;

        List<ActivityManager.RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : processList) {
            if (process.processName.startsWith(processName)) {
                //如果有，就证明该程序正在运行。
                return true;
                /* if (process.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.e(context.getPackageName(), "处于后台" + process.processName);
                    return true;
                } else {
                    Log.e(context.getPackageName(), "处于前台" + process.processName);
                    return false;
                }
                */
            }
        }
        return false;
    }



}
