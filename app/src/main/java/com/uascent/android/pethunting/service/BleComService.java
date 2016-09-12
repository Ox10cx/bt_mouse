package com.uascent.android.pethunting.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.uascent.android.pethunting.devices.BluetoothLeClass;
import com.uascent.android.pethunting.tools.Lg;
import com.uascent.android.pethunting.tools.Utils;
import com.uascent.android.pethunting.ui.ICallback;
import com.uascent.android.pethunting.ui.IService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Administrator on 16-3-15.
 */
public class BleComService extends Service {
    private static final String TAG = "BleComService";
//    private static final int SCAN_PERIOD = 10000;
//    private static final long LIVE_PERIOD = 1000 * 7;       //点击开始扫描后的10秒停止扫描

    private Map<String, BluetoothLeClass> mActiveDevices = new HashMap<String, BluetoothLeClass>();
    private Map<String, Integer> mScaningRssi = new HashMap<String, Integer>();
//    private Map<String, List<Integer>> mlivingRssiData = new HashMap<String, List<Integer>>();

    private RemoteCallbackList<ICallback> mCallbacks = new RemoteCallbackList<ICallback>();
    //    private boolean antiLostEnabled;
//    private final Object mSync = new Object();
    private HandlerThread mHandlerThread;
    private HandlerThread speedHandlerThread;
    private Handler speedHandler;
    private Handler myHandler;

    public class LocalBinder extends Binder {
        public BleComService getService() {
            return BleComService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Lg.i(TAG, "onBind");
        mHandlerThread = new HandlerThread("DirThread");
        speedHandlerThread = new HandlerThread("SpeedThread");
        mHandlerThread.start();
        speedHandlerThread.start();
        myHandler = new Handler(mHandlerThread.getLooper());
        speedHandler = new Handler(speedHandlerThread.getLooper());
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mCallbacks.kill();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private IService.Stub mBinder = new IService.Stub() {
        @Override
        public boolean initialize() throws RemoteException {
            return false;
        }

        @Override
        public boolean connect(String addr) throws RemoteException {
            Lg.i(TAG, "IService_connect->>>" + addr);
            return connectBtDevice(addr);
        }

        @Override
        public void disconnect(String addr) throws RemoteException {
            disconnectBtDevice(addr);
            Lg.i(TAG, "IService_disconnect->>>" + addr);
        }

        public void unregisterCallback(ICallback cb) {
            if (cb != null) {
                mCallbacks.unregister(cb);
            }
        }

        public void registerCallback(ICallback cb) {
            if (cb != null) {
                mCallbacks.register(cb);
            }
        }

        @Override
        public void controlMouse(final String addr, final int dir) throws RemoteException {
            speedHandler.post(new Runnable() {
                @Override
                public void run() {
                    bleControlMouse(addr, dir);
                }
            });

        }

        @Override
        public void controlMouseSpeed(final String addr, final int value, final int index) throws RemoteException {
            speedHandler.post(new Runnable() {
                @Override
                public void run() {
                    bleControlMouseSpeed(addr, value, index);
                }
            });
        }

        @Override
        public void readMouseRsp(final String addr) throws RemoteException {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Lg.i(TAG, "readMouseRsp_run");
                    bleDeviceCmdRsp(addr);
                }
            });
        }

