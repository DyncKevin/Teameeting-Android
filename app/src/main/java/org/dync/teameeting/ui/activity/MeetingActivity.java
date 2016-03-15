package org.dync.teameeting.ui.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewPropertyAnimator;

import org.anyrtc.AnyrtcMeet;
import org.anyrtc.common.MeetEvents;
import org.dync.teameeting.R;
import org.dync.teameeting.TeamMeetingApp;
import org.dync.teameeting.bean.ChatMessage;
import org.dync.teameeting.bean.ChatMessage.Type;
import org.dync.teameeting.bean.MessageListEntity;
import org.dync.teameeting.bean.ReqSndMsgEntity;
import org.dync.teameeting.db.CRUDChat;
import org.dync.teameeting.http.NetWork;
import org.dync.teameeting.sdkmsgclient.jni.JMClientType;
import org.dync.teameeting.sdkmsgclient.msgs.TMMsgSender;
import org.dync.teameeting.structs.EventType;
import org.dync.teameeting.structs.NetType;
import org.dync.teameeting.ui.adapter.ChatMessageAdapter;
import org.dync.teameeting.ui.helper.Anims;
import org.dync.teameeting.ui.helper.DialogHelper;
import org.dync.teameeting.ui.helper.MeetingAnim;
import org.dync.teameeting.ui.helper.MeetingAnim.AnimationEndListener;
import org.dync.teameeting.ui.helper.ShareHelper;
import org.dync.teameeting.utils.AnyRTCViews;
import org.dync.teameeting.utils.ScreenUtils;
import org.dync.teameeting.widgets.PopupWindowCustom;
import org.dync.teameeting.widgets.PopupWindowCustom.OnPopupWindowClickListener;
import org.dync.teameeting.widgets.ReFlashListView;
import org.dync.teameeting.widgets.RoomControls;
import org.dync.teameeting.widgets.VitualKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.greenrobot.event.EventBus;

/**
 * @author zhangqilu org.dync.teammeeting.activity MeetingActivity create at
 *         2015-12-11 5:02:32
 */

public class MeetingActivity extends MeetingBaseActivity implements MeetEvents, ReFlashListView.IReflashListener {
    // Local preview screen position before call is connected.
    private static final boolean mDebug = TeamMeetingApp.mIsDebug;
    private static final String TAG = "MeetingActivity";

    private static final int ANIMATOR_TANSLATION = 0X01;
    private static final int MESSAGE_UPDATE = 0X02;
    boolean MCSENDTAGS_SUBSCRIBE = false;
    // private AnyrtcM2Mutlier mAnyM2Mutlier;
    private AnyrtcMeet mAnyM2Mutlier;
    private MeetingAnim mMettingAnim;
    private ImageButton mChatButton, mInviteButton;
    private RoomControls mControlLayout;
    private RelativeLayout mTopbarLayout;
    private VitualKey mParentLayout;
    private ImageButton mVoiceButton, mCameraButton, mHangUpButton,
            mSwitchCameraButton, mCameraOffButton;
    private boolean mMeetingCameraFlag = true, mMeetingCameraOffFlag = true, mMeetingVoiceFlag = true;

    private TextView mTvRoomName;

    private PopupWindowCustom mPopupWindowCustom;
    private ShareHelper mShareHelper;
    private String mShareUrl;

    private AnyRTCViews mAnyrtcViews;
    // Left distance of this control button relative to its parent
    int mLeftDistanceCameraBtn;
    int mLeftDistanceHangUpBtn;
    int mLeftDistanceVoiceBtn;

    // chating
    private RelativeLayout mChatLayout;
    private ImageButton mChatClose;
    private Button mSendMessage;
    private TextView mTvRemind;
    private TextView mTvMessageCount;
    private String mUserId;
    private boolean mMessageShowFlag = true;
    private TMMsgSender mMsgSender;
    private int mMessagePageNum = 1;

