package com.yd.yunapp.gamebox;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.kuaipan.game.demo.R;

import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.GameInfo;

public class HorizontalGameAdapter extends RecyclerView.Adapter<HorizontalGameAdapter.AppViewHolder> {

    private static int MIN_SIZE = 15;
    private boolean isTimeOut = false;
    private int mRealSize;

    public interface OnItemClickListener {
        void onItemClick(View view, int pos);
    }

    List<GameInfo> mCloudAppList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mListener;

    public HorizontalGameAdapter(Context ctx) {
        mContext = ctx;
    }

    public void refresh(List<GameInfo> data) {
        if (data != null) {
            mCloudAppList.clear();
            mCloudAppList.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public Object getItem(int pos) {
        return mCloudAppList.get(pos);
    }

    public void setTimeOutMode(boolean timeOut) {
        isTimeOut = timeOut;
    }

    @Override
    public HorizontalGameAdapter.AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_game_item,
                parent, false));
    }

    @Override
    public void onBindViewHolder(HorizontalGameAdapter.AppViewHolder holder, int position) {
        final int pos = holder.getAdapterPosition();
        if (pos < mRealSize) {
            GameInfo app = mCloudAppList.get(pos);
            holder.mAppName.setText(app.name);
            Glide.with(holder.mIcon).load(app.iconUrl)
                    .transform(new CenterCrop(), new RoundedCorners(36))
                    .into(holder.mIcon);

            if (!isTimeOut) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            mListener.onItemClick(view, pos);
                        }
                    }
                });
            }
        } else {
            holder.mIcon.setImageResource(android.R.color.transparent);
            holder.mAppName.setText("");
            holder.itemView.setOnClickListener(null);
        }
        if (pos % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),
                    R.color.common_10_percent_transparent));
        }
    }

    @Override
    public int getItemCount() {
        mRealSize = mCloudAppList.size();
        // mRealSize = FIX_SIZE;
        return mRealSize < MIN_SIZE ? MIN_SIZE : mRealSize;
    }

    class AppViewHolder extends RecyclerView.ViewHolder {

        ImageView mIcon;
        TextView mAppName;

        public AppViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.app_icon);
            mAppName = itemView.findViewById(R.id.app_name);
        }
    }
}
