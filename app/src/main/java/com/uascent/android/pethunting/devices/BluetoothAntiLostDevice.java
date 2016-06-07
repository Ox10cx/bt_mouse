package com.uascent.android.pethunting.devices;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.uascent.android.pethunting.tools.Lg;

import java.util.UUID;

/**
 * Created by Administrator on 16-3-9.
 */
public class BluetoothAntiLostDevice extends BluetoothLeClass {
    private static final String TAG = "BluetoothAntiLostDevice";
    public static final UUID POWER_SERVICE_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID POWER_FUNC_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");

    public static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_FUNC_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    public static final UUID KEY_FUNC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID KEY_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    public static final UUID ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID ALERT_FUNC_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    public static final UUID LOSS_SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
    public static final UUID LOSS_FUNC_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    public static final UUID MOUSE_WRITE_FUNC_UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    public static final UUID MOUSE_READ_FUNC_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public static final UUID MOUSE_SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");

    public static final int MOUSE_STOP = 0;
    public static final int MOUSE_UP = 1;
    public static final int MOUSE_DOWN = 2;
    public static final int MOUSE_LEFT = 3;
    public static final int MOUSE_RIGHT = 4;


    public BluetoothAntiLostDevice(Context c) {
        super(c);
    }

    public boolean checkBleStatus() {
        Log.e(TAG, "check ble status = " + mBleStatus);
        return mBleStatus == BLE_STATE_CONNECTED;
    }

    public int getBleStatus() {
        return mBleStatus;
    }

    public void getLinkLossSetting() {
        Log.e(TAG, "getLinkLossSetting()");
        if (!checkBleStatus()) {
            return;
        }
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(LOSS_SERVICE_UUID).getCharacteristic(LOSS_FUNC_UUID);
        if (characteristic == null) {
            Log.e(TAG, "not support the loss service?");
            return;
        }

        readCharacteristic(characteristic);
    }

    public void setLinkLossSetting(byte val) {
        Log.e(TAG, "setLinkLossSetting()");
        if (!checkBleStatus()) {
            return;
        }
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(LOSS_SERVICE_UUID).getCharacteristic(LOSS_FUNC_UUID);
        if (characteristic == null) {
            Log.e(TAG, "not support the loss service?");
            return;
        }

        if (characteristic != null) {
            characteristic.setValue(new byte[]{val});
            //往蓝牙模块写入数据
            writeCharacteristic(characteristic);
        }
    }

    public void getBatteryLevel() {
        Log.e(TAG, "getBatteryLevel()");
        if (!checkBleStatus()) {
            return;
        }
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(BATTERY_SERVICE_UUID).getCharacteristic(BATTERY_FUNC_UUID);
        if (characteristic == null) {
            Log.e(TAG, "not support the loss service?");
            return;
        }

        readCharacteristic(characteristic);
    }

    public void enableKeyReport(boolean on) {
        Log.e(TAG, "enable notification");

        if (!checkBleStatus()) {
            return;
        }

        //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
        setCharacteristicNotification(KEY_SERVICE_UUID, KEY_FUNC_UUID, on);

        BluetoothGattService service = mBluetoothGatt.getService(KEY_SERVICE_UUID);
        if (service == null) {
            return;
        }

        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(KEY_SERVICE_UUID).getCharacteristic(KEY_FUNC_UUID);

        if (characteristic != null) {
            characteristic.setValue("send data->");
            //往蓝牙模块写入数据
            writeCharacteristic(characteristic);
        }
    }

    public boolean getMouseRsp() {
        Log.e(TAG, "getMouseRsp()");
        if (!checkBleStatus()) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(MOUSE_SERVICE_UUID).getCharacteristic(MOUSE_READ_FUNC_UUID);
        if (characteristic == null) {
            Log.e(TAG, "not support the readback service?");
            return false;
        }

        return readCharacteristic(characteristic);
    }

    public boolean mouseControl(int direction) {
        Log.e(TAG, "mouseControl");

        if (!checkBleStatus()) {
            return false;
        }

////        //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
//      if (!setCharacteristicNotification(MOUSE_SERVICE_UUID, MOUSE_READ_FUNC_UUID, true)) {
//            Log.e(TAG, "setCharacteristicNotification() failed!");
//            return false;
//        }

        BluetoothGattService service = mBluetoothGatt.getService(MOUSE_SERVICE_UUID);
        if (service == null) {
            return false;
        }

        BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(MOUSE_SERVICE_UUID).getCharacteristic(MOUSE_WRITE_FUNC_UUID);
        if (characteristic != null) {
            characteristic.setValue( new byte[]{ (byte)direction });
            //往蓝牙模块写入数据
            writeCharacteristic(characteristic);
        }

        return  true;
    }

    public void setImmediateAlert(int val) {
        Lg.i(TAG, "setImmediateAlert() value = " + val);
        if (!checkBleStatus()) {
            return;
        }

        BluetoothGattService service = mBluetoothGatt.getService(ALERT_SERVICE_UUID);

        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(ALERT_FUNC_UUID);
            if (characteristic == null) {
                Log.e(TAG, "not support the immediate alert funcion?");
                return;
            }

            if (characteristic != null) {
                characteristic.setValue(new byte[]{(byte) val});
                Log.e(TAG, "writeCharacteristic:" + val);
                //往蓝牙模块写入数据
                writeCharacteristic(characteristic);
            }
        } else {
            Lg.i(TAG, "characteristic= null");
            Log.e(TAG, "not support the immediate alert service?");
        }

    }

    public void turnOnImmediateAlert(int index) {
        Log.i(TAG, "turnOnImmediateAlert:" + index);
        setImmediateAlert(index);
    }


    public void turnOffImmediateAlert(int index) {
        Log.i(TAG, "turnOffImmediateAlert:");
        setSpeedCmd(index);
    }

    /**
     * 设置速度命令
     *
     * @param val
     */
    public boolean setSpeedCmd(int val) {
        Lg.i(TAG, "setSpeedCmd value = " + val);
        if (!checkBleStatus()) {
            return false;
        }

        BluetoothGattService service = mBluetoothGatt.getService(MOUSE_SERVICE_UUID);

        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(MOUSE_WRITE_FUNC_UUID);
            if (characteristic == null) {
                Log.e(TAG, "not support the immediate alert funcion?");
                return false;
            }

            if (characteristic != null) {
                characteristic.setValue(new byte[]{(byte) val});
                Log.e(TAG, "writeCharacteristic:" + val);
                //往蓝牙模块写入数据
                writeCharacteristic(characteristic);
            }
        } else {
            Lg.i(TAG, "characteristic= null");
            Log.e(TAG, "not support the immediate alert service?");
            return false;
        }

        return true;
    }

}

