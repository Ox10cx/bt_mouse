package com.uascent.android.pethunting.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.uascent.android.pethunting.MyApplication;
import com.uascent.android.pethunting.R;
import com.uascent.android.pethunting.myviews.LoadDialog;
import com.uascent.android.pethunting.tools.StatusBarUtil;

public class BaseActivity extends FragmentActivity {
    private final static String TAG = "BaseActivity";
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBar();
        MyApplication.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getInstance().removeActivity(this);
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));
    }

    public void showLongToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public void showShortToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void showLoadingDialog() {
        dialog = LoadDialog.createLoadingDialog(this, "正在加载中..");
        dialog.setCancelable(true);
        dialog.show();
    }

    public void showLoadingDialog(String msg) {
        dialog = LoadDialog.createLoadingDialog(this, msg);
        dialog.setCancelable(true);
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                onDialogCancel();
            }
        });
    }

    protected void onDialogCancel() {
        Log.e("hjq", "onDialogCancel called");
    }

    public boolean closeLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();

            return true;
        } else {
            return false;
        }
    }
}
