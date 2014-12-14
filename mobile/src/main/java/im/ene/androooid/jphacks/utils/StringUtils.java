package im.ene.androooid.jphacks.utils;

import android.location.Location;
import android.location.LocationManager;

/**
 * Created by eneim on 12/13/14.
 */
public class StringUtils {

    public static final String DOCOMO_API_CLIENT_SECRET = "!I?aG]'Ifq+TSIRq#O_/";
    public static final String DOCOMO_API_CLIENT_ID = "6Qz3lsYQWMflyv1tYr0vSVzeLhi8O84XXy5LBkYzkEAf";
    public static final String DOCOMO_API_KEY = "67366a64566a396a32723134637669734273456766497a744132677142415858446a30333473624d656d41";

    // TEST_LOCATION
    // 35.712944, 139.762010

    public static final Location TEST_LOCATION;

    static {
        TEST_LOCATION = new Location(LocationManager.GPS_PROVIDER) ;
        TEST_LOCATION.setLatitude(35.712944);
        TEST_LOCATION.setLongitude(139.762010);
    }
    private StringUtils() {
        // no init
    }

    public static void saySomething(String string) {
        new TextToSpeechTask(string).execute();
    }

}
