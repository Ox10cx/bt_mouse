package com.uascent.android.pethunting.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }


    @Override
    public void onClick(View v) {

    }

    public static BuntonClickListener buntonClickListener;

    public interface BuntonClickListener {
        void onButtonClick(int index);
    }

    //设置回调接口
    public static void setBuntonClickListener(BuntonClickListener temBuntonClickListener) {
        buntonClickListener = temBuntonClickListener;
    }
}
