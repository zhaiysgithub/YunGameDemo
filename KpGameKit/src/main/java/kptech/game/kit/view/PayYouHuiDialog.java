package kptech.game.kit.view;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.R;

public class PayYouHuiDialog extends Dialog {

    private int selectPosition;
    private Activity mActivity;
    private MyAdapter mAdapter;
    public PayYouHuiDialog(Activity context) {
        super(context, R.style.MyTheme_Dialog_PayYouHui);
        this.mActivity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kp_dialog_youhui);
        ListView listView = findViewById(R.id.list);

        List<String> data = new ArrayList<>();
        data.add("AAAAA");
        data.add("CCCCC");

        mAdapter = new MyAdapter(mActivity, data);

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectPosition= position;

                mAdapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }


    public class MyAdapter extends BaseAdapter {

        private List<String> mData = new ArrayList<>();
        private Activity mActivity;

        public MyAdapter(Activity activity, List<String> data) {
            mData.addAll(data);
            mActivity = activity;
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
            YhViewHodler holder = null;
            if(convertView ==null) {
                convertView = View.inflate(mActivity,R.layout.kp_layout_youhui_item, null);
                holder = new YhViewHodler(convertView);
            }else {
                holder = (YhViewHodler) convertView.getTag();
            }
            convertView.setTag(holder);
            onBindViewHolder(holder, position);



//            TextView name = (TextView) convertView.findViewById(R.id.text);
//            final RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.radio);
//            if(selectPosition== position) {
//                radioButton.setChecked(true);
//            }else{
//                radioButton.setChecked(false);
//            }
//            name.setText(mData.get(position));
            return convertView;
        }



        public void onBindViewHolder(YhViewHodler holder, final int position) {
            holder.text.setText("优惠券"+position);
            if(selectPosition== position) {
                holder.radio.setChecked(true);
            }else{
                holder.radio.setChecked(false);
            }
        }

        class YhViewHodler  {
            TextView text;
            RadioButton radio;
            public YhViewHodler(View itemView) {
                text = itemView.findViewById(R.id.text);
                radio = itemView.findViewById(R.id.radio);
            }
        }
    }

}
