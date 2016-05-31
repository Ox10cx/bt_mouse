package com.uascent.android.pethunting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uascent.android.pethunting.R;
import com.uascent.android.pethunting.model.BtDevice;

import java.util.ArrayList;

/**
 * Created by Administrator on 16-3-7.
 */
public class DeviceListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<BtDevice> data;

    public DeviceListAdapter(Context context, ArrayList<BtDevice> list) {
        this.context = context;
        data = list;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holderView;
        if (convertView == null) {
            holderView = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_device, null);
            holderView.iv_device = (ImageView) convertView.findViewById(R.id.iv_device);
            holderView.device_name = (TextView) convertView.findViewById(R.id.device_name);
            holderView.iv_device_check = (ImageView) convertView.findViewById(R.id.iv_device_check);
            convertView.setTag(holderView);
        } else {
            holderView = (ViewHolder) convertView.getTag();
        }
        holderView.device_name.setText(data.get(position).getName());
        if (!data.get(position).isChecked()) {
            holderView.iv_device_check.setImageResource(R.drawable.device_not_checked);
        } else {
            holderView.iv_device_check.setImageResource(R.drawable.device_checked);
        }
        return convertView;
    }


    private final static class ViewHolder {
        public ImageView iv_device;
        public TextView device_name;
        public ImageView iv_device_check;
    }


}
