package uk.co.stableweb.firebaseremoteconfig;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {

    // Initialization of Firebase Remote Config object
    private FirebaseRemoteConfig mRemoteConfig;

    // Remote Config Fields
    private static final String CONFIG_SIGNUP_MESSAGE = "signup_prompt";
    private static final String CONFIG_MINIMUM_PASSWORD_LENGTH = "min_password";
    private static final String CONFIG_IS_PROMOTION_AVAILABLE = "is_promotion_available";
    private static final String CONFIG_COLOR_PRIMARY = "color_primary";

    // view initialization
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView titleTextView;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // views
        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);
        titleTextView = (TextView) findViewById(R.id.textview_signup_prompt);
        signupButton = (Button) findViewById(R.id.sign_up_button);

        initializeRemoteConfig();

    }

    private void initializeRemoteConfig(){
        mRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings remoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        mRemoteConfig.setConfigSettings(remoteConfigSettings);

        mRemoteConfig.setDefaults(R.xml.default_remote_config_values);

        fetchRemoteConfigValues();
    }

    private void fetchRemoteConfigValues(){
        // cache expiration in seconds
        long cacheExpiration = 3600; // 1 hour in seconds

        // If in developer mode cacheExpiration is set to 0 so each fetch
        // will retrieve values from the server
        if(mRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()){
            cacheExpiration = 0;
        }

        // fetches the values and listen for fails.
        mRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(
                                    MainActivity.this,
                                    "Fetch Succeeded!",
                                    Toast.LENGTH_LONG
                            ).show();

                            // Once the config is successful, it must be activated
                            mRemoteConfig.activateFetched();
                        }else{
                            Toast.makeText(
                                    MainActivity.this,
                                    "Fetch failed!",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                        //display values
                        setupValue();
                    }
                });

    }

    private void setupValue(){
        String signupPromptValue = mRemoteConfig
                .getString(CONFIG_SIGNUP_MESSAGE);
        boolean isPromotionAvailable = mRemoteConfig
                .getBoolean(CONFIG_IS_PROMOTION_AVAILABLE);

        titleTextView.setText(isPromotionAvailable ?
                signupPromptValue : titleTextView.getText().toString());

        passwordEditText
                .setHint("Enter " +
                        mRemoteConfig.getString(CONFIG_MINIMUM_PASSWORD_LENGTH) +
                        " minimum letters.");

        signupButton.setBackgroundColor(isPromotionAvailable ?
                Color.parseColor(mRemoteConfig.getString(CONFIG_COLOR_PRIMARY))
                : ContextCompat.getColor(this, R.color.colorPrimary));

    }

}
