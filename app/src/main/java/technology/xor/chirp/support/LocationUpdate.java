package technology.xor.chirp.support;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class LocationUpdate {

    public String uid;
    public String assetName;
    public double latitude;
    public double longitude;
    public long time;
    public boolean emergency;

    public LocationUpdate() {}

    public LocationUpdate(String uid, String assetName, double latitude, double longitude, long time, boolean emergency) {
        this.uid = uid;
        this.assetName = assetName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.emergency = emergency;
    }

}
