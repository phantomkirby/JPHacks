package im.ene.androooid.jphacks;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;
import java.util.Random;

import im.ene.androooid.jphacks.callback.WearSensorCallback;
import im.ene.androooid.jphacks.utils.WearSensorUtil;

public class VideoViewToTV extends ActionBarActivity implements WearSensorCallback
{
    private static String videoToLoad;
    private static final String[] hardVideos =
            {"http://www.googledrive.com/host/0B5oyJCoT20suTzhqdkY4V29Hczg/bootyshaking.mp4",
             "http://www.googledrive.com/host/0B5oyJCoT20suTzhqdkY4V29Hczg/victoriasecret.mp4",
             "http://www.googledrive.com/host/0B5oyJCoT20suTzhqdkY4V29Hczg/video1.mp4"};
    private static final String easyVideo = "http://www.googledrive.com/host/0B5oyJCoT20suTzhqdkY4V29Hczg/bootyshaking.mp4";
    Random r;
    final int Low = 0;
    final int High = 2;

    private WearSensorUtil mWearSensorUtil;

    private Button mButton;
    private TextView tv_heartRate;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private Cast.Listener mCastClientListener;
    private boolean mWaitingForReconnect = false;
    private boolean mApplicationStarted = false;
    private boolean mVideoIsLoaded;
    private boolean mIsPlaying;

    private ImageView mImageViewHeart;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_video_view_to_tv );

