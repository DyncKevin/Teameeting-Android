package org.dync.teameeting.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.dync.teameeting.R;
import org.dync.teameeting.TeamMeetingApp;
import org.dync.teameeting.bean.MeetingListEntity;
import org.dync.teameeting.http.HttpContent;
import org.dync.teameeting.structs.EventType;
import org.dync.teameeting.structs.ExtraType;
import org.dync.teameeting.structs.Intent_KEY;
import org.dync.teameeting.ui.helper.Anims;
import org.dync.teameeting.ui.helper.ShareHelper;
import org.dync.teameeting.widgets.BottomMenu;
import org.dync.teameeting.widgets.BottomMenu.OnTouchSpeedListener;
import org.dync.teameeting.widgets.SlideSwitch;
import org.dync.teameeting.widgets.SlideSwitch.SlideListener;

public class RoomSettingActivity extends BaseActivity implements View.OnClickListener {
    private Context context;
    private Boolean mDebug = TeamMeetingApp.mIsDebug;
    private String TAG = "RoomSettingActivity";
    private TextView mTvRoomName;
    private TextView mTvJoninRoom;
    private TextView mTvIniviteMessage;
    private TextView mTvInviteWeixin;
    private TextView mTvCopyLink;
    private LinearLayout mLlNotifications;
    private LinearLayout mLlMeetingPrivate;
    private TextView mTvRenameRoom;
    private TextView mTvDeleteRoom;
    private TextView mTvClose;
    private SlideSwitch mSlideSwitch;
    private SlideSwitch mSlideSwitchPrivate;
    private MeetingListEntity mMeetingEntity;
    private String mMeetingName;
    private String mMeetingId;

    private int mPosition;

    private ShareHelper mShareHelper;

    private String mShareUrl = "Empty url";
    private ImageView ivNotifation;

