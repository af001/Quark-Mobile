package technology.xor.chirp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import technology.xor.chirp.mains.BeaconActivity;
import technology.xor.chirp.mains.LoginActivity;
import technology.xor.chirp.support.AppData;

public class AppController extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            LoginToApp();
        } else {
            Intent loginIntent = new Intent(AppController.this, LoginActivity.class);
            startActivityForResult(loginIntent, AppData.REQUEST_FIREBASE_LOGIN);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == AppData.REQUEST_FIREBASE_LOGIN) {
            if (resultCode == RESULT_OK) {
                LoginToApp();
            } else {
                finish();
            }
        }
    }

    private void LoginToApp() {
        Intent mapIntent = new Intent(AppController.this, BeaconActivity.class);
        startActivity(mapIntent);
        finish();
    }

}