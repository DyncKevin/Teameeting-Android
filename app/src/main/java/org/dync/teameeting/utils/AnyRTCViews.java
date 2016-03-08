package org.dync.teameeting.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import org.anyrtc.Anyrtc;
import org.anyrtc.common.AnyRTCViewEvents;
import org.anyrtc.util.AppRTCUtils;
import org.anyrtc.view.PercentFrameLayout;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Eric on 2016/3/4.
 */
public class AnyRTCViews implements View.OnTouchListener, AnyRTCViewEvents {
    private static final int SUB_X = 2;
    private static final int SUB_Y = 72;
    private static final int SUB_WIDTH = 18;
    private static final int SUB_HEIGHT = 16;

    public interface VideoViewEvent {
        void OnScreenSwitch(String strBeforeFullScrnId, String strNowFullScrnId);
    }

    protected static class VideoView {
        public String strPeerId;
        public int index;
        public int x;
        public int y;
        public int w;
        public int h;
        public PercentFrameLayout mLayout = null;
        public SurfaceViewRenderer mView = null;
        public VideoTrack mVideoTrack = null;
        public VideoRenderer mRenderer = null;

        public VideoView(String strPeerId, Context ctx, int index, int x, int y, int w, int h) {
            this.strPeerId = strPeerId;
            this.index = index;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

            mLayout = new PercentFrameLayout(ctx);
            mLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        }

        public Boolean Fullscreen() {
            return w == 100 || h == 100;
        }

        public Boolean Hited(int px, int py) {
            if (!Fullscreen()) {
                int left = x * Anyrtc.gScrnWidth / 100;
                int top = y * Anyrtc.gScrnHeight / 100;
                int right = (x + w) * Anyrtc.gScrnWidth / 100;
                int bottom = (y + h) * Anyrtc.gScrnHeight / 100;
                if ((px >= left && px <= right) && (py >= top && px <= bottom)) {
                    return true;
                }
            }
            return false;
        }

        private void updateVideoTrack(VideoTrack videoTrack) {
            AppRTCUtils.assertIsTrue(videoTrack != null);
            if (mVideoTrack == null) {
                mVideoTrack = videoTrack;
                mRenderer = new VideoRenderer(mView);
                mVideoTrack.addRenderer(mRenderer);
            }
        }

        private boolean voiceFalg;
    }

    private EglBase mRootEglBase;
    private RelativeLayout mVideoView;
    private VideoView mLocalRender;
    private HashMap<String, VideoView> mRemoteRenders;

    public AnyRTCViews(RelativeLayout videoView) {
        AppRTCUtils.assertIsTrue(videoView != null);
        mVideoView = videoView;
        mVideoView.setOnTouchListener(this);
        mRootEglBase = EglBase.create();
        mRemoteRenders = new HashMap<>();
        mLocalRender = new VideoView("localRender", mVideoView.getContext(), 0, 0, 0, 100, 100);
    }

    public VideoTrack LocalVideoTrack() {
        return mLocalRender.mVideoTrack;
    }

