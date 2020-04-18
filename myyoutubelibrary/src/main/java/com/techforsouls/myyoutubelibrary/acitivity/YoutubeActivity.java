package com.techforsouls.myyoutubelibrary.acitivity;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.techforsouls.myyoutubelibrary.R;
import com.techforsouls.myyoutubelibrary.listener.YouTubeEventListener;
import com.techforsouls.myyoutubelibrary.models.PlayerStateList;
import com.techforsouls.myyoutubelibrary.util.$Precondition$Check;
import com.techforsouls.myyoutubelibrary.util.ServiceUtil;
import com.techforsouls.myyoutubelibrary.webview.YouTubePlayerWebView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class YoutubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    /*
     * Pass video id as extras
     */
    public static final String ARG_VIDEO_ID = "videoId";

    /**
     * Pass api key as extras
     */
    public static final String ARG_API_KEY = "apiKey";

    /**
     * Pass web-url as extras
     */
    public static final String ARG_WEB_URL = "webUrl";

    private static final String TAG = "YoutubeActivity";

    @PlayerStateList.PlayerState
    private String playerState = PlayerStateList.NONE;

    @Nullable
    private YouTubePlayer youTubePlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullscreen();
        setContentView(R.layout.activity_youtube);
        YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player);

        String videoId = getVideoId();
        String apiKey = getApiKey();

        /*
         * In case videoId or apiKey is null, throw IllegalStateException as apiKey and videoId is mandatory to run
         * youtube activity.
         */
        $Precondition$Check.checkArgument(!TextUtils.isEmpty(videoId), " videoId cannot be null");
        $Precondition$Check.checkArgument(!TextUtils.isEmpty(apiKey), " apiKey cannot be null");

        /*
         * In case of YouTube Service not available, fallback to WebView implementation.
         */
       /* if (ServiceUtil.isYouTubeServiceAvailable(this)) {
            youTubePlayerView.initialize(apiKey, this);
        } else {
            String webViewUrl = getWebUrl();
            if (!TextUtils.isEmpty(webViewUrl)) {
                youTubePlayerView.setVisibility(GONE);
                handleWebViewPlayer(videoId, webViewUrl);
            } else {
                Log.d(TAG, "Web Url is Null");
                finish();
            }
        }*/
        String webViewUrl = getWebUrl();
        if (!TextUtils.isEmpty(webViewUrl)) {
            youTubePlayerView.setVisibility(GONE);
            handleWebViewPlayer(videoId, webViewUrl);
        } else {
            Log.d(TAG, "Web Url is Null");
            finish();
        }
    }

    private void setFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Nullable
    private String getVideoId() {
        Bundle extras = null != getIntent() ? getIntent().getExtras() : null;
        return null != extras ? extras.getString(ARG_VIDEO_ID) : null;
    }

    @Nullable
    private String getApiKey() {
        Bundle extras = null != getIntent() ? getIntent().getExtras() : null;
        return null != extras ? extras.getString(ARG_API_KEY) : null;
    }

    @Nullable
    private String getWebUrl() {
        Bundle extras = null != getIntent() ? getIntent().getExtras() : null;
        return null != extras ? extras.getString(ARG_WEB_URL) : null;
    }

    private void handleWebViewPlayer(String videoId, String webViewUrl) {
        //initialize youtube player webview
        YouTubePlayerWebView youTubePlayerWebView = new YouTubePlayerWebView(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        youTubePlayerWebView.setLayoutParams(layoutParams);

        //initialize progressbar and attach it to the view.
        ProgressBar progressBar = initializeProgressBar();
        handleProgressBar(progressBar, true);

        FrameLayout.LayoutParams progressBarParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        progressBarParams.gravity = Gravity.CENTER;
        youTubePlayerWebView.setBackgroundColor(getResources().getColor(R.color.black));
        youTubePlayerWebView.canGoBack();
        youTubePlayerWebView.canGoForward();
        youTubePlayerWebView.canGoBackOrForward(10);
        youTubePlayerWebView.initialize(webViewUrl);

        YouTubeEventListener youTubeEventListener = getYoutubeEventListener(youTubePlayerWebView, progressBar, videoId);
        youTubePlayerWebView.setYouTubeListener(youTubeEventListener);

        addContentView(youTubePlayerWebView, layoutParams);
        addContentView(progressBar, progressBarParams);


    }

    private ProgressBar initializeProgressBar() {
        ProgressBar progressBar = new ProgressBar(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int color;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                color = getResources().getColor(R.color.default_progress_bar_color, null);
            } else {
                color = getResources().getColor(R.color.default_progress_bar_color);
            }
            progressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }

        return progressBar;
    }

    private void handleProgressBar(@NonNull ProgressBar progressBar, boolean show) {
        progressBar.setVisibility(show ? VISIBLE : GONE);
    }

    @Override
    public void onInitializationSuccess(final YouTubePlayer.Provider provider, YouTubePlayer player, boolean restored) {
        youTubePlayer = player;
        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
        youTubePlayer.setShowFullscreenButton(true);
        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);

        youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
                if (youTubePlayer != null && !PlayerStateList.PLAYING.equals(playerState)) {
                    playerState = PlayerStateList.PLAYING;

                }
            }

            @Override
            public void onPaused() {
                handleOnPauseEvent();
            }

            @Override
            public void onStopped() {
                handleStopEvent();
            }

            @Override
            public void onBuffering(boolean b) {

            }

            @Override
            public void onSeekTo(int i) {

            }
        });

        youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
                //intentionally left blank
            }

            @Override
            public void onLoaded(String s) {
                //intentionally left blank
            }

            @Override
            public void onAdStarted() {
                //intentionally left blank
            }

            @Override
            public void onVideoStarted() {
                //intentionally left blank
            }

            @Override
            public void onVideoEnded() {
                handleStopEvent();
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
                //intentionally left blank
            }
        });
        youTubePlayer.setPlaylistEventListener(new YouTubePlayer.PlaylistEventListener() {
            @Override
            public void onPrevious() {

            }

            @Override
            public void onNext() {

            }

            @Override
            public void onPlaylistEnded() {

            }
        });

        player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
            @Override
            public void onFullscreen(boolean b) {
                handleStopEvent();
            }
        });

        if (!restored) {
            youTubePlayer.loadVideo(getVideoId());
        }
    }

    private void handleOnPauseEvent() {
        if (youTubePlayer != null && (PlayerStateList.PLAYING.equals(playerState))
                || PlayerStateList.BUFFERING.equals(playerState)) {
            playerState = PlayerStateList.PAUSED;
            Log.e("Player pause ",playerState+" "+youTubePlayer.getDurationMillis());
        }
    }

    private void handleStopEvent() {
        if (youTubePlayer != null && (PlayerStateList.PLAYING.equals(playerState))
                || PlayerStateList.BUFFERING.equals(playerState) || PlayerStateList.PAUSED.equals(playerState)) {
            playerState = PlayerStateList.STOPPED;
            Log.e("Player stop ",playerState+" "+youTubePlayer.getDurationMillis());
        }
    }

    private YouTubeEventListener getYoutubeEventListener(@NonNull final YouTubePlayerWebView youTubePlayerWebView,
                                                         @NonNull final ProgressBar progressBar,
                                                         @NonNull final String videoId) {
        return new YouTubeEventListener() {
            @Override
            @MainThread
            public void onReady() {
                youTubePlayerWebView.loadVideo(videoId);
                handleProgressBar(progressBar, false);
            }

            @Override
            public void onCued() {
                //intentionally left blank
            }

            @Override
            @MainThread
            public void onPlay(int currentTime) {
                handleProgressBar(progressBar, false);
            }

            @Override
            @MainThread
            public void onPause(int currentTime) {
                //intentionally left blank
            }

            @Override
            @MainThread
            public void onStop(int currentTime, int totalDuration) {
                //intentionally left blank
            }

            @Override
            @MainThread
            public void onBuffering(int currentTime, boolean isBuffering) {
                //intentionally left blank
            }

            @Override
            @MainThread
            public void onSeekTo(int currentTime, int newPositionMillis) {
                //intentionally left blank
            }

            @Override
            @MainThread
            public void onInitializationFailure(String error) {
                //intentionally left blank
            }

            @Override
            @MainThread
            public void onNativeNotSupported() {
                //intentionally left blank
            }
        };
    }


    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        this.youTubePlayer = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        handleStopEvent();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (youTubePlayer != null) {
            youTubePlayer.release();
        }
    }
}
