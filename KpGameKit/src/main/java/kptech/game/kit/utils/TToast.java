package kptech.game.kit.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import kptech.game.kit.R;

/**
 * Create by hanweiwei on 11/07/2018
 */
public final class TToast {
    private static Toast sToast;

    public static void show(Context context, String msg) {
        show(context, msg, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String msg, int duration) {
        Toast toast = getToast(context);
        if (toast != null) {
            toast.setDuration(duration);
            toast.setText(String.valueOf(msg));
            toast.show();
        } else {
            Log.i("TToast", "toast msg: " + String.valueOf(msg));
        }
    }

    @SuppressLint("ShowToast")
    private static Toast getToast(Context context) {
        if (context == null) {
            return sToast;
        }
//        if (sToast == null) {
//            synchronized (TToast.class) {
//                if (sToast == null) {
                    sToast = Toast.makeText(context.getApplicationContext(), "", Toast.LENGTH_SHORT);
//                }
//            }
//        }
        return sToast;
    }

    public static void reset() {
        sToast = null;
    }


    public static void showCenterToast(Context context, String msg, int duration){
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.kp_toast_center, null);
        Toast toast = new Toast(context);
        toast.setView(toastRoot);
        toast.setGravity(Gravity.CENTER, 0, 0);
        TextView tv = (TextView) toastRoot.findViewById(R.id.toast_notice);
        tv.setText(msg);
        toast.setDuration(duration);
        toast.show();
    }
}
