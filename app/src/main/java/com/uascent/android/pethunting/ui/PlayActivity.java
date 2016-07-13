package com.uascent.android.pethunting.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.uascent.android.pethunting.MyApplication;
import com.uascent.android.pethunting.R;
import com.uascent.android.pethunting.devices.BluetoothAntiLostDevice;
import com.uascent.android.pethunting.model.BtDevice;
import com.uascent.android.pethunting.myviews.VerticalSeekBar;
import com.uascent.android.pethunting.service.BleComService;
import com.uascent.android.pethunting.tools.Lg;

public class PlayActivity extends BaseActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, VerticalSeekBar.OnSeekBarStopListener
        , VerticalSeekBar.OnSeekBarStopTouchListener, View.OnTouchListener {
    private static final String TAG = "PlayActivity";
    private VerticalSeekBar ver_sb;
    private TextView ver_sb_per, tv_empty;
    private ImageView iv_play_guide, iv_play_home;
    private ImageView iv_top_dir, iv_below_dir, iv_left_dir, iv_right_dir;
    private IService mService;
    private Handler mHandler;
    private BtDevice device;
    private static int speedValue = 0;
    private static int dirValue = 0;
    private int startSpeed = 0;
    //    private int battery_status = 0;
    private ImageView iv_battery;
    //    private int dirCmdRepeatCount = 0;
    private int preDirValue = 0;
    private Long preTime;
//    private boolean isSendCmd = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play);
        super.onCreate(savedInstanceState);
        initViews();
        mHandler = new Handler();
        device = (BtDevice) getIntent().getSerializableExtra("device");
        Intent i = new Intent(this, BleComService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        ver_sb_per = (TextView) findViewById(R.id.ver_sb_per);
        ver_sb = (VerticalSeekBar) findViewById(R.id.ver_sb);
        VerticalSeekBar.setSeekBarStopListener(this);
        VerticalSeekBar.setSeekBarStopTouchListener(this);
        ver_sb.setOnSeekBarChangeListener(this);
        tv_empty = (TextView) findViewById(R.id.tv_empty);
        tv_empty.setOnClickListener(this);
        iv_play_guide = (ImageView) findViewById(R.id.iv_play_guide);
        iv_play_guide.setOnClickListener(this);
        iv_play_home = (ImageView) findViewById(R.id.iv_play_home);
        iv_play_home.setOnClickListener(this);

        iv_top_dir = (ImageView) findViewById(R.id.iv_top_dir);
        iv_top_dir.setOnTouchListener(this);
        iv_top_dir.setOnClickListener(this);

        iv_below_dir = (ImageView) findViewById(R.id.iv_below_dir);
        iv_below_dir.setOnClickListener(this);
        iv_below_dir.setOnTouchListener(this);

        iv_left_dir = (ImageView) findViewById(R.id.iv_left_dir);
        iv_left_dir.setOnClickListener(this);
        iv_left_dir.setOnTouchListener(this);

        iv_right_dir = (ImageView) findViewById(R.id.iv_right_dir);
        iv_right_dir.setOnClickListener(this);
        iv_right_dir.setOnTouchListener(this);

        iv_battery = (ImageView) findViewById(R.id.iv_battery);
    }

    @Override
    public void onClick(View v) {
        Lg.i(TAG, "onClick" + v.getId());
        Intent intent = null;
        switch (v.getId()) {
            case R.id.iv_play_guide:
                intent = new Intent(this, UserGuideActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_play_home:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    void sendMouseCmd(String addr, int cmd) {
        Lg.i(TAG, "sendMouseCmd:" + cmd);
//        return;

        //有用
        controlMouseDir(addr, cmd);
        // getMouseRsp(addr);

    }

    /**
     * 发送控制老鼠速度命令
     *
     * @param addr
     * @param cmd
     */
    void sendMouseSpeedCmd(String addr, int value, int cmd) {
        Lg.i(TAG, "sendMouseCmd:" + value + "  " + cmd);
//        return;
        //有用
        controlMouseSpeed(addr, value, cmd);
        //没有改变
        // getMouseRsp(addr);

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        ver_sb_per.setText(progress + "%");
        speedValue = progress / 10;
//        Lg.i(TAG, "onProgressChanged_speedValue->>" + speedValue);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Lg.i(TAG, "play_onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Lg.i(TAG, "play_onStartTrackingTouch");
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Lg.i(TAG, "onServiceDisconnected");
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Lg.i(TAG, "onServiceConnected");
            mService = IService.Stub.asInterface(service);
            try {
                mService.registerCallback(mCallback);
                mService.setBatteryNoc(device.getAddress());
                //读电量
            } catch (RemoteException e) {
                Lg.i(TAG, " " + e);
            }
        }
    };


    private ICallback.Stub mCallback = new ICallback.Stub() {
        @Override
        public void onConnect(String address) throws RemoteException {
            Lg.i(TAG, "onConnect called");
        }

        @Override
        public void onDisconnect(String address) throws RemoteException {
            Lg.i(TAG, "onDisconnect called");
            //蓝牙意外断开，考虑重连接
            if (!MyApplication.getInstance().isAutoBreak && device != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showShortToast(getString(R.string.bluetooth_has_breaked));
                    }
                });
            }
        }

        @Override
        public boolean onRead(String address, byte[] val) throws RemoteException {
            Lg.i(TAG, "onRead called");
            return false;
        }


        @Override
        public boolean onWrite(final String address, final byte[] val) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Lg.i(TAG, "onWrite->>" + val[0]);
                    if (val[0] <= 30) {
                        iv_battery.setBackgroundResource(R.drawable.empty_battery);
                        showLongToast(getString(R.string.low_battery_remind));
                    } else if (val[0] >= 70) {
                        iv_battery.setBackgroundResource(R.drawable.full_battery);
                    } else {
                        iv_battery.setBackgroundResource(R.drawable.half_battery);
                    }
                }
            });
            return true;
        }

        @Override
        public void onSignalChanged(String address, int rssi) throws RemoteException {
            Lg.i(TAG, "onSignalChanged called address = " + address + " rssi = " + rssi);
        }

        public void onPositionChanged(String address, int position) throws RemoteException {
            Lg.i(TAG, "onPositionChanged called address = " + address + " newpos = " + position);
        }

        @Override
        public void onAlertServiceDiscovery(final String btaddr, boolean support) throws RemoteException {

        }

        @Override
        public void onMouseServiceDiscovery(String address, boolean support) throws RemoteException {

        }
    };

    /**
     * 控制方向
     *
     * @param addr
     * @param dir
     */
    public void controlMouseDir(String addr, int dir) {
        try {
            mService.controlMouse(addr, dir);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制加速度
     *
     * @param addr
     * @param dir
     */
    public void controlMouseSpeed(String addr, int value, int dir) {
        try {
            mService.controlMouseSpeed(addr, value, dir);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void getMouseRsp(String addr) {
        try {
            mService.readMouseRsp(addr);
            Lg.i(TAG, "readMouseRsp->>" + addr);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mConnection != null) {
            if (dirValue != BluetoothAntiLostDevice.MOUSE_STOP) {
                controlMouseDir(device.getAddress(), BluetoothAntiLostDevice.MOUSE_STOP);
                Lg.i(TAG, "onPause" + "direct exit");
            }
        }
    }

    @Override
    protected void onDestroy() {
        MyApplication.getInstance().isAutoBreak = true;
        if (mConnection != null) {
            try {
                if (dirValue != BluetoothAntiLostDevice.MOUSE_STOP) {
                    controlMouseDir(device.getAddress(), BluetoothAntiLostDevice.MOUSE_STOP);
                }
                Lg.i(TAG, "onDestroy->>unregisterCallback");
                mService.unregisterCallback(mCallback);
                if (device != null) {
                    Lg.i(TAG, "disconnect_device_address = " + device.getAddress());
                    mService.disconnect(device.getAddress());
                    device = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        Lg.i(TAG, "onDestroy->>unbindService");
        super.onDestroy();
    }


    @Override
    public void onSeekBarStop() {
        Lg.i(TAG, "onSeekBarStop_滑动到底部-->>" + "   speedValue:" + 0 + "  dirValue: " + dirValue);
        ver_sb.setProgress(0);
        if (dirValue != 0) {
            sendMouseSpeedCmd(device.getAddress(), 0, dirValue);
        } else {  //方向键先松开  dirvalue=0
            sendMouseCmd(device.getAddress(), dirValue);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.iv_top_dir:
                Lg.i(TAG, "onTouch_iv_top_dir");
                dirValue = BluetoothAntiLostDevice.MOUSE_UP;
                break;
            case R.id.iv_below_dir:
                dirValue = BluetoothAntiLostDevice.MOUSE_DOWN;
                break;
            case R.id.iv_left_dir:
                dirValue = BluetoothAntiLostDevice.MOUSE_LEFT;
                break;
            case R.id.iv_right_dir:
                dirValue = BluetoothAntiLostDevice.MOUSE_RIGHT;
                break;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:  //判断按下和抬起的时间间隔
                Lg.i(TAG, "event.getAction()---ACTION_DOWN--" + dirValue);
                if (dirValue == preDirValue && System.currentTimeMillis() - preTime < 500) {
                    MyApplication.isCmdSendRepeat = true;
//                    dirCmdRepeatCount++;
//                    if (dirCmdRepeatCount % 3 == 0) {
                    Lg.i(TAG, "event.getAction()---ACTION_DOWN--sendCmd--repeat" + dirValue);
//                        sendMouseCmd(device.getAddress(), dirValue);
//                        isSendCmd = true;
//                    }
//                } else {
//                    Lg.i(TAG, "event.getAction()---ACTION_DOWN--sendCmd" + dirValue);
//                    sendMouseCmd(device.getAddress(), dirValue);
//                    isSendCmd = true;
                } else {
                    MyApplication.isCmdSendRepeat = false;
                }
                sendMouseCmd(device.getAddress(), dirValue);
                preDirValue = dirValue;
                preTime = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                dirValue = BluetoothAntiLostDevice.MOUSE_STOP;
                Lg.i(TAG, "event.getAction()---ACTION_UP--" + dirValue);
//                        if (isSendCmd) {
                sendMouseCmd(device.getAddress(), dirValue);
//                            isSendCmd = false;
//                        }
                break;
        }
        return false;
    }

    @Override
    public void onSeekBarStopTouch() {
        Lg.i(TAG, "onSeekBarStopTouch_speedValue->>>" + speedValue + " startSpeed->>>" + startSpeed);
        if (speedValue != startSpeed) {
            startSpeed = speedValue;
            if (dirValue != 0) {
                MyApplication.isCmdSendRepeat=false;
                sendMouseSpeedCmd(device.getAddress(), speedValue, dirValue);
            } else {  //方向键先松开
                sendMouseCmd(device.getAddress(), dirValue);
            }
        }
    }
}