    private void SwitchIndex1ToFullscreen(VideoView fullscrnView) {
        AppRTCUtils.assertIsTrue(fullscrnView != null);
        VideoView view1 = null;
        if (mLocalRender.index == 1) {
            view1 = mLocalRender;
        } else {
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                if (render.index == 1) {
                    view1 = render;
                    break;
                }
            }
        }
        SwitchViewPosition(view1, fullscrnView);
    }

    private VideoView GetFullScreen() {
        if (mLocalRender.Fullscreen())
            return mLocalRender;
        Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, VideoView> entry = iter.next();
            //* String peerId = entry.getKey();
            VideoView render = entry.getValue();
            if (render.Fullscreen())
                return render;
        }
        return null;
    }

    private void SwitchViewPosition(VideoView view1, VideoView view2) {
        AppRTCUtils.assertIsTrue(view1 != null && view2 != null);
        int index, x, y, w, h;
        index = view1.index;
        x = view1.x;
        y = view1.y;
        w = view1.w;
        h = view1.h;
        view1.index = view2.index;
        view1.x = view2.x;
        view1.y = view2.y;
        view1.w = view2.w;
        view1.h = view2.h;

        view2.index = index;
        view2.x = x;
        view2.y = y;
        view2.w = w;
        view2.h = h;

        VideoTrack videoTrack1 = view1.mVideoTrack;
        VideoTrack videoTrack2 = view2.mVideoTrack;
        videoTrack1.removeRenderer(view1.mRenderer);
        videoTrack2.removeRenderer(view2.mRenderer);
        view1.mVideoTrack = null;
        view2.mVideoTrack = null;
        view1.updateVideoTrack(videoTrack2);
        view2.updateVideoTrack(videoTrack1);

        view1.mView.requestLayout();
        view2.mView.requestLayout();
    }

    private void SwitchViewToFullscreen(VideoView view1, VideoView fullscrnView) {
        AppRTCUtils.assertIsTrue(view1 != null && fullscrnView != null);
        int index, x, y, w, h;
        index = view1.index;
        x = view1.x;
        y = view1.y;
        w = view1.w;
        h = view1.h;
        view1.index = fullscrnView.index;
        view1.x = fullscrnView.x;
        view1.y = fullscrnView.y;
        view1.w = fullscrnView.w;
        view1.h = fullscrnView.h;

        fullscrnView.index = index;
        fullscrnView.x = x;
        fullscrnView.y = y;
        fullscrnView.w = w;
        fullscrnView.h = h;

        VideoTrack videoTrack1 = view1.mVideoTrack;
        VideoTrack videoTrack2 = fullscrnView.mVideoTrack;
        videoTrack1.removeRenderer(view1.mRenderer);
        videoTrack2.removeRenderer(fullscrnView.mRenderer);
        view1.mVideoTrack = null;
        fullscrnView.mVideoTrack = null;
        view1.updateVideoTrack(videoTrack2);
        fullscrnView.updateVideoTrack(videoTrack1);

        view1.mView.requestLayout();
        fullscrnView.mView.requestLayout();
    }

    public void BubbleSortSubView(VideoView view) {
        if (view.index + 1 == mLocalRender.index) {
            SwitchViewPosition(mLocalRender, view);
        } else {
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                if (view.index + 1 == render.index) {
                    SwitchViewPosition(render, view);
                    break;
                }
            }
        }
        if (view.index < mRemoteRenders.size()) {
            BubbleSortSubView(view);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int startX = (int) event.getX();
            int startY = (int) event.getY();
            if (mLocalRender.Hited(startX, startY)) {
                return true;
            } else {
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, VideoView> entry = iter.next();
                    String peerId = entry.getKey();
                    VideoView render = entry.getValue();
                    if (render.Hited(startX, startY)) {
                        return true;
                    }
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            int startX = (int) event.getX();
            int startY = (int) event.getY();
            if (mLocalRender.Hited(startX, startY)) {
                SwitchViewToFullscreen(mLocalRender, GetFullScreen());
                return true;
            } else {
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, VideoView> entry = iter.next();
                    String peerId = entry.getKey();
                    VideoView render = entry.getValue();
                    if (render.Hited(startX, startY)) {
                        SwitchViewToFullscreen(render, GetFullScreen());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Implements for AnyRTCViewEvents.
     */
    @Override
    public EglBase GetEglBase() {
        return mRootEglBase;
    }

    @Override
    public void OnRtcOpenRemoteRender(String peerId, VideoTrack remoteTrack) {
        VideoView remoteRender = mRemoteRenders.get(peerId);
        if (remoteRender == null) {
            int size = mRemoteRenders.size() + 1;
            remoteRender = new VideoView(peerId, mVideoView.getContext(), size, (100 - size * (SUB_WIDTH + SUB_X)), SUB_Y, SUB_WIDTH, SUB_HEIGHT);

            remoteRender.mView = new SurfaceViewRenderer(mVideoView.getContext());
            remoteRender.mView.init(mRootEglBase.getEglBaseContext(), null);
            remoteRender.mView.setZOrderMediaOverlay(true);
            remoteRender.mView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            remoteRender.mLayout.addView(remoteRender.mView);
            mVideoView.addView(remoteRender.mLayout);

            remoteRender.mLayout.setPosition(
                    remoteRender.x, remoteRender.y, remoteRender.w, remoteRender.h);
            remoteRender.mView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            remoteRender.mRenderer = new VideoRenderer(remoteRender.mView);

            remoteRender.mVideoTrack = remoteTrack;
            remoteRender.mVideoTrack.addRenderer(remoteRender.mRenderer);
            mRemoteRenders.put(peerId, remoteRender);
            if (mRemoteRenders.size() == 1) {
                SwitchViewToFullscreen(remoteRender, mLocalRender);
            }
        }
    }

    @Override
    public void OnRtcRemoveRemoteRender(String peerId) {
        VideoView remoteRender = mRemoteRenders.get(peerId);
        if (remoteRender != null) {
            if (remoteRender.Fullscreen()) {
                SwitchIndex1ToFullscreen(remoteRender);
            }
            if (remoteRender.index < mRemoteRenders.size()) {
                BubbleSortSubView(remoteRender);
            }
            if (remoteRender.mVideoTrack != null) {
                remoteRender.mVideoTrack.removeRenderer(remoteRender.mRenderer);
                remoteRender.mVideoTrack = null;
                remoteRender.mView.release();
                remoteRender.mView = null;
                remoteRender.mRenderer = null;
            }
            mVideoView.removeView(remoteRender.mLayout);
            mRemoteRenders.remove(peerId);
        }
    }

    @Override
    public void OnRtcOpenLocalRender(VideoTrack localTrack) {
        mLocalRender.mVideoTrack = localTrack;
        if (mLocalRender.mView == null) {
            mLocalRender.mView = new SurfaceViewRenderer(mVideoView.getContext());
            mLocalRender.mView.init(mRootEglBase.getEglBaseContext(), null);
            mLocalRender.mView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            mLocalRender.mLayout.addView(mLocalRender.mView);
            mVideoView.addView(mLocalRender.mLayout);

            mLocalRender.mLayout.setPosition(
                    mLocalRender.x, mLocalRender.y, mLocalRender.w, mLocalRender.h);
            mLocalRender.mView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            mLocalRender.mRenderer = new VideoRenderer(mLocalRender.mView);
        }
        mLocalRender.mVideoTrack.addRenderer(mLocalRender.mRenderer);
    }

    @Override
    public void OnRtcRemoveLocalRender() {
        if (mLocalRender.mVideoTrack != null) {
            mLocalRender.mVideoTrack.removeRenderer(mLocalRender.mRenderer);
            mLocalRender.mVideoTrack = null;
            mLocalRender.mView.release();
            mLocalRender.mView = null;
            mLocalRender.mRenderer = null;

            mVideoView.removeView(mLocalRender.mLayout);
        }
    }

    @Override
    public void OnRtcRemoteAVStatus(String peerId, boolean audioEnable, boolean videoEnable) {

    }
}
