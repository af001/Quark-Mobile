package technology.xor.chirp.mains;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;

import technology.xor.barcodereader.BarcodeCaptureActivity;
import technology.xor.chirp.AppController;
import technology.xor.chirp.R;
import technology.xor.chirp.dialogs.IntervalDialog;
import technology.xor.chirp.support.AppData;
import technology.xor.chirp.support.DeviceUpdate;
import technology.xor.chirp.support.LocationProvider;
import technology.xor.chirp.support.LocationUpdateService;

public class BeaconActivity extends AppCompatActivity
        implements LocationProvider.LocationCallback {

    private LocationProvider mLocationProvider;
    private Button beacon;
    private TextView position;
    private TextView beaconStatus;
    private TextView gpsError;
    private TextView beaconMsg;
    private SharedPreferences sharedPref;
    private String name;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals("")) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        mLocationProvider = new LocationProvider(this, this);

        beacon = (Button) findViewById(R.id.btn_tracking);
        position = (TextView) findViewById(R.id.tv_position);
        beaconStatus = (TextView) findViewById(R.id.tv_beacon_interval);
        gpsError = (TextView) findViewById(R.id.tv_gps_error);
        beaconMsg = (TextView) findViewById(R.id.tv_beacon_sent);
        beaconMsg.setVisibility(View.GONE);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int intervalValue = sharedPref.getInt("interval", 103);
        long timeValue = sharedPref.getLong("lastReport", 0);
        name = sharedPref.getString("assetName", null);

        if (sharedPref.getInt("startId", 0) == 0) {
            beacon.setText("Off");
        } else {
            beacon.setText("On");
        }

        if (timeValue > 0) {
            beaconMsg.setVisibility(View.VISIBLE);
            beaconMsg.setText("Beacon sent at " + parseTime(timeValue));
        }

        CheckAccount();

        SetDisplayInterval(intervalValue);

        beacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateBeaconStatus(true);
            }
        });

        // Use instance field for listener
        // It will not be gc'd as long as this instance is kept referenced
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                System.out.println("It changed");
                if (key.equals("interval")) {
                    int var = prefs.getInt(key, 101);
                    SetDisplayInterval(var);
                }
            }
        };

        sharedPref.registerOnSharedPreferenceChangeListener(listener);

    }

    private void CheckAccount() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("devices");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        rootRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (user != null) {
                    if (dataSnapshot.hasChild(user.getUid() + "/" + name)) {
                        System.out.println("Device exists");
                        beacon.setEnabled(true);
                    } else {
                        System.out.println("Device does not exist");
                        beacon.setEnabled(false);
                        UpdateBeaconStatus(false);
                        UpdateAssetName(null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void UpdateBeaconStatus(boolean isActive) {
        int startId = sharedPref.getInt("startId", 0);

        if (isActive) {
            if (startId == 0) {
                beacon.setText("On");
                startService(new Intent(getApplicationContext(), LocationUpdateService.class));
            } else {
                beacon.setText("Off");
                stopService(new Intent(getApplicationContext(), LocationUpdateService.class));
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("startId", 0);
                editor.apply();
            }
        } else {
            beacon.setText("Off");
            if (startId != 0) {
                stopService(new Intent(getApplicationContext(), LocationUpdateService.class));
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("startId", 0);
                editor.apply();
            }
        }
    }

    public String parseTime(long milliseconds) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(milliseconds);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppData.MY_PERMISSIONS_REQUEST_READ_PHONE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Uri packageURI = Uri.parse("package:" + AppController.class.getPackage().getName());
                    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                    startActivity(uninstallIntent);
                }
                return;
            default:
                Log.e("MapsActivity", "Permission error onRequestPermissionsResult");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationProvider.connect();
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        isLocationEnabled();
        name = sharedPref.getString("assetName", null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
        mLocationProvider.disconnect();
        isLocationEnabled();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // HANDLE ITEM SELECTION
        switch (item.getItemId()) {
            case R.id.system_logout:
                FirebaseAuth.getInstance().signOut();
                Intent logoutIntent = new Intent(this, AppController.class);
                startActivity(logoutIntent);
                mLocationProvider.disconnect();
                finish();
                return true;
            case R.id.system_exit:
                mLocationProvider.disconnect();
                finish();
                return true;
            case R.id.system_interval:
                IntervalDialog intervalDialog = new IntervalDialog();
                intervalDialog.AlertUser(BeaconActivity.this, BeaconActivity.this, beaconStatus);
                return true;
            case R.id.system_scan:
                Intent intent = new Intent(BeaconActivity.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, AppData.AUTO_FOCUS);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, AppData.USE_FLASH);
                startActivityForResult(intent, AppData.RC_BARCODE_CAPTURE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppData.RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String assetName = barcode.displayValue;

                    UpdateAssetName(assetName);

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    DeviceUpdate deviceUpdate = new DeviceUpdate(user.getUid(), assetName);

                    mDatabase.child("devices").child(user.getUid()).child(assetName).setValue(deviceUpdate);
                    CheckAccount();

                } else {
                    Log.d("Barcode", "Failed to capture barcode.");
                }
            } else {
                Log.e("Barcode", "Error scanning barcode");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void UpdateAssetName(String asset) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("assetName", asset);
        editor.apply();
    }

    @Override
    public void handleNewLocation(Location location) {
        String positionText = "position\n" + String.valueOf(String.format("%.5f", location.getLatitude()) +
                "," + String.valueOf(String.format("%.5f",location.getLongitude())));

        position.setText(positionText);

        if (location.getAccuracy() > 100) {
            String accuracyText = "gps error\n>100m";
            gpsError.setText(accuracyText);
        } else {
            String accuracyText = "gps error\n" + String.valueOf(Math.round(location.getAccuracy()) + "m");
            gpsError.setText(accuracyText);
        }

        if (location.getAccuracy() <= 10) {
            gpsError.setBackgroundColor(ContextCompat.getColor(this, R.color.good_gps));
        } else if (location.getAccuracy() > 10 && location.getAccuracy() <= 50) {
            gpsError.setBackgroundColor(ContextCompat.getColor(this, R.color.medium_gps));
        } else if (location.getAccuracy() > 50) {
            gpsError.setBackgroundColor(ContextCompat.getColor(this, R.color.bad_gps));
        }
    }

    public void SetDisplayInterval(int interval) {
        switch (interval) {
            case 100:
                beaconStatus.setText("beacon\n1m");
                break;
            case 101:
                beaconStatus.setText("beacon\n2m");
                break;
            case 102:
                beaconStatus.setText("beacon\n5m");
                break;
            case 103:
                beaconStatus.setText("beacon\n10m");
                break;
            case 104:
                beaconStatus.setText("beacon\n15m");
                break;
            case 105:
                beaconStatus.setText("beacon\n30m");
                break;
            case 106:
                beaconStatus.setText("beacon\n1hr");
                break;
            case 107:
                beaconStatus.setText("beacon\n6hr");
                break;
            case 108:
                beaconStatus.setText("beacon\n12hr");
                break;
        }
    }

    public boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        boolean isEnabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            gpsError.setBackgroundColor(ContextCompat.getColor(this, R.color.good_gps));
            isEnabled = true;
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            gpsError.setBackgroundColor(ContextCompat.getColor(this, R.color.good_gps));
            isEnabled = true;
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
           isEnabled = false;
            gpsError.setBackgroundColor(ContextCompat.getColor(this, R.color.bad_gps));
        }
        return isEnabled;
    }
}