        @Override
        public void setBatteryNoc(final String address) throws RemoteException {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    setBtDeviceBatteryNoc(address);
                }
            });
        }
    };


    private boolean bleControlMouse(String addr, int dir) {
        BluetoothLeClass device = mActiveDevices.get(addr);
        if (device != null) {
            Lg.i(TAG, "device != null");
            return device.mouseControl(dir);
        } else {
            Lg.i(TAG, "the device is null?");

            return false;
        }
    }

    private boolean bleControlMouseSpeed(String addr, int value, int dir) {
        BluetoothLeClass device = mActiveDevices.get(addr);
        if (device != null) {
            Lg.i(TAG, "device != null");
            return device.mouseControlSpeed(value, dir);
        } else {
            Lg.i(TAG, "the device is null?");

            return false;
        }
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    boolean connectBtDevice(String address) {
        boolean ret;

        BluetoothLeClass d = mActiveDevices.get(address);
        if (d != null) {
            Lg.i(TAG, "warning the address: " + address + " is not disconnected");
            d.disconnect();
            d.close();
            mActiveDevices.remove(address);
        }

        BluetoothLeClass device = new BluetoothLeClass(this);
        device.initialize();
        //连接Ble设备的监听事情
        device.setOnConnectListener(mOnConnectListener);

        //断开Ble设备的监听事情
        device.setOnDisconnectListener(mOnDisconnectListener);

        //发现服务端是否支持该服务
        device.setOnServiceDiscoverListener(mOnServiceDiscover);

        //收到BLE终端数据交互的事件
        device.setOnDataAvailableListener(mOnDataAvailable);

        //服务端信号监听
        device.setOnReadRemoteRssiListener(mOnReadRemoteRssiListener);
        mActiveDevices.put(address, device);
        ret = device.connect(address);
        if (ret) {
            Lg.i(TAG, "connect to " + address + " success");
        } else {
            Lg.i(TAG, "connect to " + address + " failed");
        }
        return ret;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    void disconnectBtDevice(String address) {
        BluetoothLeClass device = mActiveDevices.get(address);
        if (device != null) {
            Lg.i(TAG, "device != null");
            mActiveDevices.remove(address);
            device.disconnect();
            //lzg edit   不使用该链接之后，释放资源
            //device.close();
        } else {
            Lg.i(TAG, "device == null");
        }
    }

    /**
     * 命令回复通知
     *
     * @param addr
     * @return
     */
    private boolean bleDeviceCmdRsp(String addr) {
        BluetoothLeClass device = mActiveDevices.get(addr);
        if (device == null) {
            Lg.i(TAG, "the device is null?");
            return false;
        }
        Lg.i(TAG, "device != null");
        return device.getMouseRsp();
    }


    /**
     * 设置电量通知
     *
     * @param address
     */
    private void setBtDeviceBatteryNoc(String address) {
        BluetoothLeClass device = mActiveDevices.get(address);
        if (device != null) {
            Lg.i(TAG, "setCharacteristi_batterycNotification");
            device.setCharacteristicNotification(BluetoothLeClass.BATTERY_SERVICE_UUID, BluetoothLeClass.BATTERY_FUNC_UUID, true);
        }
    }

    private BluetoothLeClass.OnDisconnectListener mOnDisconnectListener = new BluetoothLeClass.OnDisconnectListener() {
        @Override
        public void onDisconnect(BluetoothGatt gatt) {
            BluetoothDevice device = gatt.getDevice();
            BluetoothLeClass leDevice = mActiveDevices.get(device.getAddress());
            if (leDevice != null) {
                mActiveDevices.remove(device.getAddress());
            } else {
                Lg.i("hjq", "remove address = " + device.getAddress() + " is null");
            }

            synchronized (mCallbacks) {
                int n = mCallbacks.beginBroadcast();
                try {
                    int i;
                    for (i = 0; i < n; i++) {
                        mCallbacks.getBroadcastItem(i).onDisconnect(gatt.getDevice().getAddress());
                    }
                } catch (RemoteException e) {
                    Lg.i(TAG, "remote call exception->>>" + e);
                }
                mCallbacks.finishBroadcast();
            }
        }
    };

    private BluetoothLeClass.OnReadRemoteRssiListener mOnReadRemoteRssiListener = new BluetoothLeClass.OnReadRemoteRssiListener() {
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            BluetoothDevice device = gatt.getDevice();
            Lg.i(TAG, "read remote " + device.getAddress() + " rssi = " + rssi + " status = " + status);
            synchronized (mScaningRssi) {
                mScaningRssi.put(gatt.getDevice().getAddress(), rssi);
                mScaningRssi.notify();
            }
        }
    };

    private BluetoothLeClass.OnConnectListener mOnConnectListener = new BluetoothLeClass.OnConnectListener() {
        @Override
        public void onConnect(BluetoothGatt gatt) {
            synchronized (mCallbacks) {
                int n = mCallbacks.beginBroadcast();
                try {
                    int i;
                    for (i = 0; i < n; i++) {
                        mCallbacks.getBroadcastItem(i).onConnect(gatt.getDevice().getAddress());
                    }
                } catch (RemoteException e) {
                    Lg.i(TAG, "remote call exception->>>" + e);
                }
                mCallbacks.finishBroadcast();
            }
        }

    };

    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {
        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            Lg.i(TAG, "onServiceDiscover");
            BluetoothDevice device = gatt.getDevice();
            BluetoothLeClass leDevice = mActiveDevices.get(device.getAddress());
            if (leDevice != null) {
                Lg.i(TAG, "leDevice != null");
//                displayGattServices(leDevice.getSupportedGattServices());
                boolean supported = checkMouseService(leDevice.getSupportedGattServices());
                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    try {
                        int i;
                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onMouseServiceDiscovery(device.getAddress(), supported);
                        }
                    } catch (RemoteException e) {
                        Lg.i(TAG, "remote call exception->>>" + e);
                    }
                    mCallbacks.finishBroadcast();
                }
            } else {
                Lg.i(TAG, "address = " + device.getAddress() + " is null");
            }
        }
    };

    boolean checkMouseService(List<BluetoothGattService> gattServices) {
        boolean bWriteFn = false;
        if (gattServices == null) {
            return false;
        }
        Lg.e(TAG, "checkCon_MouseService" + "    gattServices length:" + gattServices.size());
        for (BluetoothGattService gattService : gattServices) {
            final UUID serviceUUID = gattService.getUuid();
            Lg.i(TAG, "-->service uuid:" + gattService.getUuid());
            if (!serviceUUID.equals(BluetoothLeClass.MOUSE_SERVICE_UUID)) {
                continue;
            }
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Lg.i(TAG, "---->char uuid:" + gattCharacteristic.getUuid());
                if (!bWriteFn) {
                    bWriteFn = gattCharacteristic.getUuid().equals(BluetoothLeClass.MOUSE_WRITE_FUNC_UUID);
                }
                if (bWriteFn) {
                    return true;
                }
            }
        }
        return false;
    }


