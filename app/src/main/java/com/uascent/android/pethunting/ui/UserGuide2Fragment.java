package com.uascent.android.pethunting.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.uascent.android.pethunting.R;

public class UserGuide2Fragment extends Fragment implements View.OnClickListener {
    private Button bt_back;
    private Button bt_menu;
    private Button bt_next;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_userguide5, container, false);
//        bt_back = (Button) rootView.findViewById(R.id.bt_back);
//        bt_back.setOnClickListener(this);
//        bt_menu = (Button) rootView.findViewById(R.id.bt_menu);
//        bt_menu.setOnClickListener(this);
//        bt_next = (Button) rootView.findViewById(R.id.bt_next);
//        bt_next.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.bt_menu:
                intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_back:
                intent = new Intent(getActivity(), UserGuide1Fragment.class);
                startActivity(intent);
                break;
            case R.id.bt_next:
                intent = new Intent(getActivity(), UserGuide3Fragment.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
