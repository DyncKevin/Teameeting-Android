package org.dync.teameeting.ui.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.TableRow;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.mm.sdk.platformtools.Util;

import org.dync.teameeting.R;
import org.dync.teameeting.TeamMeetingApp;
import org.dync.teameeting.structs.Constants;

import java.util.List;

/**
 * @author zhulang <br/>
 *         org.dync.teameeting.helper ShareHelper create at 2015-12-22
 *         上午10:59:20
 */
public class ShareHelper {
    private Context context;

    public ShareHelper(Context context) {
        this.context = context;
    }


    /**
     * 调用系统界面，给指定的号码发送短信，并附带短信内容
     *
     * @param context
     * @param number
     * @param body
     */
    public void shareSMS(Context context, String number, String body) {
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(Uri.parse("smsto:" + number));
        sendIntent.putExtra("sms_body", body);
        context.startActivity(sendIntent);
    }

    public void shareWeiXin(String msgTitle, String msgText, String webUrl) {
        msgText = msgText + webUrl;
        ShareItem share = new ShareItem("分享到.....",
                "com.tencent.mm.ui.tools.ShareImgUI", "com.tencent.mm");
        //shareWeiXinUrl(TeamMeetingApp.getTeamMeetingApp().getContext(), webUrl);
        shareMsg(msgTitle, msgText, share);
    }

    public void shareWeiXinUrl(Context context, String url) {

        IWXAPI api = WXAPIFactory.createWXAPI(context, Constants.APP_ID);

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = "http://www.baidu.com";
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title WebPage Title Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long";
        msg.description = "WebPage Description WebPage Description WebPage Description WebPage Description WebPage Description WebPage Description WebPage Description WebPage Description WebPage Description Very Long Very Long Very Long Very Long Very Long Very Long Very Long";
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_ico);
        msg.thumbData = Util.bmpToByteArray(thumb, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = true ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);

    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * @param context
     * @param msgTitle
     * @param msgText
     * @param share
     */
    private void shareMsg(String msgTitle, String msgText, ShareItem share) {
        if (!share.packageName.isEmpty() && !isAvilible(share.packageName)) {
            Toast.makeText(context, "请安装微信" + share.title, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Intent intent = new Intent("android.intent.action.SEND");
        if (msgText.equals("")) {
            intent.setType("text/plain");
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!share.packageName.isEmpty()) {
            intent.setComponent(new ComponentName(share.packageName, share.activityName));
            context.startActivity(intent);
        } else {
            context.startActivity(Intent.createChooser(intent, msgTitle));
        }
    }

    /**
     * @param context
     * @param packageName
     * @return
     */
    public boolean isAvilible(String packageName) {
        PackageManager packageManager = context.getPackageManager();

        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (((PackageInfo) pinfo.get(i)).packageName
                    .equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    private class ShareItem {
        String title;

        String activityName;
        String packageName;

        public ShareItem(String title, String activityName, String packageName) {
            this.title = title;
            this.activityName = activityName;
            this.packageName = packageName;
        }
    }

}
