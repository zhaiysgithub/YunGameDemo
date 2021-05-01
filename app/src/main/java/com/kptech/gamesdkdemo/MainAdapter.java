package com.kptech.gamesdkdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.GameInfo;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private final List<GameInfo> gameinfos = new ArrayList<>();
    private final Context context;
    private final LayoutInflater mInflater;
    private OnItemCallback mCallback;

    public MainAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    public void addAll(List<GameInfo> games) {
        gameinfos.clear();
        add(games);
    }

    public void add(List<GameInfo> games) {
        gameinfos.addAll(games);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemCallback callback){
        this.mCallback = callback;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  view = mInflater.inflate(R.layout.item_main,parent,false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        GameInfo gameInfo = gameinfos.get(position);
        String icon = gameInfo.iconUrl;
        if (icon != null && !icon.isEmpty()){
            Picasso.with(context).load(icon).into(holder.mGame_icon);
        }else{
            Picasso.with(context).load(R.mipmap.ic_launcher).into(holder.mGame_icon);
        }
        String name = gameInfo.name;
        holder.mGame_name.setText(name);
        holder.mPlay_btn.setText("开始试玩");
        holder.mPlay_btn.setOnClickListener(v -> {
            if (mCallback != null){
                mCallback.onItemPlayClick(gameInfo);
            }
        });
        View itemView = holder.itemView;
        if (itemView != null){
            itemView.setOnClickListener(v -> {
                if (mCallback != null){
                    mCallback.onItemClickListener(gameInfo);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return gameinfos.size();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mGame_icon;
        private final TextView mGame_name;
        private final Button mPlay_btn;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);

            mGame_icon = itemView.findViewById(R.id.game_icon);
            mGame_name = itemView.findViewById(R.id.game_name);
            mPlay_btn = itemView.findViewById(R.id.play_btn);
        }
    }
}
