package com.uascent.android.pethunting.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uascent.android.pethunting.MyApplication;
import com.uascent.android.pethunting.R;
import com.uascent.android.pethunting.adapter.DeviceListAdapter;
import com.uascent.android.pethunting.model.BtDevice;
import com.uascent.android.pethunting.service.BleComService;
import com.uascent.android.pethunting.tools.Lg;

import java.util.ArrayList;

/**
 * Created by lenovo001 on 2016/5/28.
 */
public class ConnectCatActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "ConnectCatActivity";

    private ArrayList<BtDevice> mListData = new ArrayList<>();
    private IService mService;
    private Handler mHandler;
    boolean mScanningStopped;
    private ImageView iv_load_null;
    private Button bt_match;
    private PullToRefreshListView lv_device;
    private DeviceListAdapter adpater;

    private int index_checked = 0;
    private int count_device = 0;
    private BtDevice device;
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * 正在连接设备
     */
    private boolean isConnecting = false;

    /**
     * 正在刷新
     */
    private boolean isRefresh = false;

    //没有触发检查服务
    private boolean isClickMatch = false;
    private boolean isCheckServicee = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_connect_cat);
        super.onCreate(savedInstanceState);
        initViews();
        mHandler = new Handler();
        showLoadingDialog();
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        } else {
            showShortToast(getString(R.string.moible_not_support_bluetooth4));
        }
        Intent i = new Intent(this, BleComService.class);
        getApplicationContext().bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initViews() {
        bt_match = (Button) findViewById(R.id.bt_match);
        bt_match.setOnClickListener(this);
        iv_load_null = (ImageView) findViewById(R.id.iv_load_null);
        lv_device = (PullToRefreshListView) findViewById(R.id.lv_device);
        lv_device.setOnItemClickListener(this);
        lv_device.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                Lg.i(TAG, "onRefresh>>>>" + label);

                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                // Do work to refresh the list here.
                doRefreshWork();
            }
        });
        adpater = new DeviceListAdapter(this, mListData);
        lv_device.setAdapter(adpater);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(true);
                    Lg.i(TAG, "scanLeDevice(true)");
                }
            });
        }
    };


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            //搜索10s,10s后停止搜索
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    closeLoadingDialog();
                    Lg.i(TAG, "stop scanning after 10s");
                    refreshOk();
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanningStopped = true;
                    if (mListData == null || mListData.size() == 0) {
                        showShortToast(getResources().getString(R.string.search_device_empty));
                        iv_load_null.setVisibility(View.VISIBLE);
                        Lg.i(TAG, "mListData的大小为0");
                    }
                }
            }, 10 * 1000);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