    private SweetAlertDialog mNetErrorSweetAlertDialog;
    private int mNotifTags;
    private Message closeMsg = null;
    private RelativeLayout mRlChatButton;
    private TextView tvDuoyu;
    private String mRoomName;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private ReFlashListView mChatView;
    private ImageView mCloseVoice;
    private ImageView mCloseVideo;
    private EditText mMsg;
    private List<ChatMessage> mDatas = new ArrayList<ChatMessage>();
    private List<MessageListEntity> mMessageListEntity = new ArrayList<MessageListEntity>();
    private ChatMessageAdapter mAdapter;
    private InputMethodManager mIMM;
    boolean mChatLayoutShow = false;
    private String mMeetingId;
    private NetWork mNetWork;

    // private VideoViews mVideoView;
    private String mPublishId;
    private HashMap<String, Boolean> mVoiceSetting = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> mVideoSetting = new HashMap<String, Boolean>();

    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ANIMATOR_TANSLATION:

                    mVoiceButton.setVisibility(View.VISIBLE);
                    mHangUpButton.setVisibility(View.VISIBLE);
                    mSwitchCameraButton.setVisibility(View.GONE);
                    mCameraOffButton.setVisibility(View.GONE);

                    break;
                case MESSAGE_UPDATE:
                    mMessageListEntity = TeamMeetingApp.getmSelfData().getMessageListEntityList();
                    int oldSize = mDatas.size();
                    for (int i = 0; i < mMessageListEntity.size() - 1; i++) {

                        String message = mMessageListEntity.get(i).getMessage();
                        long time = mMessageListEntity.get(i).getSendtime();
                        ChatMessage chatMessage;
                        if (mMessageListEntity.get(i).getUserid().equals(mUserId)) {
                            chatMessage = new ChatMessage(Type.OUTPUT, message, "name", time + "");
                        } else {
                            chatMessage = new ChatMessage(Type.INPUT, message, "name", time + "");
                        }

                        mDatas.add(0, chatMessage);
                    }

