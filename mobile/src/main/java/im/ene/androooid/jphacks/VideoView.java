package im.ene.androooid.jphacks;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Created by Cherry_Zhang on 2014-12-13.
 */
public class VideoView extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener
{
    private final String APIKEY = "AIzaSyBMP9t4pFD0xtux5nSnQBXwMRRqdEOE2CY";

    YouTubePlayerView youTubePlayerView;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.youtube_layout);

        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtubePlayerView);
        youTubePlayerView.initialize(APIKEY, this);
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
