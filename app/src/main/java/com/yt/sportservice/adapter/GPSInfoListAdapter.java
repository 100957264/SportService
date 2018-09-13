package com.yt.sportservice.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.yt.sportservice.R;

import java.util.List;

/**
 * Created by B415 on 2018/3/5.
 */

public class GPSInfoListAdapter extends RecyclerView.Adapter<GPSInfoListAdapter.ViewHolder> {

    private Context mContext;
    private List<String> locationList;
    private LayoutInflater inflater;

    public GPSInfoListAdapter(Context context, List<String> locationList) {
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        this.locationList = locationList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.gps_info_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
         holder.gpsInfo.setText(locationList.get(position));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView  gpsInfo;
        public ViewHolder(View itemView) {
            super(itemView);
            this.gpsInfo = itemView.findViewById(R.id.gps_info);
        }
    }
}