                    mAdapter.notifyDataSetChanged();
                    if (mMessagePageNum == 1)
                        mChatView.setSelection(mDatas.size() - 1);
                    else
                        mChatView.setSelection(mDatas.size() - oldSize);
                default:
                    break;
            }

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        inintData();

        List<String> activityList = TeamMeetingApp.getActivityList();
        activityList.add(0, mMeetingId);
        if (mDebug)
            Log.e(TAG, "onStart:save id " + mMeetingId);
    }

    private void inintData() {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EventBus.getDefault().register(this);

        mNetErrorSweetAlertDialog = DialogHelper.createNetErroDilaog(getApplicationContext(), sweetClickListener);

        mIMM = (InputMethodManager) MeetingActivity.this.getSystemService(MainActivity.INPUT_METHOD_SERVICE);


        mAnyrtcViews = new AnyRTCViews((RelativeLayout) findViewById(R.id.rl_videos), TeamMeetingApp.getTeamMeetingApp().getContext(), mCloseVoice, mCloseVideo);
        mAnyrtcViews.setVideoViewPeopleNumEvent(mVideoViewPeopleNumEvent);
        mAnyM2Mutlier = new AnyrtcMeet(TeamMeetingApp.getMainActivity(), this);


        Intent intent = getIntent();
        mMeetingId = intent.getStringExtra("meetingId");
        mUserId = intent.getStringExtra("userId");
        mNotifTags = intent.getIntExtra("tags", 0);
        mRoomName = getIntent().getStringExtra("meetingName");
        String anyrtcId = intent.getStringExtra("anyrtcId");
        mTvRoomName.setText(mRoomName);

        mAnyM2Mutlier.Join(anyrtcId);
        mAnyM2Mutlier.InitAnyRTCViewEvents(mAnyrtcViews);


        mMsgSender = TeamMeetingApp.getmMsgSender();
        mNetWork = new NetWork();
        mShareHelper = new ShareHelper(MeetingActivity.this);
        mMettingAnim = new MeetingAnim();
        mMettingAnim.setAnimEndListener(mAnimationEndListener);

        mShareUrl = "Let us see in a meeting!:" + "http://115.28.70.232/share_meetingRoom/#" + mMeetingId;

        mChatView.setInterface(this);

        leaveMessageDealWith();

        int code = mMsgSender.TMOptRoom(JMClientType.MCCMD_ENTER, mMeetingId, mRoomName, "");
        if (code >= 0) {
            if (mDebug) {
                Log.e(TAG, "inintData: " + "TMEnterRoom Successed");
            }
        } else if (mDebug) {
            Log.e(TAG, "inintData: " + "TMEnterRoom Failed");
        }

        mNetWork.getMeetingMsgList(getSign(), mMeetingId, "" + mMessagePageNum, 20 + "");
        if (mNotifTags == 1) {

            if (TeamMeetingApp.isPad) {
                chatLayoutControl(300);
            } else {
                mMessageShowFlag = false;
                mChatLayout.setVisibility(View.VISIBLE);
            }
        }
    }


    /* Init UI */
    private void initView() {

        // Create UI controls.
        mParentLayout = (VitualKey) findViewById(R.id.meet_parent);
        mTopbarLayout = (RelativeLayout) findViewById(R.id.rl_meeting_topbar);
        mControlLayout = (RoomControls) findViewById(R.id.rl_meeting_control);
        tvDuoyu = (TextView) findViewById(R.id.tv_duoyu);
        mChatButton = (ImageButton) findViewById(R.id.imgbtn_chat);
        mInviteButton = (ImageButton) findViewById(R.id.imgbtn_invite);
        mTvRoomName = (TextView) findViewById(R.id.tv_room_name);
        mTvRemind = (TextView) findViewById(R.id.tv_remind);
        mRlChatButton = (RelativeLayout) findViewById(R.id.rl_chat_bottom);

        mCloseVideo = (ImageView) findViewById(R.id.iv_close_video);
        mCloseVoice = (ImageView) findViewById(R.id.iv_close_voice);
        mVoiceButton = (ImageButton) findViewById(R.id.meeting_voice);
        mCameraButton = (ImageButton) findViewById(R.id.meeting_camera);
        mHangUpButton = (ImageButton) findViewById(R.id.meeting_hangup);
        mSwitchCameraButton = (ImageButton) findViewById(R.id.meeting_camera_switch);
        mCameraOffButton = (ImageButton) findViewById(R.id.meeting_camera_off);

        mInviteButton.setOnClickListener(onClickListener);
        mChatButton.setOnClickListener(onClickListener);
        mVoiceButton.setOnClickListener(onClickListener);
        mCameraButton.setOnClickListener(onClickListener);
        mHangUpButton.setOnClickListener(onClickListener);
        mSwitchCameraButton.setOnClickListener(onClickListener);
        mCameraOffButton.setOnClickListener(onClickListener);

        mParentLayout.setInterface(mLayoutKeyChange);

        // Chat ui inint
        mTvMessageCount = (TextView) findViewById(R.id.tv_message_count);
        mChatLayout = (RelativeLayout) findViewById(R.id.rl_chating);
        mSendMessage = (Button) findViewById(R.id.btn_chat_send);
        mChatClose = (ImageButton) findViewById(R.id.imgbtn_back);
        mChatView = (ReFlashListView) findViewById(R.id.listView_chat);
        mMsg = (EditText) findViewById(R.id.et_chat_msg);
        mSendMessage.setOnClickListener(onClickListener);
        mChatClose.setOnClickListener(onClickListener);
        //下拉刷新
        // initSwipeRefreshLayout();
        mAdapter = new ChatMessageAdapter(this, mDatas);
        mChatView.setAdapter(mAdapter);

    }


    float downX = 0;
    float downY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                Log.e(TAG, downX + "onTouchEvent ACTION_DOWN " + downY);

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float moveX = event.getX() - downX;
                float moveY = event.getY() - downY;
                Log.e(TAG, moveY + "onTouchEvent ACTION_UP " + moveX);
                if (Math.abs(moveX) > Math.abs(moveY) && TeamMeetingApp.isPad) {
                    chatLayoutControl(moveX);
                } else {
                    contralAnim();
                }

                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * chataShow
     *
     * @param moveX
     */
    public void chatLayoutControl(float moveX) {
        int controllMove = controllerMoveDistance(mChatLayout);
        int showTime = 500;
        if (moveX > 0 && !mChatLayoutShow) {
            mChatLayoutShow = true;
            mMessageShowFlag = false;
            Anims.animateRightMarginTo(mChatLayout, 0, mChatLayout.getWidth() - tvDuoyu.getWidth(), showTime, Anims.ACCELERATE);
            Anims.animateRightMarginTo(mControlLayout, 0, controllMove, showTime, Anims.ACCELERATE);
            Anims.animateRightMarginTo(mControlLayout, 0, controllMove, showTime, Anims.ACCELERATE);
            Anims.animateRightMarginTo(mTvRemind, 0, controllMove, showTime, Anims.ACCELERATE);
            // Anims.animateRightMarginTo(mTvRoomName, 0, controllMove, showTime, Anims.ACCELERATE);
            mTvMessageCount.setVisibility(View.INVISIBLE);

        } else if (moveX < 0 && mChatLayoutShow) {
            mChatLayoutShow = false;
            mMessageShowFlag = true;
            Anims.animateRightMarginTo(mChatLayout, mChatLayout.getWidth() - tvDuoyu.getWidth(), 0, showTime, Anims.ACCELERATE);
            Anims.animateRightMarginTo(mControlLayout, controllMove, 0, showTime, Anims.ACCELERATE);
            Anims.animateRightMarginTo(mTvRemind, controllMove, 0, showTime, Anims.ACCELERATE);
            CRUDChat.deleteByMeetingId(MeetingActivity.this, mMeetingId);

        }
    }

    private void contralAnim() {

        if (MCSENDTAGS_SUBSCRIBE) {
            if (mControlLayout.mAvailable) {
                mControlLayout.hide();
                ViewPropertyAnimator.animate(mTopbarLayout).translationY(
                        -mTopbarLayout.getHeight());
                ViewPropertyAnimator.animate(mCloseVoice).translationY(
                        -mTopbarLayout.getHeight());
            } else {
                mControlLayout.show();
                ViewPropertyAnimator.animate(mTopbarLayout).translationY(0f);
                ViewPropertyAnimator.animate(mCloseVoice).translationY(0f);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mPopupWindowCustom != null) {
            mPopupWindowCustom.dismiss();
            mPopupWindowCustom = null;
        }

        if (mAnyrtcViews != null) {

            mAnyrtcViews.onScreenChanged();
        }


        super.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN
                ) {
            if (TeamMeetingApp.isPad) {
                if (mChatLayoutShow)
                    chatLayoutControl(-100);
                else
                    msgSenderLeave();
            } else {
                if (mChatLayout.getVisibility() == View.VISIBLE) {
                    mMessageShowFlag = true;
                    mChatLayout.setVisibility(View.GONE);
                    mTopbarLayout.setVisibility(View.VISIBLE);
                    mControlLayout.setVisibility(View.VISIBLE);
                    //delete db  data
                    CRUDChat.deleteByMeetingId(MeetingActivity.this, mMeetingId);
                    mTvMessageCount.setVisibility(View.INVISIBLE);
                } else {
                    msgSenderLeave();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        measureLeftDistance();
        super.onWindowFocusChanged(hasFocus);

    }

    /**
     * Measuring the distance button
     */
    private void measureLeftDistance() {
        mLeftDistanceCameraBtn = mCameraButton.getLeft()
                + mCameraButton.getWidth() / 2;
        mLeftDistanceHangUpBtn = mHangUpButton.getLeft()
                + mHangUpButton.getWidth() / 2;
        mLeftDistanceVoiceBtn = mVoiceButton.getLeft()
                + mVoiceButton.getWidth() / 2;
    }

    private AnimationEndListener mAnimationEndListener = new AnimationEndListener() {
        @Override
        public void onAnimationEnd(Animator arg0) {
            mVoiceButton.setVisibility(View.VISIBLE);
            mHangUpButton.setVisibility(View.VISIBLE);
            mSwitchCameraButton.setVisibility(View.GONE);
            mCameraOffButton.setVisibility(View.GONE);
            mMettingAnim.alphaAnimator(mVoiceButton, 1.0f, 1.0f, 100);
            mMettingAnim.alphaAnimator(mHangUpButton, 1.0f, 1.0f, 100);
        }
    };

    private OnPopupWindowClickListener mPopupWindowListener = new OnPopupWindowClickListener() {
        @Override
        public void onPopupClickListener(View view) {

            switch (view.getId()) {
                case R.id.ibtn_close:
                    mPopupWindowCustom.dismiss();
                    break;
                case R.id.ibtn_message:
                    mPopupWindowCustom.dismiss();
                    mShareHelper.shareSMS(MeetingActivity.this, "", mShareUrl);
                    break;
                case R.id.ibtn_weixin:
                    mPopupWindowCustom.dismiss();
                    mShareHelper.shareWeiXin("Share into ... ", "", mShareUrl);
                    break;
                case R.id.tv_copy:

                case R.id.btn_copy:
                    mPopupWindowCustom.dismiss();
                    DialogHelper.onClickCopy(MeetingActivity.this, mShareUrl);
                    break;

                default:
                    break;
            }
        }

    };

    /* set button clickListener */
    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View mView) {
            int code = 0;
            switch (mView.getId()) {
                case R.id.imgbtn_invite:
                    mPopupWindowCustom = new PopupWindowCustom(MeetingActivity.this, mInviteButton, mTopbarLayout,
                            mPopupWindowListener, mShareUrl);
                    break;

                case R.id.meeting_camera:
                    videoSetting();
                    break;
                case R.id.meeting_hangup:

                    msgSenderLeave();

                    break;
                case R.id.meeting_voice:

                    voiceSetting();

                    break;
                case R.id.meeting_camera_switch:

                    mAnyM2Mutlier.SwitchCamera();

                    break;
                case R.id.meeting_camera_off:

                    videoCloseSetting();
                    break;

                case R.id.imgbtn_chat:
                    if (TeamMeetingApp.isPad) {
                        int value = mChatLayoutShow == true ? -100 : 100;
                        chatLayoutControl(value);
                    } else {
                        mMessageShowFlag = false;
                        mChatLayout.setVisibility(View.VISIBLE);
                    }


                    break;
                case R.id.btn_chat_send:
                    sendMessageChat();
                    break;
                case R.id.imgbtn_back:
                    mMessageShowFlag = true;
                    CRUDChat.deleteByMeetingId(MeetingActivity.this, mMeetingId);
                    mTvMessageCount.setVisibility(View.INVISIBLE);
                    mIMM.hideSoftInputFromWindow(mMsg.getWindowToken(), 0);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mChatLayout.setVisibility(View.GONE);
                        }
                    }, 400);
                    break;
            }
        }


    };

    /**
     * Solve the problem of hide the virtual keyboard
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(TAG, "dispatchTouchEvent: " + super.dispatchTouchEvent(ev) + " ev.getAction() " + ev.getAction());

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // View v = getCurrentFocus();

            if (isShouldHideInput(mMsg, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(mMsg.getWindowToken(), 0);
                }
                //return false;
            }
            // return false;
            return super.dispatchTouchEvent(ev);
        }
        // Essential, otherwise all the components are there won't be TouchEvent
        if (getWindow().superDispatchTouchEvent(ev)) {
            Log.e(TAG, "dispatchTouchEvent: " + " getWindow() ");
            return true;
        }
        // return onTouchEvent(ev);
        return false;
    }

    /**
     * isShouldHideInput
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //To obtain input box's current location location to obtain input box's current location
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = ScreenUtils.getScreenWidth(getApplicationContext());
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * msgSenderLeave
     */
    private void msgSenderLeave() {
        int code = mMsgSender.TMOptRoom(JMClientType.MCCMD_LEAVE, mMeetingId, mRoomName, "");
        finish();
        if (code >= 0) {
            if (mDebug) {
                Log.e(TAG, "TMLeaveRoom Successed");
            }
        } else if (mDebug) {
            Log.e(TAG, "TMLeaveRoom Failed");
        }
    }

    /**
     * videoSetting
     */

    private void videoSetting() {
        if (!mMeetingCameraOffFlag) {
            mAnyM2Mutlier.SetLocalVideoEnabled(true);
            mCameraButton.setImageResource(R.drawable.btn_camera_on);
            mMeetingCameraOffFlag = true;
            if (mAnyrtcViews.LocalVideoTrack() != null) {
                mAnyrtcViews.LocalVideoTrack().setEnabled(true);
                mAnyrtcViews.updateLocalVideoImage(true);
            }
            return;
        }

        if (mMeetingCameraFlag) {
            mCameraButton.setImageResource(R.drawable.btn_camera_back);
            mVoiceButton.setVisibility(View.GONE);
            mHangUpButton.setVisibility(View.GONE);
            mSwitchCameraButton.setVisibility(View.VISIBLE);
            mCameraOffButton.setVisibility(View.VISIBLE);

            mMettingAnim.rotationOrApaha(mCameraButton,
                    mMeetingCameraFlag);
            mMettingAnim.translationAlphaAnimator(mSwitchCameraButton,
                    (mLeftDistanceCameraBtn - mLeftDistanceHangUpBtn),
                    0, 400, true);

            mMettingAnim.translationAlphaAnimator(mCameraOffButton,
                    (mLeftDistanceCameraBtn - mLeftDistanceVoiceBtn),
                    0, 400, true);

        } else {
            mCameraButton.setImageResource(R.drawable.btn_camera_on);
            mMettingAnim.rotationOrApaha(mCameraButton,
                    mMeetingCameraFlag);
            mMettingAnim.translationAlphaAnimator(mSwitchCameraButton,
                    0,
                    (mLeftDistanceCameraBtn - mLeftDistanceHangUpBtn),
                    300, false);
            mMettingAnim.translationAlphaAnimator(mCameraOffButton, 0,
                    (mLeftDistanceCameraBtn - mLeftDistanceVoiceBtn),
                    300, false);

        }

        mMeetingCameraFlag = !mMeetingCameraFlag;
    }


    /**
     * videoCloseSetting
     */
    private void videoCloseSetting() {

        if (mAnyrtcViews.LocalVideoTrack() != null) {
            mAnyrtcViews.LocalVideoTrack().setEnabled(false);
            mAnyrtcViews.updateLocalVideoImage(false);
            mAnyM2Mutlier.SetLocalVideoEnabled(false);
        }

        mCameraButton.setImageResource(R.drawable.btn_camera_off_select);
        mMettingAnim.rotationOrApaha(mCameraButton, mMeetingCameraFlag);
        mMettingAnim.translationAlphaAnimator(mSwitchCameraButton, 0,
                (mLeftDistanceCameraBtn - mLeftDistanceHangUpBtn), 300,
                false);
        mMettingAnim.translationAlphaAnimator(mCameraOffButton, 0,
                (mLeftDistanceCameraBtn - mLeftDistanceVoiceBtn), 300,
                false);
        mMeetingCameraOffFlag = false;
        mMeetingCameraFlag = true;


    }


    /**
     * voice Setting
     */
    private void voiceSetting() {

        if (mMeetingVoiceFlag) {
            mVoiceButton.setImageResource(R.drawable.btn_voice_off);
            mAnyrtcViews.updateLocalVoiceImage(false);
            mAnyM2Mutlier.SetLocalAudioEnabled(false);

        } else {
            mVoiceButton.setImageResource(R.drawable.btn_voice_on);
            mAnyrtcViews.updateLocalVoiceImage(true);
            mAnyM2Mutlier.SetLocalAudioEnabled(true);

        }
        mMeetingVoiceFlag = !mMeetingVoiceFlag;

    }


    /**
     * sendMessageChat
     */

    private void sendMessageChat() {
        final String pushMsg = mMsg.getText().toString();
        if (TextUtils.isEmpty(pushMsg)) {
            Toast.makeText(this, R.string.str_content_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mMsgSender.TMConnStatus() != JMClientType.CSCONNECTED) {
            Toast.makeText(this, R.string.str_message_server_not_connect, Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage to = new ChatMessage(Type.OUTPUT, pushMsg, "name", System.currentTimeMillis() + "");
        mDatas.add(to);
        mAdapter.notifyDataSetChanged();
        mChatView.setSelection(mDatas.size() - 1);
        mMsg.setText("");

        //  mNetWork.pushMeetingMsg(getSign(), mMeetingId, "push message", "notification");

        int code = mMsgSender.TMSndMsg(mMeetingId, mRoomName, pushMsg);
        if (code >= 0) {
            if (mDebug) {
                Log.e(TAG, "sendMessageChat: " + "TMSndMsg Successed");
            }
        } else if (mDebug) {
            Log.e(TAG, "sendMessageChat: " + "TMSndMsg Failed");
        }

    }

    /**
     * leaveMessageDealWith
     */
    private void leaveMessageDealWith() {
        String leaveMessageCount = CRUDChat.selectChatList(MeetingActivity.this, mMeetingId).size() + "";
        if (mDebug) {
            Log.e(TAG, "leaveMessageDealWith: leaveMessageCount " + leaveMessageCount);
        }
        if (mTvMessageCount.getVisibility() == View.INVISIBLE && !leaveMessageCount.equals("0")) {
            mTvMessageCount.setVisibility(View.VISIBLE);
        }
        mTvMessageCount.setText(leaveMessageCount);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause: ");
        super.onPause();
        mAnyM2Mutlier.OnPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
        mAnyM2Mutlier.OnResume();
/*        if (mVideoView.LocalVideoTrack() != null) {
            mVideoView.LocalVideoTrack().setEnabled(true);
        }
        mAnyM2Mutlier.SetLocalVideoEnabled(true);*/
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop: ");
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mDebug)
            Log.e(TAG, "onDestroy: ");
        List<String> activityList = TeamMeetingApp.getActivityList();
        activityList.clear();
        //mVideoView.CloseLocalRender();
        {// Close all
            if (mAnyM2Mutlier != null) {
                mAnyM2Mutlier.Leave();
                mAnyM2Mutlier = null;
            }
        }
        if(mAnyrtcViews!=null){
            mAnyrtcViews.destoryAnyRTCViews();
            mAnyrtcViews = null;
        }

        //TeamMeetingApp.getRefWatcher(TeamMeetingApp.getTeamMeetingApp().getContext()).watch(mAnyrtcViews);
        TeamMeetingApp.getRefWatcher(TeamMeetingApp.getTeamMeetingApp().getContext()).watch(mCloseVoice);

        super.onDestroy();

    }

    /**
     * VitualKey Change
     */
    private VitualKey.LayoutKeyChange mLayoutKeyChange = new VitualKey.LayoutKeyChange() {
        @Override
        public void onLayoutKeyChange(int b) {

            int screenHeight = ScreenUtils.getScreenHeight(MeetingActivity.this);
/*            if(mDebug)
            Log.e(TAG, "onLayoutKeyChange: "+" onLayoutKeyChange "+screenHeight);*/

            // Vitual keymap
            if (screenHeight - b > 300) {
                mChatView.setSelection(mDatas.size() - 1);
            }// Vitual key
            else if (mAnyrtcViews != null) {
                mAnyrtcViews.onScreenChanged();
            }
        }
    };

    private AnyRTCViews.VideoViewPeopleNumEvent mVideoViewPeopleNumEvent = new AnyRTCViews.VideoViewPeopleNumEvent() {
        @Override
        public void OnPeopleNumChange(int peopleNum) {
            if (mDebug) {
                Log.e(TAG, "OnPeopleNumChange: peopleNum " + peopleNum);
            }
            numberOfDisplay(peopleNum);
        }
    };

    /**
     * chat data onReflash
     */
    @Override
    public void onReflash() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMessagePageNum++;
                mNetWork.getMeetingMsgList(getSign(), mMeetingId, "" + mMessagePageNum, 20 + "");
                mChatView.reflashComplete();
            }
        }, 1500);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * For M2MultierEvents callback.
     * All callback is running run handle thread, so could update ui directly.
     */

    @Override
    public void OnRtcJoinMeetOK(String strAnyrtcId) {

    }

    @Override
    public void OnRtcJoinMeetFailed(String strAnyrtcId, int code, String strReason) {

    }

    @Override
    public void OnRtcLeaveMeet(int code) {
        if (mDebug)
            Log.e(TAG, "OnRtcLeaveMeet: " + code);
        if (code == 0) {
/*            mMessage.what = EventType.MSG_NOTIFICATION_MEETING_CLOSE.ordinal();
            EventBus.getDefault().post(mMessage);*/
        }
    }