//    boolean checkMouseService(List<BluetoothGattService> gattServices) {
//        boolean bWriteFn = false;
////        boolean bReadFn = false;
//        if (gattServices == null) {
//            return false;
//        }
//        Lg.e(TAG, "checkCon_MouseService" + "    gattServices length:" + gattServices.size());
//
//        for (BluetoothGattService gattService : gattServices) {
//            final UUID serviceUUID = gattService.getUuid();
//            Lg.i(TAG, "-->service uuid:" + gattService.getUuid());
//            if (!serviceUUID.equals(BluetoothLeClass.MOUSE_SERVICE_UUID)) {
//                continue;
//            }
//            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                Lg.i(TAG, "---->char uuid:" + gattCharacteristic.getUuid());
//                if (!bWriteFn) {
//                    bWriteFn = gattCharacteristic.getUuid().equals(BluetoothLeClass.MOUSE_WRITE_FUNC_UUID);
//                }
//                if (bWriteFn) {
//                    return true;
//                }
//
////                if (!bReadFn) {
////                    bReadFn = gattCharacteristic.getUuid().equals(BluetoothLeClass.MOUSE_READ_FUNC_UUID);
////                }
////                if (bWriteFn && bReadFn) {
////                    return true;
////                }
//            }
//        }
//
//        return false;
//    }

    /**
     * 收到BLE终端数据交互的事件
     */
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new BluetoothLeClass.OnDataAvailableListener() {

        /**
         * BLE终端数据被读的事件
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Lg.i(TAG, "onCharRead " + gatt.getDevice().getName()
                        + " read "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + Utils.bytesToHexString(characteristic.getValue()));

                synchronized (mCallbacks) {
                    int n = mCallbacks.beginBroadcast();
                    try {
                        int i;
                        for (i = 0; i < n; i++) {
                            mCallbacks.getBroadcastItem(i).onRead(gatt.getDevice().getAddress(), characteristic.getValue());
                        }
                    } catch (RemoteException e) {
                        Lg.i(TAG, "remote call exception->>>" + e);
                    }
                    mCallbacks.finishBroadcast();
                }
            }
        }

        /**
         * 收到BLE终端写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();

            Lg.i(TAG, "onCharWrite " + gatt.getDevice().getName()
                    + " write "
                    + characteristic.getUuid().toString()
                    + " -> "
                    + Utils.bytesToHexString(value));

            synchronized (mCallbacks) {
                int n = mCallbacks.beginBroadcast();
                try {
                    int i;
                    for (i = 0; i < n; i++) {
                        mCallbacks.getBroadcastItem(i).onWrite(gatt.getDevice().getAddress(), characteristic.getValue());
                    }
                } catch (RemoteException e) {
                    Lg.i(TAG, "remote call exception->>" + e);
                }
                mCallbacks.finishBroadcast();
            }
        }
    };

//    private void displayGattServices(List<BluetoothGattService> gattServices) {
//        if (gattServices == null) {
//            return;
//        }
//
//        for (BluetoothGattService gattService : gattServices) {
//            //-----Service的字段信息-----//
//            int type = gattService.getType();
//            final UUID serviceUUID = gattService.getUuid();
//            Lg.i(TAG, "-->service type:" + Utils.getServiceType(type));
//            Lg.i(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
//            Lg.i(TAG, "-->service uuid:" + gattService.getUuid());
//
//            //-----Characteristics的字段信息-----//
//            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                Lg.i(TAG, "---->char uuid:" + gattCharacteristic.getUuid());
//
//                int permission = gattCharacteristic.getPermissions();
//                Lg.i(TAG, "---->char permission:" + Utils.getCharPermission(permission));
//
//                int property = gattCharacteristic.getProperties();
//                Lg.i(TAG, "---->char property:" + Utils.getCharPropertie(property));
//
//                byte[] data = gattCharacteristic.getValue();
//                if (data != null && data.length > 0) {
//                    Lg.i(TAG, "---->char value:" + new String(data));
//                }
//
//                //-----Descriptors的字段信息-----//
//                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
//                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
//                    Lg.i(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
//                    int descPermission = gattDescriptor.getPermissions();
//                    Lg.i(TAG, "-------->desc permission:" + Utils.getDescPermission(descPermission));
//
//                    byte[] desData = gattDescriptor.getValue();
//                    if (desData != null && desData.length > 0) {
//                        Lg.i(TAG, "-------->desc value:" + new String(desData));
//                    }
//                }
//            }
//        }
//    }


    // 15秒获取一次连接的rssi值并进行判断，是否掉线了。
//    void setBleAntiLost(boolean enable) {
//
//        Lg.i("hjq", "set antilost enable = " + enable);
//        if (antiLostEnabled == enable) {
//            return;
//        }
//
//        antiLostEnabled = enable;
//
//        if (enable) {
//            Thread th = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (antiLostEnabled) {
//                        mScaningRssi.clear();
//                        boolean bleok = false;
//                        for (String k : mActiveDevices.keySet()) {
//                            BluetoothLeClass d = mActiveDevices.get(k);
//
//                            bleok = (d != null && d.getBleStatus() == BluetoothLeClass.BLE_STATE_CONNECTED);
//                            if (bleok) {
//                                d.readRemoteRssi();
//                                // 必须在此处同步回调函数，否则蓝牙协议栈会出错
//                                synchronized (mScaningRssi) {
//                                    try {
//                                        mScaningRssi.wait();
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//                        }
//
//                        Lg.i("hjq", "ble status = " + bleok);
//                        if (!bleok) {
//                            try {
//                                Thread.sleep(6000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            continue;
//                        }
//
//                        // 判断蓝牙设备是否丢失
//                        for (String k : mlivingRssiData.keySet()) {
//                            int val = -130;    // 蓝牙设备已经丢失的信号值
//                            if (mScaningRssi.get(k) != null) {
//                                val = mScaningRssi.get(k);
//                                mScaningRssi.remove(k);
//                            }
//                            List<Integer> list = mlivingRssiData.get(k);
//                            if (list.size() > 20) {
//                                list.remove(0);
//                            }
//                            list.add(val);
//                        }
//
//                        for (String key : mScaningRssi.keySet()) {
//                            List<Integer> list = new ArrayList<Integer>();
//                            list.add(mScaningRssi.get(key));
//
//                            mlivingRssiData.put(key, list);
//                        }
//
//                        Map<String, Double> rssidata = PreventAntiLostCore.getDeviceState(mlivingRssiData);
//                        for (String key : rssidata.keySet()) {
//                            int rssi = rssidata.get(key).intValue();
//                            int pos;
//
//                            Lg.i("hjq", "rssi = " + rssi);
//                            if (rssi < -100) {
//                                pos = BtDevice.LOST;
//                            } else {
//                                pos = BtDevice.OK;
//                            }
//
//                            synchronized (mActiveDevices) {
//                                if (mActiveDevices.get(key) == null) {
//                                    continue;
//                                }
//                            }
//
//                            synchronized (mCallbacks) {
//                                int n = mCallbacks.beginBroadcast();
//                                try {
//                                    int i;
//                                    for (i = 0; i < n; i++) {
//                                        mCallbacks.getBroadcastItem(i).onPositionChanged(key, pos);
//                                        mCallbacks.getBroadcastItem(i).onSignalChanged(key, rssi);
//                                    }
//                                } catch (RemoteException e) {
//                                    Lg.i(TAG, "remote call exception->>>" + e);
//                                }
//                                mCallbacks.finishBroadcast();
//                            }
//                        }
//
//                        try {
//                            Thread.sleep(6000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
//
//            th.start();
//        }
//    }
}
