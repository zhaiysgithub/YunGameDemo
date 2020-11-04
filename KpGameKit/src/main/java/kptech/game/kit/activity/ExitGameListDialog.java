package kptech.game.kit.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import kptech.game.kit.GameInfo;
import kptech.game.kit.R;
import kptech.game.kit.utils.DensityUtil;


public class ExitGameListDialog extends Dialog {

    public interface ICallback{
        void onGameItem(GameInfo gameInfo);
        void onExit();
        void onClose();
    }

    private Activity mContext;
    private ListAdapter mAdapter;

    private List<GameInfo> mGames;


    private ICallback mCallback;
    public void setCallback(ICallback callback){
        this.mCallback = callback;
    }


    public ExitGameListDialog(@NonNull Activity context, List<GameInfo> list) {
        super(context, R.style.MyTheme_CustomDialog_Background);
        this.mContext = context;
        mGames = new ArrayList<>();
        if (list!=null){
            for (int i = 0; i < list.size(); i++) {
                if (i >= 6){
                    break;
                }
                mGames.add(list.get(i));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_game_exit);

        findViewById(R.id.exit_game).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback!=null){
                    mCallback.onExit();
                }
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback!=null){
                    mCallback.onClose();
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.list);

        int count = 3;
        if (mGames.size() == 1){
            count = 1;
        }else if (mGames.size() == 2){
            count = 2;
        }

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), count));
        mAdapter = new ListAdapter(mContext, mGames);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//                outRect.left = DisplayUtil.dip2px(getContext(),10);
//                outRect.right = DisplayUtil.dip2px(getContext(),10);
                outRect.top = DensityUtil.dip2px(getContext(),5);
                outRect.bottom = DensityUtil.dip2px(getContext(),5);
            }
        });

        setCanceledOnTouchOutside(false);

    }

    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void fullScreenImmersive(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }

    class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Activity mActivity;
        private List<GameInfo> mList = null;

        public ListAdapter(Activity context, List<GameInfo> list) {
            this.mActivity = context;
            mList = new ArrayList<>(list);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            if (mList.size() == 1 && mList.get(0).coverUrl!=null){
                return new ItemViewHolder(LayoutInflater.from(this.mActivity).inflate(R.layout.dialog_exit_item_1, viewGroup, false));
            }
            return new ItemViewHolder(LayoutInflater.from(this.mActivity).inflate(R.layout.dialog_exit_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            ((ItemViewHolder)holder).bindHolder(mList.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GameInfo game = mList.get(position);
                    if (mCallback != null){
                        mCallback.onGameItem(game);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            private ImageView mImageView;
            private TextView mTitleText;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                mImageView = itemView.findViewById(R.id.img);
                mTitleText = itemView.findViewById(R.id.title);
            }

            public void bindHolder(GameInfo gameInfo) {
                mTitleText.setText(gameInfo.name);
                String imgUrl;
                if (mList.size() == 1 && gameInfo.coverUrl!=null){
                    imgUrl = gameInfo.coverUrl;
                }else {
                    imgUrl = gameInfo.iconUrl;
                }
                try {
                    Picasso.with(getContext()).load(imgUrl).into(mImageView);
                }catch (Exception e){}
            }

        }
    }
}