/*    @Override
    public void OnRtcOpenLocalRender(VideoTrack localTrack) {
        if (mDebug)
            Log.e(TAG, "onRtcOpenLocalRender: " + localTrack);
        mVideoView.OpenLocalRender(localTrack);
    }*/

/*    @Override
    public void OnRtcRemoteAVStatus(String s, boolean b, boolean b1) {

    }*/


    @Override
    public void onRequesageMsg(ReqSndMsgEntity requestMsg) {
        super.onRequesageMsg(requestMsg);
        if (mDebug) {
            Log.e(TAG, "onRequesageMsg: " + requestMsg.toString());
        }
        int tags = requestMsg.getTags();
        final String message = requestMsg.getCont();
        final String name = requestMsg.getNname();
        String from = requestMsg.getFrom();
        if (mDebug) {
            Log.e(TAG, "onRequesageMsg: " + "tags " + tags + " message " + message + " name " + name + " from " + from);
        }
        MessageTagsDistribute(tags, message, name);
        //numberOfDisplay(requestMsg.getNmem());
    }


    private void numberOfDisplay(int mennum) {
        if (mennum > 0) {
            MCSENDTAGS_SUBSCRIBE = true;
            mTvRemind.setVisibility(View.GONE);
        } else {
            MCSENDTAGS_SUBSCRIBE = false;
            mTvRemind.setVisibility(View.VISIBLE);
        }
    }

    private void MessageTagsDistribute(int tags, String message, String name) {
        switch (tags) {
            case JMClientType.MCSENDTAGS_TALK://1
                mcsendtags_talk(message, name);
                break;
            case JMClientType.MCSENDTAGS_ENTER://2
                //mTvRemind.setVisibility(View.GONE);
                break;
            case JMClientType.MCSENDTAGS_LEAVE://3
                break;
            default:
                break;

        }
    }


    private void mcsendtags_talk(String message, String name) {
        ChatMessage to = new ChatMessage(Type.INPUT, message, name, System.currentTimeMillis() + "");
        mDatas.add(to);
        mAdapter.notifyDataSetChanged();
        mChatView.setSelection(mDatas.size() - 1);
        mMsg.setText("");
        if (mMessageShowFlag) {
            leaveMessageDealWith();
            //addAutoView(message, name);
        }
    }


    private void netWorkTypeStart(int type) {
        if (type == NetType.TYPE_NULL.ordinal()) {
            mNetErrorSweetAlertDialog.show();
        } else {
            mSign = getSign();
            Log.e(TAG, "netWorkTypeStart: mSign" + mSign);
            // getListNetWork();
        }
    }

    SweetAlertDialog.OnSweetClickListener sweetClickListener = new SweetAlertDialog.OnSweetClickListener() {
        @Override
        public void onClick(SweetAlertDialog sweetAlertDialog) {
            sweetAlertDialog.dismiss();
        }
    };


    /**
     * For EventBus callback.
     */
    private Message mMessage;

    public void onEventMainThread(Message msg) {
        switch (EventType.values()[msg.what]) {
            case MSG_MESSAGE_RECEIVE:
                break;
            case MSG_GET_MEETING_MSG_LIST_SUCCESS:
                mUiHandler.sendEmptyMessage(MESSAGE_UPDATE);
                if (mDebug)
                    Log.e(TAG, "onEventMainThread: " + "MSG_GET_MEETING_MSG_LIST_SUCCESS ");
                break;
            case MSG_GET_MEETING_MSG_LIST_FAILED:
                if (mDebug)
                    Log.e(TAG, "onEventMainThread: " + "MSG_GET_MEETING_MSG_LIST_FAILED");
                break;
            case MSG_NET_WORK_TYPE:
                if (mDebug)
                    Log.e(TAG, "MSG_NET_WORK_TYPE");
                int type = msg.getData().getInt("net_type");
                netWorkTypeStart(type);
                //msgSenderLeave();
                break;
            case MSG_NOTIFICATION_MEETINGID_EQUAL:
                if (mDebug)
                    Log.e(TAG, "MSG_NOTIFICATION_MEETINGID_EQUAL");
                Intent resultIntent = new Intent(this, MeetingActivity.class);
                startActivity(resultIntent);
                break;
            case MSG_NOTIFICATION_MEETING_CLOSE_MAIN:
                if (mDebug)
                    Log.e(TAG, "MSG_NOTIFICATION_MEETING_CLOSE_MAIN " + msg.getData().toString());
                msgSenderLeave();
                msg.what = EventType.MSG_NOTIFICATION_MEETING_CLOSE.ordinal();
                EventBus.getDefault().post(msg);
            default:
                break;
        }
    }


}
