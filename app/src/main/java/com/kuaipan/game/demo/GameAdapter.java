package com.kuaipan.game.demo;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.data.RequestLoginTask;
import kptech.game.kit.msg.MsgManager;
import kptech.game.kit.view.LoginActivity;
import kptech.game.kit.view.LoginDialog;
import kptech.game.kit.view.RemindDialog;

public class GameAdapter extends BaseAdapter {

    private List<GameInfo> mData = new ArrayList<>();
    private Activity mActivity;

    public GameAdapter(Activity activity, Collection<GameInfo> data) {
        mData.addAll(data);
        mActivity = activity;
    }

    public void refresh(final Collection<GameInfo> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        GameViewHodler holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list_item, parent, false);
            holder = new GameViewHodler(convertView);
        } else {
            holder = (GameViewHodler) convertView.getTag();
        }
        convertView.setTag(holder);
        onBindViewHolder(holder, position);
        return convertView;
    }

    public void onBindViewHolder(GameViewHodler holder, final int position) {
        final GameInfo game = mData.get(position);
        holder.name.setText(game.name);
        holder.playCount.setText("剩余试玩时间：" + (game.totalTime - game.usedTime) / 60 + "分钟    试玩次数： " + game.playCount);
        if (game.totalTime <= game.usedTime) {
            holder.playBtn.setText("试玩结束");
        } else {
            holder.playBtn.setText("开始试玩");
        }
        Glide.with(holder.icon).load(game.iconUrl).into(holder.icon);
        holder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, GameRunningActivity.class);
                intent.putExtra(GameRunningActivity.EXTRA_GAME, game);
                mActivity.startActivityForResult(intent, HomeActivity.PLAY_GAME_REQUEST);
////                LoginActivity.showRemindDialog(mActivity);

//                LoginDialog.showLoginDialog(mActivity);

                //处理登录，判断是联运登录，还是本地登录
//                new RequestLoginTask(new RequestLoginTask.ICallback() {
//                    @Override
//                    public void onResult(HashMap<String, String> map) {
//
//
//                    }
//                }).execute("2222");
//
//                FloatMenuDialog mMenuDialog = new FloatMenuDialog(mActivity);
//                mMenuDialog.show();

//                LoginDialog dialog = new LoginDialog(mActivity);
//                dialog.show();

//                MsgManager.showKpLogin(mActivity);
            }
        });
    }



    class GameViewHodler extends RecyclerView.ViewHolder {
        TextView name;
        TextView playCount;
        ImageView icon;
        Button playBtn;

        public GameViewHodler(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.game_name);
            playCount = (TextView) itemView.findViewById(R.id.play_count);
            icon = (ImageView) itemView.findViewById(R.id.game_icon);
            playBtn = (Button) itemView.findViewById(R.id.play_btn);
        }
    }
}
