package com.uascent.android.pethunting.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.uascent.android.pethunting.R;

public class UserGuide5Fragment extends BaseFragment {
    private Button bt_back;
    private Button bt_menu;
    private Button bt_next;
    private int index = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_userguide5, container, false);
        bt_back = (Button) rootView.findViewById(R.id.bt_back);
        bt_back.setOnClickListener(this);
        bt_menu = (Button) rootView.findViewById(R.id.bt_menu);
        bt_menu.setOnClickListener(this);
        bt_next = (Button) rootView.findViewById(R.id.bt_next);
        bt_next.setOnClickListener(this);
        return rootView;
    }


    @Override
    public void onClick(View v) {
        if (buntonClickListener != null) {
            switch (v.getId()) {
                case R.id.bt_menu:
                    index = 6;
                    break;
                case R.id.bt_back:
                    index = 3;
                    break;
                case R.id.bt_next:
                    index = 0;
                    break;
                default:
                    break;
            }
            buntonClickListener.onButtonClick(index);
        }
    }
}
