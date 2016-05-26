package com.uascent.android.pethunting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.uascent.android.pethunting.R;

public class FristFlashActivity extends BaseActivity {
    private final static String TAG = "FristFlashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_frist);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "postDelayed");
                startActivity(new Intent(FristFlashActivity.this, MainActivity.class));
                finish();
            }
        }, 800);
    }


}
