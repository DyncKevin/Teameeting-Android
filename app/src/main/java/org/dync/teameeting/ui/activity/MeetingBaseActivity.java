/**
 * BaseAcitvity.java [V 1.0.0]
 * classes:org.dync.teammeeting.BaseAcitvity
 * Zlang Create at 2015-11-30.下午2:48:42
 */
package org.dync.teameeting.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import org.dync.teameeting.R;
import org.dync.teameeting.TeamMeetingApp;
import org.dync.teameeting.bean.ReqSndMsgEntity;
import org.dync.teameeting.chatmessage.ChatMessageClient;
import org.dync.teameeting.chatmessage.IChatMessageInteface;
import org.dync.teameeting.http.NetWork;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

/**
 * @author ZLang <br/>
 *         create at 2015-11-30 下午2:48:42
 */
public class MeetingBaseActivity extends Activity implements IChatMessageInteface {
    public String TAG = "MeetingBaseActivity";
    public boolean mDebug = TeamMeetingApp.mIsDebug;
    private RelativeLayout mainView;
    private ChatMessageClient mChatMessageClinet;
    Random random = new Random();
    public String mSign;
    NetWork mNetWork;
    private boolean isShowMessage = true;
    private List<LinearLayout> viewList;
    private int top;
    private int[] colors = new int[]{R.color.skyblue, R.color.blue_btn_bg_color, R.color.grey_txt, R.color.right_bg, R.color.grayBlue, R.color.layout_content_black, R.color.skyblue,
            R.color.vifrification, R.color.grayBlue, R.color.mediumslateblue,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetWork = new NetWork();
        mainView = (RelativeLayout) View.inflate(this, R.layout.activity_meeting, null);
        setContentView(mainView);
        viewList = new ArrayList<LinearLayout>();

        registerObserverClinet();
        //startShowMessage();
        Log.e(TAG, "onCreate: ");
    }

    private void registerObserverClinet() {
        mChatMessageClinet = TeamMeetingApp.getmChatMessageClient();
        mChatMessageClinet.registerObserver(chatMessageObserver);
    }

    ChatMessageClient.ChatMessageObserver chatMessageObserver = new ChatMessageClient.ChatMessageObserver() {
        @Override
        public void OnReqSndMsg(final ReqSndMsgEntity reqSndMsg) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onRequesageMsg(reqSndMsg);
                    }
                });
            } else {
                onRequesageMsg(reqSndMsg);
            }
        }

    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatMessageClinet.unregisterObserver(chatMessageObserver);
        EventBus.getDefault().unregister(this);
    }

    public String getSign() {
        return TeamMeetingApp.getmSelfData().getAuthorization();
    }

    public void addAutoView(String msg, String name) {
        MoveDownView();
        final LinearLayout showView = (LinearLayout) View.inflate(this, R.layout.text_view, null);
        TextView tvChatContent = (TextView) showView.findViewById(R.id.tv_chat_content);
        TextView tvSendName = (TextView) showView.findViewById(R.id.tv_send_name);
        showView.setBackgroundColor(colors[random.nextInt(10)]);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        params.topMargin = (int) getResources().getDimension(R.dimen.height_top_bar);
        showView.setLayoutParams(params);
        showView.postDelayed(new Runnable() {
            @Override
            public void run() {
                alphaAnimation(showView, viewList);
            }
        }, 1000);

        viewList.add(showView);
        mainView.addView(showView);
        tvChatContent.setText(msg);
        tvSendName.setText(name);
    }

    private void alphaAnimation(final View view,
                                final List<LinearLayout> viewList) {
        view.clearAnimation();
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                viewList.remove(view);
            }
        });
        anim.setDuration(3000).start();
    }


    private void MoveDownView() {
        int topMargin = 0;
        for (int i = 0; i < viewList.size(); i++) {
            LinearLayout linearLayout = viewList.get(i);
            topMargin = linearLayout.getHeight() + 20;
            LayoutParams layoutParams = (LayoutParams) linearLayout
                    .getLayoutParams();
            layoutParams.topMargin += topMargin;
            linearLayout.setLayoutParams(layoutParams);
        }
    }

    public void stopShowMessage() {

    }

    /**
     * @param chactView
     * @return
     */
    public int controllerMoveDistance(View chactView) {
        return chactView.getWidth() / 2;
    }

    @Override
    public void onRequesageMsg(ReqSndMsgEntity requestMsg) {

    }
}
