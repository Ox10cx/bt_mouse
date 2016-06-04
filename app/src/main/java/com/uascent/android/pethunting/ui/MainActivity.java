package com.uascent.android.pethunting.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.uascent.android.pethunting.R;
import com.uascent.android.pethunting.myviews.ComReminderDialog;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private Button bt_set_up;
    private Button bt_user_guide;
    private Button bt_play;
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    /**
     * 打开蓝牙开关请求码
     */
    private static final int REQUEST_ENABLE_CODE = 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        initViews();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        final ComReminderDialog dialog = new ComReminderDialog(this, getResources().getString(R.string.exit_app_remind)
                , getResources().getString(R.string.yes), getResources().getString(R.string.no));
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.dialog_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.cancel();
                MainActivity.this.finish();
            }
        });

        dialog.dialog_submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
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
                if (!btAdapter.isEnabled()) {
//                    showShortToast(getResources().getString(R.string.bluetooth_switch_not_opened));
                    final ComReminderDialog dialog = new ComReminderDialog(this, getResources().getString(R.string.open_bluetooth_switch)
                            , getResources().getString(R.string.no), getResources().getString(R.string.yes));
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.dialog_cancel.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            showShortToast(getResources().getString(R.string.bluetooth_switch_not_opened));
                            dialog.cancel();
                        }
                    });
                    dialog.dialog_submit.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableIntent, REQUEST_ENABLE_CODE);
                            dialog.cancel();
                        }
                    });
                } else {
                    intent = new Intent(this, ConnectCatActivity.class);
                    startActivity(intent);
                }
//                intent = new Intent(this, PlayActivity.class);
//                startActivity(intent);
                break;
            default:
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_CODE: {
                if (resultCode == Activity.RESULT_OK) {
//                    showShortToast(getResources().getString(R.string.bluetooth_switch_has_opened));
                    Intent intent = new Intent(this, ConnectCatActivity.class);
                    startActivity(intent);
                } else {
                    showShortToast(getResources().getString(R.string.bluetooth_switch_not_opened));
                }
                break;
            }
            default:
                break;
        }
    }
}