    private boolean mMeetingPrivateFlag=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_setting);
        mShareHelper = new ShareHelper(RoomSettingActivity.this);
        context = this;
        initData();
        initLayout();
        inintSwitchState();

    }

    private void initData() {
        Intent intent = getIntent();
        mPosition = intent.getIntExtra(Intent_KEY.POSITION, 0);
        Bundle extras = intent.getExtras();
        mMeetingEntity = (MeetingListEntity) extras.getSerializable(Intent_KEY.MEETING_ENTY);
        mMeetingId = mMeetingEntity.getMeetingid();
        mMeetingName = mMeetingEntity.getMeetname();
        if (mDebug) {
            Log.e(TAG, mMeetingEntity.toString());
        }

        mSign = getSign();
        mShareUrl ="Let us see in a meeting!:"+"http://115.28.70.232/share_meetingRoom/#"+mMeetingId;
    }

    void initLayout() {
        mTvRoomName = (TextView) findViewById(R.id.tv_room_name);
        mTvRoomName.setText(mMeetingName);
        mTvJoninRoom = (TextView) findViewById(R.id.tv_join_room);
        mTvJoninRoom.setOnClickListener(this);

        mTvIniviteMessage = (TextView) findViewById(R.id.tv_invite_message);
        mTvIniviteMessage.setOnClickListener(this);

        mTvInviteWeixin = (TextView) findViewById(R.id.tv_invite_weixin);
        mTvInviteWeixin.setOnClickListener(this);

        mTvCopyLink = (TextView) findViewById(R.id.tv_copy_link);
        mTvCopyLink.setOnClickListener(this);

        mLlNotifications = (LinearLayout) findViewById(R.id.ll_notifications);
        mLlNotifications.setOnClickListener(this);
        // mLlNotifications.setOnTouchListener();

        mTvRenameRoom = (TextView) findViewById(R.id.tv_rename_room);
        mTvRenameRoom.setOnClickListener(this);

        mTvDeleteRoom = (TextView) findViewById(R.id.tv_delete_room);
        mTvDeleteRoom.setOnClickListener(this);

        mTvClose = (TextView) findViewById(R.id.tv_close);
        mTvClose.setOnClickListener(this);
        mSlideSwitch = (SlideSwitch) findViewById(R.id.ss_SlideSwitch);
        mSlideSwitch.setSlideListener(slideNotificationListener);

        mSlideSwitchPrivate = (SlideSwitch) findViewById(R.id.ss_SlideSwitch_private);
        mSlideSwitchPrivate.setSlideListener(mslideMeetingPrivateListener);

        mLlMeetingPrivate = (LinearLayout) findViewById(R.id.ll_private);
        mLlMeetingPrivate.setOnClickListener(this);

        ivNotifation = (ImageView) findViewById(R.id.iv_notifications);

        BottomMenu bottomMenu = (BottomMenu) findViewById(R.id.bottomMenu);
        bottomMenu.setOnTouchQuickSpeedListener(onTouchSpeedListener);
    }

    private void inintSwitchState() {
        boolean state = mMeetingEntity.getPushable() == 1 ? true : false;
        mSlideSwitch.setState(state);
        if (!state) {
            Anims.ScaleAnim(ivNotifation, 0, 1, 10);
        }

        mMeetingPrivateFlag = mMeetingEntity.getMeetusable() == 2 ? true : false;
        mSlideSwitchPrivate.setState(mMeetingPrivateFlag);

    }

    /**
     * 　Touch slide Listener
     */
    OnTouchSpeedListener onTouchSpeedListener = new OnTouchSpeedListener() {

        @Override
        public void touchSpeed(int velocityX, int velocityY) {
            setResult(ExtraType.RESULT_CODE_ROOM_SETTING_CLOSE);
            finishActivity();
        }
    };


    private  void  meetingPrivateUIUpdate(){
        if(mMeetingPrivateFlag){
            mTvIniviteMessage.setTextColor(getResources().getColor(R.color.black));
            mTvInviteWeixin.setTextColor(getResources().getColor(R.color.black));
            mTvCopyLink.setTextColor(getResources().getColor(R.color.black));
            mTvIniviteMessage.setClickable(false);
            mTvInviteWeixin.setClickable(false);
            mTvCopyLink.setClickable(false);
        }else{
            mTvIniviteMessage.setTextColor(getResources().getColor(R.color.white));
            mTvInviteWeixin.setTextColor(getResources().getColor(R.color.white));
            mTvCopyLink.setTextColor(getResources().getColor(R.color.white));
            mTvIniviteMessage.setClickable(true);
            mTvInviteWeixin.setClickable(true);
            mTvCopyLink.setClickable(true);
        }
    }

    /**
     * mslideMeetingPrivateListener
     */
    SlideListener mslideMeetingPrivateListener = new SlideListener() {
        public void open() {
            mNetWork.updateRoomEnable(mSign,mMeetingId,"2");
            mMeetingPrivateFlag =true;
            meetingPrivateUIUpdate();
        }

        @Override
        public void close() {
            mNetWork.updateRoomEnable(mSign,mMeetingId,"1");
            mMeetingPrivateFlag = false;
            meetingPrivateUIUpdate();
        }
    };

    /**
     * slideNotificationListener
     */

    SlideListener slideNotificationListener = new SlideListener() {
        @Override
        public void open() {

            mNetWork.updateRoomPushable(mSign, mMeetingId, 1 + "");
            Anims.ScaleAnim(ivNotifation, 1, 0, 500);
            Toast.makeText(context, "打开推送", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void close() {

            mNetWork.updateRoomPushable(mSign, mMeetingId, 0 + "");
            Anims.ScaleAnim(ivNotifation, 0, 1, 500);
            String url = "meeting/getMeetingInfo/" + mMeetingId;

            Log.e(TAG, "close: mMeetingId" + mMeetingId);
            HttpContent.get(url, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int i, Header[] headers, String respospone, Throwable throwable) {
                    Log.e(TAG, "onFailure:  respospone" + respospone);
                }

                @Override
                public void onSuccess(int i, Header[] headers, String respospone) {
                    Log.e(TAG, "onSuccess:  respospone" + respospone);
                }
            });

        }
    };






    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.tv_close:
                setResult(ExtraType.RESULT_CODE_ROOM_SETTING_CLOSE);
                if (mDebug)
                    Log.e(TAG, "onClick: setResult");

                finishActivity();
                return;
            case R.id.tv_join_room:

                intent = new Intent(RoomSettingActivity.this, MeetingActivity.class);
                intent.putExtra("meetingName", mMeetingName);
                finishActivity();
                break;
            case R.id.tv_invite_message:
                // SMS

                mShareHelper.shareSMS(this, "", mShareUrl);

                break;
            case R.id.tv_invite_weixin:
                // weixin

                mShareHelper.shareWeiXin("Share into ... ", "", mShareUrl);
                finishActivity();
                break;
            case R.id.tv_copy_link:

                intent = new Intent();
                intent.putExtra("shareUrl",mShareUrl);
                setResult(ExtraType.RESULT_CODE_ROOM_SETTING_COPY_LINK,intent);
                finish();

                break;
            case R.id.ll_notifications:
                if (mSlideSwitch.isOpen) {
                    mSlideSwitch.moveToDest(false);
                } else {
                    mSlideSwitch.moveToDest(true);
                }
                break;
            case R.id.ll_private:
                if (mSlideSwitchPrivate.isOpen) {
                    mSlideSwitchPrivate.moveToDest(false);
                } else {
                    mSlideSwitchPrivate.moveToDest(true);
                }
                break;

            case R.id.tv_rename_room:

                intent = new Intent();
                intent.putExtra("position", mPosition);
                intent.putExtra("meetingId", mMeetingId);
                intent.putExtra("meetingName", mMeetingName);
                setResult(ExtraType.RESULT_CODE_ROOM_SETTING_RENAME, intent);

                finishActivity();
                break;
            case R.id.tv_delete_room:
                intent = new Intent();
                intent.putExtra("position", mPosition);
                intent.putExtra("meetingId", mMeetingId);
                setResult(ExtraType.RESULT_CODE_ROOM_SETTING_DELETE, intent);
                finishActivity();
                break;

            default:
                break;
        }

    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.activity_close_enter,
                R.anim.activity_close_exit);
    }

    @Override
    public void onEventMainThread(Message msg) {
        switch (EventType.values()[msg.what]) {
            case MSG_UPDATE_ROOM_PUSHABLE_SUCCESS:
                if (mDebug)
                    Log.e(TAG, "onEventMainThread: MSG_UPDATE_ROOM_PUSHABLE_SUCCESS");
                break;
            case MSG_UPDATE_ROOM_PUSHABLE_FAILED:
                if (mDebug)
                    Log.e(TAG, "onEventMainThread: MSG_UPDATE_ROOM_PUSHABLE_FAILED");
                break;
        }
    }


}