//        tv_heartRate = (TextView) findViewById(R.id.heartRate);

        mImageViewHeart = (ImageView) findViewById(R.id.image_heart);
        Animation heartFlashAnimation = AnimationUtils.loadAnimation(this, R.anim.heart_flash);
        mImageViewHeart.startAnimation(heartFlashAnimation);

        mButton = (Button) findViewById( R.id.button );
        mButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if( !mVideoIsLoaded )
                    startVideo();
                else
                    controlVideo();
            }
        });

        r = new Random();

        mWearSensorUtil = new WearSensorUtil(this);

        initMediaRouter();
    }

    private void initMediaRouter() {
        // Configure Cast device discovery
        mMediaRouter = MediaRouter.getInstance( getApplicationContext() );
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory( CastMediaControlIntent.categoryForCast(getString(R.string.app_id)) )
                .build();
        mMediaRouterCallback = new MediaRouterCallback();
    }

    private void initCastClientListener() {
        mCastClientListener = new Cast.Listener() {
            @Override
            public void onApplicationStatusChanged() {
            }

            @Override
            public void onVolumeChanged() {
            }

            @Override
            public void onApplicationDisconnected( int statusCode ) {
                teardown();
            }
        };
    }

    private void initRemoteMediaPlayer() {
        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener( new RemoteMediaPlayer.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
                mIsPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
            }
        });

        mRemoteMediaPlayer.setOnMetadataUpdatedListener( new RemoteMediaPlayer.OnMetadataUpdatedListener() {
            @Override
            public void onMetadataUpdated() {
            }
        });
    }

    private void controlVideo() {
        if( mRemoteMediaPlayer == null || !mVideoIsLoaded )
            return;

        if( mIsPlaying ) {
            mRemoteMediaPlayer.pause( mApiClient );
            mButton.setText( getString( R.string.resume_video ) );
        } else {
            mRemoteMediaPlayer.play( mApiClient );
            mButton.setText( getString( R.string.pause_video ) );
        }
    }

    private void startVideo() {
        MediaMetadata mediaMetadata = new MediaMetadata( MediaMetadata.MEDIA_TYPE_MOVIE );
        mediaMetadata.putString( MediaMetadata.KEY_TITLE, getString( R.string.video_title ) );

        if (videoToLoad == null)
        {
            videoToLoad = "http://www.googledrive.com/host/0B5oyJCoT20suTzhqdkY4V29Hczg/bootyshaking.mp4";
        }

        Toast.makeText(this, "initializing video " + videoToLoad, Toast.LENGTH_SHORT).show();

        //getString(R.string.video_url)
        MediaInfo mediaInfo = new MediaInfo.Builder(videoToLoad)
                .setContentType( getString( R.string.content_type_mp4 ) )
                .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                .setMetadata( mediaMetadata )
                .build();
        try {
            mRemoteMediaPlayer.load( mApiClient, mediaInfo, true )
                    .setResultCallback( new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult( RemoteMediaPlayer.MediaChannelResult mediaChannelResult ) {
                            if( mediaChannelResult.getStatus().isSuccess() ) {
                                mVideoIsLoaded = true;
                                mButton.setText( getString( R.string.pause_video ) );
                            }
                        }
                    } );
        } catch( Exception e ) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWearSensorUtil.setCallback(this);
        mWearSensorUtil.resume();
        // Start media router discovery
        mMediaRouter.addCallback( mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN );
    }

    @Override
    protected void onPause() {
        mWearSensorUtil.removeCallback();
        if ( isFinishing() ) {
            // End media router discovery
            mMediaRouter.removeCallback( mMediaRouterCallback );
        }
        super.onPause();
    }

    private class MediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            initCastClientListener();
            initRemoteMediaPlayer();

            mSelectedDevice = CastDevice.getFromBundle( info.getExtras() );

            launchReceiver();
        }

        @Override
        public void onRouteUnselected( MediaRouter router, MediaRouter.RouteInfo info ) {
            teardown();
            mSelectedDevice = null;
            mButton.setText( getString( R.string.play_video ) );
            mVideoIsLoaded = false;
        }
    }

    private void launchReceiver() {
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder( mSelectedDevice, mCastClientListener );

        ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks();
        ConnectionFailedListener mConnectionFailedListener = new ConnectionFailedListener();
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Cast.API, apiOptionsBuilder.build() )
                .addConnectionCallbacks( mConnectionCallbacks )
                .addOnConnectionFailedListener( mConnectionFailedListener )
                .build();

        mApiClient.connect();
    }

    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected( Bundle hint ) {
            if( mWaitingForReconnect ) {
                mWaitingForReconnect = false;
                reconnectChannels( hint );
            } else {
                try {
                    Cast.CastApi.launchApplication( mApiClient, getString( R.string.app_id ), false )
                            .setResultCallback( new ResultCallback<Cast.ApplicationConnectionResult>() {
                                                    @Override
                                                    public void onResult(Cast.ApplicationConnectionResult applicationConnectionResult) {
                                                        Status status = applicationConnectionResult.getStatus();
                                                        if( status.isSuccess() ) {
                                                            //Values that can be useful for storing/logic
                                                            ApplicationMetadata applicationMetadata = applicationConnectionResult.getApplicationMetadata();
                                                            String sessionId = applicationConnectionResult.getSessionId();
                                                            String applicationStatus = applicationConnectionResult.getApplicationStatus();
                                                            boolean wasLaunched = applicationConnectionResult.getWasLaunched();

                                                            mApplicationStarted = true;
                                                            reconnectChannels( null );
                                                        }
                                                    }
                                                }
                            );
                } catch ( Exception e ) {

                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            mWaitingForReconnect = true;
        }
    }

    private void reconnectChannels( Bundle hint ) {
        if( ( hint != null ) && hint.getBoolean( Cast.EXTRA_APP_NO_LONGER_RUNNING ) ) {
            //Log.e( TAG, "App is no longer running" );
            teardown();
        } else {
            try {
                Cast.CastApi.setMessageReceivedCallbacks( mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer );
            } catch( IOException e ) {
                //Log.e( TAG, "Exception while creating media channel ", e );
            } catch( NullPointerException e ) {
                //Log.e( TAG, "Something wasn't reinitialized for reconnectChannels" );
            }
        }
    }

    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed( ConnectionResult connectionResult ) {
            teardown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
//        super.onCreateOptionsMenu( menu );
        getMenuInflater().inflate( R.menu.menu_video_view_to_tv, menu );
        MenuItem mediaRouteMenuItem = menu.findItem( R.id.media_route_menu_item );
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector( mMediaRouteSelector );
        return true;
    }


    private void teardown() {
        if( mApiClient != null ) {
            if( mApplicationStarted ) {
                try {
                    Cast.CastApi.stopApplication( mApiClient );
                    if( mRemoteMediaPlayer != null ) {
                        Cast.CastApi.removeMessageReceivedCallbacks( mApiClient, mRemoteMediaPlayer.getNamespace() );
                        mRemoteMediaPlayer = null;
                    }
                } catch( IOException e ) {
                    //Log.e( TAG, "Exception while removing application " + e );
                }
                mApplicationStarted = false;
            }
            if( mApiClient.isConnected() )
                mApiClient.disconnect();
            mApiClient = null;
        }
        mSelectedDevice = null;
        mVideoIsLoaded = false;
    }

    @Override
    protected void onStop() {
        mWearSensorUtil.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        teardown();
        mWearSensorUtil.destroy();
        super.onDestroy();
    }

    @Override
    public void onHeartRateChanged(float heartRate) {
        Log.d("", "heart rate:" + heartRate);
        tv_heartRate.setText(Float.toString(heartRate));
        if (heartRate > 100) //TODO: change this number to be more suitable
        {

            final Dialog dialog = new Dialog(this);
            dialog.setTitle("We Noticed You have Heartrate > 100...");
            dialog.setContentView(R.layout.load_new_video_dialog);

            TextView textView = (TextView) dialog.findViewById(R.id.show_easy_or_hard_video_textView);
            textView.setText("Want to show easier video?");

            Button confirmButton = (Button) dialog.findViewById(R.id.confirm);
            confirmButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // use easyvideo
                    videoToLoad = easyVideo;
                    dialog.dismiss();
                    recreate();
                }
            });
            Button declineButton = (Button) dialog.findViewById(R.id.decline);
            declineButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        else if (heartRate < 80) //TODO: change this number to be more suitable
        {

            final Dialog dialog = new Dialog(this);
            dialog.setTitle("We Noticed You have Heartrate < 80...");
            dialog.setContentView(R.layout.load_new_video_dialog);

            TextView textView = (TextView) dialog.findViewById(R.id.show_easy_or_hard_video_textView);
            textView.setText("Want to show harder video?");

            Button confirmButton = (Button) dialog.findViewById(R.id.confirm);
            confirmButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //show one of the harder videos
                    int randomIndex = r.nextInt(High-Low) + Low;
                    videoToLoad = hardVideos[randomIndex];
                    dialog.dismiss();
                    recreate();
                }
            });
            Button declineButton = (Button) dialog.findViewById(R.id.decline);
            declineButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        // else don't do anything
    }

    @Override
    public void onStepDetected(int sumOfSteps) {
        Log.d("", "steps:"+sumOfSteps);
    }

}
