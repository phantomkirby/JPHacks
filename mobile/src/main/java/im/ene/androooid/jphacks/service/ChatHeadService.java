package im.ene.androooid.jphacks.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.premnirmal.Magnet.IconCallback;
import com.premnirmal.Magnet.Magnet;

import im.ene.androooid.jphacks.R;

/**
 * Created by Cherry_Zhang on 2014-12-13.
 */
public class ChatHeadService extends Service implements IconCallback
{

    private static final String TAG = "Magnet";
    private Magnet mMagnet;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO change this iconView into Android-ify GIF!!!
        ImageView iconView = new ImageView(this);
        iconView.setImageResource(R.drawable.ic_launcher);

        mMagnet = new Magnet.Builder(this)
                .setIconView(iconView)
                .setIconCallback(this)
                .setRemoveIconResId(R.drawable.trash)
                .setRemoveIconShadow(R.drawable.bottom_shadow)
                .setShouldFlingAway(true)
                .setShouldStickToWall(true)
                .setRemoveIconShouldBeResponsive(true)
                .build();
        mMagnet.show();

    }


    @Override
    public void onFlingAway() {
        Log.i(TAG, "onFlingAway");
    }

    @Override
    public void onMove(float x, float y) {
        Log.i(TAG, "onMove(" + x + "," + y + ")");
    }

    @Override
    public void onIconClick(View icon, float iconXPose, float iconYPose) {
        Log.i(TAG, "onIconClick(..)");
        mMagnet.destroy();
    }

    @Override
    public void onIconDestroyed() {
        Log.i(TAG, "onIconDestroyed()");
    }
}