package com.uascent.android.pethunting.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.uascent.android.pethunting.R;
import com.uascent.android.pethunting.devices.BluetoothAntiLostDevice;
import com.uascent.android.pethunting.model.BtDevice;
import com.uascent.android.pethunting.myviews.VerticalSeekBar;
import com.uascent.android.pethunting.service.BleComService;
import com.uascent.android.pethunting.tools.Lg;

public class PlayActivity extends BaseActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, VerticalSeekBar.OnSeekBarStopListener {
    private static final String TAG = "PlayActivity";
    private VerticalSeekBar ver_sb;
    private TextView ver_sb_per, tv_empty;
    private ImageView iv_play_guide, iv_play_home;
    private ImageView iv_top_dir, iv_below_dir, iv_left_dir, iv_right_dir;
    private IService mService;
    //    private Handler mHandler;
    private BtDevice device;
    //    private static final int STOPCMD = 0;
//    private static final int TOPCMD = 1;
//    private static final int BOMCMD = 2;
//    private static final int LEFTCMD = 3;
//    private static final int RIGHTCMD = 4;
    private static int speedValue = 0;
    private static int dirValue = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_play);
        super.onCreate(savedInstanceState);
        initViews();
        device = (BtDevice) getIntent().getSerializableExtra("device");
        Intent i = new Intent(this, BleComService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        ver_sb_per = (TextView) findViewById(R.id.ver_sb_per);
        ver_sb = (VerticalSeekBar) findViewById(R.id.ver_sb);
//        new VerticalSeekBar(this).setSeekBarStopListener(this);
        VerticalSeekBar.setSeekBarStopListener(this);
        ver_sb.setOnSeekBarChangeListener(this);
        tv_empty = (TextView) findViewById(R.id.tv_empty);
        tv_empty.setOnClickListener(this);
        iv_play_guide = (ImageView) findViewById(R.id.iv_play_guide);
        iv_play_guide.setOnClickListener(this);
        iv_play_home = (ImageView) findViewById(R.id.iv_play_home);
        iv_play_home.setOnClickListener(this);
        iv_top_dir = (ImageView) findViewById(R.id.iv_top_dir);
        iv_top_dir.setOnClickListener(this);
        iv_below_dir = (ImageView) findViewById(R.id.iv_below_dir);
        iv_below_dir.setOnClickListener(this);
        iv_left_dir = (ImageView) findViewById(R.id.iv_left_dir);
        iv_left_dir.setOnClickListener(this);
        iv_right_dir = (ImageView) findViewById(R.id.iv_right_dir);
        iv_right_dir.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.iv_play_guide:
                intent = new Intent(this, UserGuide4Activity.class);
                startActivity(intent);
                break;

            case R.id.iv_play_home:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.tv_empty:
                dirValue = BluetoothAntiLostDevice.MOUSE_STOP;
                sendMouseCmd(device.getAddress(), dirValue);
                break;

            case R.id.iv_top_dir:
                dirValue = BluetoothAntiLostDevice.MOUSE_UP;
                sendMouseCmd(device.getAddress(), BluetoothAntiLostDevice.MOUSE_UP);
                break;

            case R.id.iv_below_dir:
                dirValue = BluetoothAntiLostDevice.MOUSE_DOWN;
                sendMouseCmd(device.getAddress(), dirValue);
                break;

            case R.id.iv_left_dir:
                dirValue = BluetoothAntiLostDevice.MOUSE_LEFT;
                sendMouseCmd(device.getAddress(), dirValue);
                break;

            case R.id.iv_right_dir:
                dirValue = BluetoothAntiLostDevice.MOUSE_RIGHT;
                sendMouseCmd(device.getAddress(), dirValue);
                break;

            default:
                break;
        }
    }

    void sendMouseCmd(String addr, int cmd) {
        controlMouseDir(addr, cmd);
        getMouseRsp(addr);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        ver_sb_per.setText(progress + "%");
        speedValue = progress;
        Lg.i(TAG, "onProgressChanged");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
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
        }

        @Override
        public boolean onRead(String address, byte[] val) throws RemoteException {
            Lg.i(TAG, "onRead called");

            return false;
        }


        @Override
        public boolean onWrite(final String address, byte[] val) throws RemoteException {
            Lg.i(TAG, "onWrite called");
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

    public void controlMouseDir(String addr, int dir) {
        try {
            mService.controlMouse(addr, dir);
            Lg.i(TAG, "controlMouseDir->>" + dir);
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
    protected void onDestroy() {
        if (mConnection != null) {
            try {
                if (dirValue != BluetoothAntiLostDevice.MOUSE_STOP) {
                    controlMouseDir(device.getAddress(), BluetoothAntiLostDevice.MOUSE_STOP);
                }
                Log.i(TAG, "onDestroy->>unregisterCallback");
                mService.unregisterCallback(mCallback);
                if (device != null) {
                    mService.disconnect(device.getAddress());
                    Lg.i(TAG, "disconnect_device_address = " + device.getAddress());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        Log.i(TAG, "onDestroy->>unbindService");
        super.onDestroy();
    }

    @Override
    public void onSeekBarStop() {
        Lg.i(TAG, "onSeekBarStop_onStartTrackingTouch");
//        turnOffImmediateAlert(device.getAddress(), speedValue);
    }

}