//            mBluetoothAdapter.startLeScan(new UUID[]{BluetoothAntiLostDevice.MOUSE_SERVICE_UUID,
//                    BluetoothAntiLostDevice.ALERT_SERVICE_UUID,BluetoothAntiLostDevice.LOSS_SERVICE_UUID,
//                    BluetoothAntiLostDevice.POWER_SERVICE_UUID,BluetoothAntiLostDevice.KEY_SERVICE_UUID}, mLeScanCallback);
//            mBluetoothAdapter.startLeScan(new UUID[]{BluetoothAntiLostDevice.KEY_SERVICE_UUID}, mLeScanCallback);
            mScanningStopped = false;
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanningStopped = true;
        }
    }

    private void refreshOk() {
        if (isRefresh) {
            lv_device.onRefreshComplete();
            isRefresh = false;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
//                    closeLoadingDialog();
                    addDevice(device.getAddress(), device.getName(), rssi);
                }
            };

    private ICallback.Stub mCallback = new ICallback.Stub() {
        @Override
        public void onConnect(String address) throws RemoteException {
            Lg.i(TAG, "onConnect calll");
            isConnecting = true;
            isCheckServicee = false;
        }

        @Override
        public void onDisconnect(String address) throws RemoteException {
            Lg.i(TAG, TAG + "  onDisconnect called");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    closeLoadingDialog();
                    if (isConnecting == false && isClickMatch == true) {
                        showShortToast(getString(R.string.check_ble_switch));
                        return;
                    }
                    isConnecting = false;
                    if (isCheckServicee == false && isClickMatch == true) {
                        showShortToast(getString(R.string.device_service_not_match));
                        isClickMatch = false;
                    }
                }
            });

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
            Lg.i(TAG, "onPositionChanged called address = " + address + " newpos = " + position);
        }

        @Override
        public void onAlertServiceDiscovery(final String btaddr, final boolean support) throws RemoteException {
            Lg.i(TAG, "onAlertServiceDiscovery");
        }

        @Override
        public void onMouseServiceDiscovery(final String address, final boolean support) throws RemoteException {
            isCheckServicee = true;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (support) {
                        Lg.i(TAG, "onAlertServiceDiscovery_support");
                        closeLoadingDialog();
                        if (isConnecting) {
                            //判断服务
                            Intent intent = new Intent(ConnectCatActivity.this, PlayActivity.class);
                            intent.putExtra("device", device);
                            startActivity(intent);
                            isConnecting = false;
                            isClickMatch = false;
                        }
                    } else {
                        Lg.i(TAG, "onAlertServiceDiscovery_not_support");
                        closeLoadingDialog();
                        showShortToast(getString(R.string.device_service_not_match));
                        try {
                            mService.disconnect(address);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    };

    public void addDevice(final String address, final String name, final int rssi) {
        Lg.i(TAG, "addDevice called:" + address + "   " + name + "  " + rssi);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //避免重复添加
                for (BtDevice ele : mListData) {
                    if (ele.getAddress().equalsIgnoreCase(address)) {
                        return;
                    }
                }
                BtDevice device = new BtDevice();
                device.setName(name);
                device.setAddress(address);
                device.setRssi(rssi);
                if (count_device == 0) {
                    device.setChecked(true);
                }
                mListData.add(device);
                iv_load_null.setVisibility(View.GONE);
                count_device = count_device + 1;
                Lg.i(TAG, "add_device_ok");
                adpater.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListData.get(index_checked).setChecked(false);
        Lg.i(TAG, "position-1=:" + (position - 1));
        mListData.get(position - 1).setChecked(true);
        index_checked = position - 1;
        adpater.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_match:
                //没用
//                Intent intent = new Intent(ConnectCatActivity.this, PlayActivity.class);
//                intent.putExtra("device", device);
//                startActivity(intent);

//                //有用
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    refreshOk();
                    showLoadingDialog(getString(R.string.connecting_device));
                } else {
                    mBluetoothAdapter.enable();
                }
                if (mListData == null || mListData.size() == 0) {
                    showShortToast(getResources().getString(R.string.device_is_empty_not_match));
                    return;
                }
                Lg.i(TAG, "index_checked——>>>>>" + index_checked);
                if (!connectBLE(index_checked)) {
                    showShortToast(getResources().getString(R.string.match_device_fail));
                }
                isClickMatch = true;
                break;
            default:
                break;
        }

    }


    public boolean connectBLE(int index) {
        boolean ret = false;
        device = mListData.get(index);
//        int status = device.getStatus();
//        Lg.i(TAG, "device_status = " + status);
        if (device != null && device.getAddress() != null) {
            Lg.i(TAG, "device_address = " + device.getAddress());
            try {
                ret = mService.connect(device.getAddress());
            } catch (RemoteException e) {
                e.printStackTrace();
                return ret;
            }
        }
        return ret;
    }

    @Override
    public void onBackPressed() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        MyApplication.getInstance().isAutoBreak = true;
        if (mConnection != null) {
            try {
                if (device != null) {
                    mService.disconnect(device.getAddress());
                    Lg.i(TAG, "disconnect_device_address = " + device.getAddress());
                }
                mService.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        getApplicationContext().unbindService(mConnection);
        super.onDestroy();
    }

    public void doRefreshWork() {
        if (mService != null) {
            if (device != null) {
                try {
                    mService.disconnect(device.getAddress());
                    Lg.i(TAG, "refresh_disconnect_device_address = " + device.getAddress());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            index_checked = 0;
            count_device = 0;
            mListData.clear();
            isRefresh = true;
            scanLeDevice(true);
        } else {
            showShortToast(getString(R.string.bluetooth_service_break_restart));
        }
    }

}
