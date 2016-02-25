package org.dync.teameeting.chatmessage;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.dync.teameeting.R;
import org.dync.teameeting.TeamMeetingApp;
import org.dync.teameeting.bean.ReqSndMsgEntity;
import org.dync.teameeting.db.CRUDChat;
import org.dync.teameeting.sdkmsgclient.jni.JMClientHelper;
import org.dync.teameeting.sdkmsgclient.jni.JMClientType;
import org.dync.teameeting.structs.EventType;
import org.dync.teameeting.ui.helper.ActivityTaskHelp;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;
import de.greenrobot.event.EventBus;


/**
 * Created by zhangqilu on 2016/1/8.
 */
public class ChatMessageClient implements JMClientHelper {
    private Message mMessage;
    private String TAG = "ChatMessageClient";
    private boolean mDebug = TeamMeetingApp.mIsDebug;
    private ArrayList<ChatMessageObserver> mObServers = new ArrayList<ChatMessageObserver>();
    private Context context;

    public ChatMessageClient(Context context) {
        this.context = context;
    }

    /**
     * regiseter
     *
     * @param observer
     */
    public synchronized void registerObserver(ChatMessageObserver observer) {
        if ((observer != null) && !mObServers.contains(observer)) {
            mObServers.add(observer);
        }
    }

    public synchronized void unregisterObserver(ChatMessageObserver observer) {
        if ((observer != null) && mObServers.contains(observer)) {
            mObServers.remove(observer);
        }
    }

    /**
     * notify
     *
     * @param reqSndMsg
     */
    public synchronized void notifyRequestMessage(ReqSndMsgEntity reqSndMsg) {
        for (ChatMessageObserver observer : mObServers) {
            Log.e(TAG, "notifyRequestMessage: " + 1);
            observer.OnReqSndMsg(reqSndMsg);
        }
    }

    //

    /**
     * implement for JMClientHelper
     */


    public void OnSndMsg(String msg) {
        if (mDebug) {
            Logger.e(msg);
        }
        if (msg != null) {
            senMag(msg);
        }
    }

    private void senMag(String msg) {
        Gson gson = new Gson();
        ReqSndMsgEntity reqSndMsgEntity = gson.fromJson(msg, ReqSndMsgEntity.class);

        if (mDebug) {
            Log.e(TAG, reqSndMsgEntity.getFrom() + "---" + TeamMeetingApp.getTeamMeetingApp().getDevId());
        }

        if (reqSndMsgEntity.getTags() == JMClientType.MCSENDTAGS_TALK) {
            CRUDChat.queryInsert(context, reqSndMsgEntity);
        }

        notifyRequestMessage(reqSndMsgEntity);

        //Are push local news
        if (!ActivityTaskHelp.isPackageNameonResume(context, context.getPackageName())) {
            sendPushNotifiaction(reqSndMsgEntity);
        }
    }

    int i = 11111111;
    public void sendPushNotifiaction(ReqSndMsgEntity reqSndMsgEntity) {

        int tags = 0;
        String title;
        if (reqSndMsgEntity.getTags() == JMClientType.MCSENDTAGS_TALK) {
            tags = 1;
            title = context.getString(R.string.notifi_str_new_message) + reqSndMsgEntity.getCont();
        } else if (reqSndMsgEntity.getTags() == JMClientType.MCSENDTAGS_ENTER) {
            tags = 2;
            title = context.getString(R.string.notifi_str_enter_room) + reqSndMsgEntity.getRoom();
        } else {
            return;
        }

        JPushLocalNotification ln = new JPushLocalNotification();
        ln.setBuilderId(0);
        ln.setTitle("Teameeting");
        ln.setContent(title);
        i++;
        ln.setNotificationId(i);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tags", tags);
        map.put("roomid", reqSndMsgEntity.getRoom());

        JSONObject json = new JSONObject(map);
        ln.setExtras(json.toString());
        JPushInterface.addLocalNotification(context.getApplicationContext(), ln);

    }


    @Override
    public void OnGetMsg(String msg) {
        String s = "OnReqGetMsg msg:" + msg;

        if (mDebug) {
            Log.e(TAG, "OnReqGetMsg: " + s);
        }
    }

    @Override
    public void OnMsgServerConnected() {
        mMessage = new Message();
        mMessage.what = EventType.MSG_MESSAGE_SERVER_CONNECTED.ordinal();
        EventBus.getDefault().post(mMessage);

        if (mDebug) {
            Log.e(TAG, "OnMsgServerConnected: ");
        }
    }

    @Override
    public void OnMsgServerDisconnect() {
        if (mDebug) {
            Log.e(TAG, "OnMsgServerDisconnect: ");
        }
    }

    @Override
    public void OnMsgServerConnectionFailure() {
        if (mDebug) {
            Log.i(TAG, "OnMsgServerConnectionFailure: ");
        }
    }

    @Override
    public void OnMsgServerState(int connStatus) {
        if (mDebug) {
            Log.i(TAG, "OnMsgServerState: " + connStatus);
        }
    }


    public interface ChatMessageObserver {
        void OnReqSndMsg(ReqSndMsgEntity reqSndMsg);
    }
}
