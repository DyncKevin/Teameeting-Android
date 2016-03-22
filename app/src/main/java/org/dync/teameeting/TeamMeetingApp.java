package org.dync.teameeting;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.widget.Toast;

import com.pgyersdk.crash.PgyCrashManager;
import com.tencent.mm.sdk.openapi.IWXAPI;

import org.anyrtc.Anyrtc;
import org.dync.teameeting.bean.SelfData;
import org.dync.teameeting.chatmessage.ChatMessageClient;
import org.dync.teameeting.receiver.NetWorkReceiver;
import org.dync.teameeting.sdkmsgclient.msgs.TMMsgSender;
import org.dync.teameeting.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class TeamMeetingApp extends Application {

    private Context context;
    public static boolean mIsDebug = true;// debug deal with
    private static final String TAG = "Application";
    private static final boolean mDebug = true;
    private static TeamMeetingApp mTeamMeetingApp;
    private static ChatMessageClient mChatMessageClient;

    private static SelfData mSelfData;
    private NetWorkReceiver mNetReceiver;
    public static boolean isPad = false;
    private static TMMsgSender mMsgSender;

    public static List<String> activityList = new ArrayList<String>();

    public static Activity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(Activity mainActivity) {
        TeamMeetingApp.mainActivity = mainActivity;
    }

    public static Activity mainActivity ;
    public static List<String> getActivityList() {
        return activityList;
    }

    public static TeamMeetingApp getTeamMeetingApp() {
        return mTeamMeetingApp;
    }

    /*chat message deal with*/
    public static TMMsgSender getmMsgSender() {
        return mMsgSender;
    }

    public void setmMsgSender(TMMsgSender msgSender) {
        mMsgSender = msgSender;
    }

    public static void setSelfData(SelfData selfData) {
        mSelfData = selfData;
    }

    public static SelfData getmSelfData() {
        return mSelfData;
    }

    public static ChatMessageClient getmChatMessageClient() {
        return mChatMessageClient;
    }

    private IWXAPI api;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        context = this;
        mSelfData = new SelfData();
        mChatMessageClient = new ChatMessageClient(context);
        registerReceiver();

        isPad = ScreenUtils.isPad(this);
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
       // refWatcher = LeakCanary.install(this);

        PgyCrashManager.register(this);

        Anyrtc.InitAnyrtc("13103994", "de095967d87cd6f9a51ec4e3ee9a0ab7", "E7FCkvPeaRBWGIxtO+mTjoJqu+TmqEDRNyi9YyFu82o", "Teameeting");
    }

  /*  public static RefWatcher getRefWatcher(Context context) {
        TeamMeetingApp application = (TeamMeetingApp) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;*/

    public Context getContext() {
        return context;
    }

    /**
     * registerReceiver
     */
    private void registerReceiver() {
        mNetReceiver = new NetWorkReceiver();
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetReceiver, netFilter);
    }

    public TeamMeetingApp() {
        super();
        mTeamMeetingApp = this;
    }


    /**
     * get the Token from manifest.xml
     *
     * @return the value of Token
     */
    public String getToken() {
        Bundle bundle = null;
        String tokenKey = "";
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            bundle = info.metaData;
            if (bundle != null) {
                tokenKey = bundle.getString("TOKEN");
            }
        } catch (PackageManager.NameNotFoundException localNameNotFoundException1) {

        }
        return tokenKey;
    }

    public void Destroy() {
        unregisterReceiver(mNetReceiver);
        if (mMsgSender != null)
            mMsgSender.TMUnin();
    }

    /**
     * Judge if device has SD card
     */
    public boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int string) {
        Toast.makeText(this, getString(string), Toast.LENGTH_SHORT).show();
    }

    public String getVersionName() {
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pkgInfo.versionName;
    }

    public int getVersionCode() {
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pkgInfo.versionCode;
    }

    /**
     * get the device id unique
     *
     * @return the device id
     */
    public String getDevId() {
        return Secure.getString(getContentResolver(), Secure.ANDROID_ID);
    }

}
