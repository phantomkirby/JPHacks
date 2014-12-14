package im.ene.androooid.jphacks;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Activity is called if user only wants video on phone
 */
public class VideoPlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener
{

    private final String APIKEY = "AIzaSyBMP9t4pFD0xtux5nSnQBXwMRRqdEOE2CY";

    private YouTubePlayerView youTubePlayerView;

    private ImageView mImageHeart;

    private TextView mHeartBeat;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.youtube_layout);

        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtubePlayerView);
        youTubePlayerView.initialize(APIKEY, this);

        mImageHeart = (ImageView) findViewById(R.id.image_heart);
        Animation heartFlashAnimation = AnimationUtils.loadAnimation(this, R.anim.heart_flash);
        mImageHeart.startAnimation(heartFlashAnimation);

        mHeartBeat = (TextView) findViewById(R.id.text_heart_beat);

    }


    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored)
    {
        if (!wasRestored) {
            youTubePlayer.cueVideo("PqJNc9KVIZE");
        }

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult)
    {

    }
}
