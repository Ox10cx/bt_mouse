// ICallback.aidl
package com.uascent.android.pethunting.ui;

// Declare any non-default types here with import statements
interface ICallback {
       void onConnect(String address);
       void onDisconnect(String address);
       boolean onRead(String address, in byte[] val);
       boolean onWrite(String address, out byte[] val);
       void onMouseServiceDiscovery(String address, boolean support);

       //蓝牙设备信号改变
//       void onSignalChanged(String address, int rssi);
//       void onPositionChanged(String address, int position);
//       void onAlertServiceDiscovery(String address, boolean support);


}
