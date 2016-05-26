package com.uascent.android.pethunting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.uascent.android.pethunting.R;

public class SetUp2Activity extends BaseActivity implements View.OnClickListener {
    private Button bt_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);
        initViews();
    }

    private void initViews() {
        bt_menu = (Button) findViewById(R.id.bt_menu);
        bt_menu.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_menu:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
