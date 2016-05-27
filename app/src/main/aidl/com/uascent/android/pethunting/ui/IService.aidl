// IService.aidl
package com.uascent.android.pethunting.ui;

// Declare any non-default types here with import statements
import com.uascent.android.pethunting.ui.ICallback;
interface IService {
    void registerCallback(ICallback cb);
    void unregisterCallback(ICallback cb);

    boolean initialize();
    boolean connect(String addr);
    void disconnect(String addr);

    void turnOnImmediateAlert(String addr);
    void turnOffImmediateAlert(String addr);

    void setAntiLost(boolean enable);
}
