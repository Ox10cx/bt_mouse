package com.uascent.android.pethunting.devices;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.uascent.android.pethunting.tools.Lg;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeClass {
    private final static String TAG = "BluetoothLeClass";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    protected BluetoothGatt mBluetoothGatt;

    /**
     * 初始状态或者断开连接状态
     */
    public static final int BLE_STATE_INIT = 0;

    /**
     * 连上Ble状态
     */
    public static final int BLE_STATE_CONNECTED = 1;

    /**
     * 连上Ble之后，没有发现服务，迅速断开
     */
    public static final int BLE_STATE_ERROR = 3;

    /**
     * 连上Ble并且发现服务，没有断开
     */
//    public static final int BLE_STATE_CON_SERVICE = 5;

    public static int mBleStatus = 0;


    //电量服务
    public static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_FUNC_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    //玩具老鼠服务
    public static final UUID MOUSE_WRITE_FUNC_UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    //    public static final UUID MOUSE_READ_FUNC_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public static final UUID MOUSE_SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public static final UUID MOUSE_READCMDRSP_FUNC_UUID = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");

    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int MOUSE_STOP = 0;
    public static final int MOUSE_UP = 1;
    public static final int MOUSE_DOWN = 2;
    public static final int MOUSE_LEFT = 3;
    public static final int MOUSE_RIGHT = 4;
    public static final byte SPEED_ID = 5;
//    private static boolean isRealWrite = false;
//    private Timer timer = null;
//    private static boolean isFristWritePerCmd = false;

    public interface OnConnectListener {
        public void onConnect(BluetoothGatt gatt);
    }

    public interface OnDisconnectListener {
        public void onDisconnect(BluetoothGatt gatt);
    }

    public interface OnServiceDiscoverListener {
        public void onServiceDiscover(BluetoothGatt gatt);
    }

    public interface OnDataAvailableListener {
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status);

        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic);
    }

    public interface OnReadRemoteRssiListener {
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);
    }

    private OnConnectListener mOnConnectListener;
    private OnDisconnectListener mOnDisconnectListener;
    private OnServiceDiscoverListener mOnServiceDiscoverListener;
    private OnDataAvailableListener mOnDataAvailableListener;
    private OnReadRemoteRssiListener mOnReadRemoteRssiListener;
    private Context mContext;

    public void setOnConnectListener(OnConnectListener l) {
        mOnConnectListener = l;
    }

    public void setOnDisconnectListener(OnDisconnectListener l) {
        mOnDisconnectListener = l;
    }

    public void setOnServiceDiscoverListener(OnServiceDiscoverListener l) {
        mOnServiceDiscoverListener = l;
    }

    public void setOnDataAvailableListener(OnDataAvailableListener l) {
        mOnDataAvailableListener = l;
    }

    public void setOnReadRemoteRssiListener(OnReadRemoteRssiListener l) {
        mOnReadRemoteRssiListener = l;
    }

    public BluetoothLeClass(Context c) {
        mContext = c;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Lg.i(TAG, "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (mOnConnectListener != null)
                    mOnConnectListener.onConnect(gatt);
                mBleStatus = BLE_STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                if (mBluetoothGatt != null) {
                    boolean ret = mBluetoothGatt.discoverServices();
                    Lg.i(TAG, "Attempting to start service discovery:" + ret);
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mOnDisconnectListener != null)
                    mOnDisconnectListener.onDisconnect(gatt);
                Lg.i(TAG, "Disconnected from GATT server.");
                mBleStatus = BLE_STATE_INIT;
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                }
                mBluetoothGatt = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Lg.i(TAG, "onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS && mOnServiceDiscoverListener != null) {
                mOnServiceDiscoverListener.onServiceDiscover(gatt);
                mBleStatus = BLE_STATE_CONNECTED;
//                mBleStatus = BLE_STATE_CON_SERVICE;
            } else {
                Lg.i(TAG, "onServicesDiscovered received: " + status);
                mBleStatus = BLE_STATE_ERROR;
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Lg.i(TAG, "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Lg.i(TAG, "onCharacteristicRead" + characteristic.getValue()[0]);
                if (mOnDataAvailableListener != null && characteristic.getUuid().equals(BluetoothLeClass.MOUSE_READCMDRSP_FUNC_UUID)) {
                    mOnDataAvailableListener.onCharacteristicRead(gatt, characteristic, status);
                }
            }
        }

//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            super.onCharacteristicWrite(gatt, characteristic, status);
//            Lg.i(TAG, "onCharacteristicWrite");
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                isRealWrite = true;
//                timer.cancel();
//                Lg.i(TAG, "onCharacteristicWrite_ok" + characteristic.getValue()[0]);
//                getMouseRsp();
//            }
//        }

        /**
         * 如果对一个特性启用通知,当远程蓝牙设备特性发送变化，回调函数onCharacteristicChanged( ))被触发。
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Lg.i(TAG, "onCharacteristicChanged");
            if (mOnDataAvailableListener != null && characteristic.getUuid().equals(BluetoothLeClass.BATTERY_FUNC_UUID)) {
                Lg.i(TAG, "onCharacteristicChanged_onCharacteristicWrite");
                mOnDataAvailableListener.onCharacteristicWrite(gatt, characteristic);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Lg.i(TAG, "onReadRemoteRssi");
            if (mOnReadRemoteRssiListener != null) {
                mOnReadRemoteRssiListener.onReadRemoteRssi(gatt, rssi, status);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Lg.i(TAG, "onReliableWriteCompleted");
        }
    };

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Lg.i(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
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
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        try {
            mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
            Log.d(TAG, "Trying to create a new connection.");
            mBluetoothDeviceAddress = address;
        } catch (Exception e) {
            Log.d(TAG, "device.connectGatt fail");
            return false;
        }
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        //     mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readRemoteRssi() {
        mBluetoothGatt.readRemoteRssi();
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        return mBluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * Enables or disables notification on a give characteristic.
     */
    public boolean setCharacteristicNotification(UUID serviceUuid, UUID characteristicUuid,
                                                 boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(serviceUuid);
        if (gattService != null) {
            BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(characteristicUuid);
            Log.e("hjq", "char = " + characteristic);
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            if (descriptor != null) {
                Lg.i(TAG, "descriptor!=null");
                descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
            } else {
                Lg.i(TAG, "descriptor==null");
            }
            mBluetoothGatt.writeDescriptor(descriptor);
        } else {
            Log.e("hjq", "service id = " + serviceUuid + " is not support!");
            return false;
        }

        return true;
    }

    public synchronized void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//        isRealWrite = false;
        boolean var = false;
        if (mBluetoothGatt != null) {
            var = mBluetoothGatt.writeCharacteristic(characteristic);
        }
        Lg.e(TAG, "writeCharacteristic->>>>" + var);
        //如果写入蓝牙设备失败(可能是上一次的命令还没有得到响应，等待轮训10次发送)
        int var_count = 0;
//        if (!var) {
        while (!var) {
            try {
                Thread.sleep(150 + 150 * var_count);
                Lg.i(TAG, "sleep");
                if (mBluetoothGatt != null) {
                    var = mBluetoothGatt.writeCharacteristic(characteristic);
                }
                Lg.e(TAG, "writeCharacteristic-repeat_var_>>>>" + var);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            var_count++;
            if (var_count == 10) {
                mouseControl(MOUSE_STOP);
                return;
            }
        }
/*       }else {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
//                Lg.i(TAG,"pretime:"+System.currentTimeMillis());
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mouseControl(MOUSE_STOP);
                    Lg.i(TAG, "timer.schedule->>>run");
                }
            }, 1500);
        }*/

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }


    /**
     * 检查是否为连接状态
     *
     * @return
     */
    public boolean checkBleStatus() {
        Lg.i(TAG, "check ble status = " + mBleStatus);
        return mBleStatus == BLE_STATE_CONNECTED;
    }

    /**
     * 获得发送命令的回复
     *
     * @return
     */
    public boolean getMouseRsp() {
        Lg.i(TAG, "getMouseRsp()");
        if (!checkBleStatus()) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = null;
        if (mBluetoothGatt != null && mBluetoothGatt.getService(MOUSE_SERVICE_UUID) != null) {
            characteristic = mBluetoothGatt.getService(MOUSE_SERVICE_UUID).getCharacteristic(MOUSE_READCMDRSP_FUNC_UUID);
        }
        if (characteristic == null) {
            Lg.i(TAG, "not support the readback service?");
            return false;
        }
        return readCharacteristic(characteristic);
    }

    /**
     * 控制老鼠的方向
     *
     * @param direction
     * @return
     */
    public boolean mouseControl(int direction) {
        Lg.i(TAG, "mouseControl");
        if (!checkBleStatus()) {
            return false;
        }
        BluetoothGattService service = mBluetoothGatt.getService(MOUSE_SERVICE_UUID);
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(MOUSE_SERVICE_UUID).getCharacteristic(MOUSE_WRITE_FUNC_UUID);
        if (characteristic != null) {
            characteristic.setValue(new byte[]{(byte) direction});
            //往蓝牙模块写入数据
            Lg.i(TAG, "value:" + direction);
            writeCharacteristic(characteristic);
        }
        return true;
    }

    /**
     * 控制老鼠的加速度
     *
     * @param speed
     * @param direction
     * @return
     */
    public boolean mouseControlSpeed(int speed, int direction) {
        Lg.e(TAG, "mouseControlSpeed");
        if (!checkBleStatus()) {
            return false;
        }
        BluetoothGattService service = mBluetoothGatt.getService(MOUSE_SERVICE_UUID);
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(MOUSE_SERVICE_UUID).getCharacteristic(MOUSE_WRITE_FUNC_UUID);
        if (characteristic != null) {
            characteristic.setValue(new byte[]{SPEED_ID, (byte) speed, (byte) direction});
            //往蓝牙模块写入数据
            Lg.i(TAG, "speed:" + speed + "  " + "value:" + direction);
            writeCharacteristic(characteristic);
        }
        return true;
    }

}

