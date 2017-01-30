package technology.xor.chirp.mains;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import technology.xor.chirp.AppController;
import technology.xor.chirp.R;
import technology.xor.chirp.dialogs.ForgotDialog;
import technology.xor.chirp.support.AppData;

public class LoginActivity extends AppCompatActivity {

    private EditText userEmail;
    private EditText userPass;
    private Button loginBtn;
    private CheckBox newUser;
    private TextView forgot;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        userEmail = (EditText) findViewById(R.id.et_email);
        userPass = (EditText) findViewById(R.id.et_password);
        loginBtn = (Button) findViewById(R.id.btn_signin);
        newUser = (CheckBox) findViewById(R.id.tb_signup);
        forgot = (TextView) findViewById(R.id.tv_signup);

        mAuth = FirebaseAuth.getInstance();

        String msg = "Forgot password? <font color='#1fb6ed'>Click here</font> to reset!";
        forgot.setText(Html.fromHtml(msg));

        RequestPermissionLocation();

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgotDialog forgotDialog = new ForgotDialog();
                forgotDialog.AlertUser(LoginActivity.this);
            }
        });

        userEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userEmail.setError(null);
                userPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        userPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userEmail.setError(null);
                userPass.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userEmail.getText().toString().length() == 0 || !IsValidEmail(userEmail.getText().toString())) {
                    userEmail.setText("");
                    userPass.setText("");
                    userEmail.setError("Invalid email address!");
                } else if (userPass.getText().toString().length() == 0) {
                    userEmail.setText("");
                    userPass.setText("");
                    userPass.setError("Invalid password!");
                } else {
                    UserLogin(userEmail.getText().toString(), userPass.getText().toString());
                }
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    LoginSuccess();
                    Log.d("Login", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d("Login", "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void RequestPermissionLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        AppData.MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                Log.d("Home", "Already granted access to location.");
            }
        }
    }

    private void UserLogin(String username, String password) {

        // showProgressDialog();

        if (newUser.isChecked()) {
            mAuth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("Login", "createUserWithEmail:onComplete:" + task.isSuccessful());

                            if (!task.isSuccessful()) {
                                userEmail.setText("");
                                userPass.setText("");
                                Toast.makeText(getBaseContext(), "Email already exists!", Toast.LENGTH_SHORT).show();
                            } else {
                                LoginSuccess();
                            }
                            // hideProgressDialog();
                        }
                    });
        } else {
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("Login", "signInWithEmail:onComplete:" + task.isSuccessful());

                            if (!task.isSuccessful()) {
                                Log.w("Login", "signInWithEmail:failed", task.getException());
                                Toast.makeText(getBaseContext(), "Login failed!", Toast.LENGTH_SHORT).show();
                            } else {
                                LoginSuccess();
                            }
                            // hideProgressDialog();
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppData.MY_PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Uri packageURI = Uri.parse("package:" + AppController.class.getPackage().getName());
                    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                    startActivity(uninstallIntent);
                }
                return;
            default:
                Log.e("Login", "Failed to login.");
        }
    }

    private void LoginSuccess() {
        // Become user - Token

        Intent returnIntent = new Intent();
        setResult(RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED,returnIntent);
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED,returnIntent);
        super.onBackPressed();
        finish();
    }

    private boolean IsValidEmail(String email) {
        CharSequence target = email;
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
