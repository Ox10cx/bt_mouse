package com.uascent.android.pethunting.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.uascent.android.pethunting.R;
import com.uascent.android.pethunting.myviews.ComReminderDialog;

public class SetUp2Activity extends BaseActivity implements View.OnClickListener {
    private Button bt_menu;
    private ImageView iv_blue_switch;
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    /**
     * 打开蓝牙开关请求码
     */
    private static final int REQUEST_ENABLE_CODE = 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setup2);
        initViews();
        super.onCreate(savedInstanceState);
    }

    private void initViews() {
        bt_menu = (Button) findViewById(R.id.bt_menu);
        bt_menu.setOnClickListener(this);
        iv_blue_switch = (ImageView) findViewById(R.id.iv_blue_switch);
        //取消点击蓝牙开关
//        iv_blue_switch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.bt_menu:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_blue_switch:
                //判断蓝牙开关是否打开
                if (!btAdapter.isEnabled()) {
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
                break;
            default:
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_CODE: {
                if (resultCode == Activity.RESULT_OK) {
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
