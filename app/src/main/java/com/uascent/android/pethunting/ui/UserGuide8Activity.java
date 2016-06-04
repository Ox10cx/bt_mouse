package com.uascent.android.pethunting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.uascent.android.pethunting.R;

public class UserGuide8Activity extends BaseActivity implements View.OnClickListener {
    private Button bt_back;
    private Button bt_menu;
    private Button bt_next;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_userguide8);
        initViews();
        super.onCreate(savedInstanceState);
    }

    private void initViews() {
        bt_back = (Button) findViewById(R.id.bt_back);
        bt_back.setOnClickListener(this);
        bt_menu = (Button) findViewById(R.id.bt_menu);
        bt_menu.setOnClickListener(this);
        bt_next = (Button) findViewById(R.id.bt_next);
        bt_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.bt_menu:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_back:
                intent = new Intent(this, UserGuide7Activity.class);
                startActivity(intent);
                break;
            case R.id.bt_next:
                intent = new Intent(this, UserGuide4Activity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
