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
import com.uascent.android.pethunting.model.BtDevice;
import com.uascent.android.pethunting.service.BleComService;
import com.uascent.android.pethunting.tools.Lg;

public class PlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "PlayActivity";
    private SeekBar ver_sb;
    private TextView ver_sb_per, tv_empty;
    private ImageView iv_play_guide, iv_play_home;
    private ImageView iv_top_dir, iv_below_dir, iv_left_dir, iv_right_dir;
    private IService mService;
//    private Handler mHandler;
    private BtDevice device;
    private static final int STOPCMD = 0;
    private static final int TOPCMD = 1;
    private static final int BOMCMD = 2;
    private static final int LEFTCMD = 3;
    private static final int RIGHTCMD = 4;


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
        ver_sb = (SeekBar) findViewById(R.id.ver_sb);
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
                turnOnImmediateAlert(device.getAddress(), 0);
                break;
            case R.id.iv_top_dir:
                turnOnImmediateAlert(device.getAddress(), 1);
                break;
            case R.id.iv_below_dir:
                turnOnImmediateAlert(device.getAddress(), 2);
                break;
            case R.id.iv_left_dir:
                turnOnImmediateAlert(device.getAddress(), 3);
                break;
            case R.id.iv_right_dir:
                turnOnImmediateAlert(device.getAddress(), 4);
                break;
            default:
                break;
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        ver_sb_per.setText(progress + "%");
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

//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
////                    scanLeDevice(true);
//                    Lg.i(TAG, "scanLeDevice(true)");
//                }
//            });
        }
    };


    private ICallback.Stub mCallback = new ICallback.Stub() {
        @Override
        public void onConnect(String address) throws RemoteException {
        }

        @Override
        public void onDisconnect(String address) throws RemoteException {
//            synchronized (mListData) {
            Lg.i(TAG, "onDisconnect called");
            //断开连接， do .....
//                for (int i = 0; i < mListData.size(); i++) {
//                    BtDevice d = mListData.get(i);
//                    if (d.getAddress().equals(address)) {
//                        d.setStatus(BluetoothAntiLostDevice.BLE_STATE_INIT);
//                        d.setPosition(BtDevice.LOST);
//                    }
//                }
//            }

//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
////                    mDeviceListAdapter.notifyDataSetChanged();
////                    checkAntiLost();
//                }
//            });
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
//            synchronized (mListData) {
            Lg.i(TAG, "onSignalChanged called address = " + address + " rssi = " + rssi);
//                for (int i = 0; i < mListData.size(); i++) {
//                    BtDevice d = mListData.get(i);
//                    if (d.getAddress().equals(address)) {
//                        d.setRssi(rssi);
//                    }
//                }
//            }
        }

        public void onPositionChanged(String address, int position) throws RemoteException {
//            synchronized (mListData) {
            Lg.i(TAG, "onPositionChanged called address = " + address + " newpos = " + position);
//                for (int i = 0; i < mListData.size(); i++) {
//                    BtDevice d = mListData.get(i);
//                    if (d.getAddress().equals(address)) {
//                        d.setPosition(position);
//                    }
//                }
//            }
        }

        @Override
        public void onAlertServiceDiscovery(final String btaddr, boolean support) throws RemoteException {

        }
    };

    public void turnOnImmediateAlert(String addr, int index) {
        try {
            mService.turnOnImmediateAlert(addr, index);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (mConnection != null) {
            try {
                Log.i(TAG, "onDestroy->>unregisterCallback");
                mService.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        Log.i(TAG, "onDestroy->>unbindService");
        super.onDestroy();
    }
}
