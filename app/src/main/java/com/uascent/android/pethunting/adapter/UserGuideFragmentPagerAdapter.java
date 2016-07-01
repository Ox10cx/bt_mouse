package com.uascent.android.pethunting.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class UserGuideFragmentPagerAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> list;
    FragmentManager fm;
    public UserGuideFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> list){
        super(fm);
        this.fm=fm;
        this.list=list;
    }
    public void clear() {
        for(int i = 0; i < list.size(); i ++) {
            fm.beginTransaction().remove(list.get(i)).commit();
        }

        list.clear();
        notifyDataSetChanged();
    }
    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
