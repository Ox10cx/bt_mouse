package com.uascent.android.pethunting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.uascent.android.pethunting.R;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private Button bt_set_up;
    private Button bt_user_guide;
    private Button bt_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        bt_set_up = (Button) findViewById(R.id.bt_set_up);
        bt_set_up.setOnClickListener(this);
        bt_user_guide = (Button) findViewById(R.id.bt_user_guide);
        bt_user_guide.setOnClickListener(this);
        bt_play = (Button) findViewById(R.id.bt_play);
        bt_play.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.bt_set_up:
                intent = new Intent(this, SetUp2Activity.class);
                startActivity(intent);
                break;
            case R.id.bt_user_guide:
                intent = new Intent(this, UserGuide4Activity.class);
                startActivity(intent);
                break;
            case R.id.bt_play:
                intent = new Intent(this, PlayActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
