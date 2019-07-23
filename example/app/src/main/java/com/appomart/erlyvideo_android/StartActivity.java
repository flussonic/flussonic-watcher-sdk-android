package com.appomart.erlyvideo_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class StartActivity extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 1500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_app);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent mainIntent = new Intent(StartActivity.this, LoginActivity.class);
                StartActivity.this.startActivity(mainIntent);
                StartActivity.this.finish();
                overridePendingTransition(0, 0);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}