package technology.xor.chirp.support;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DeviceUpdate {

    public String uid;
    public String assetName;

    public DeviceUpdate() {}

    public DeviceUpdate(String uid, String assetName) {
        this.uid = uid;
        this.assetName = assetName;
    }
}
