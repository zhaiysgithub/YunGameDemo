package com.yd.yunapp.gamebox.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.yd.yunapp.gamebox.activity.MainActivity;
import com.yd.yunapp.gamebox.UserCertificationDialog;
import com.yd.yunapp.gamebox.utils.AppUtils;

import kptech.game.kit.GameBox;
import kptech.game.kit.GameInfo;
import kptech.game.kit.Params;
import kptech.game.kit.callback.OnAuthCallback;

public class MainModel {

    private final MainActivity activity;
    private final Handler mHandler;

    public MainModel(MainActivity activity){
        this.activity = activity;
        mHandler = new Handler(Looper.myLooper());
    }

    public String getTitleStr() {
        String appName = AppUtils.getAppName(activity);
        String appVersionName = AppUtils.getVersionName(activity);
        return appName + " " + appVersionName;
    }

    /**
     * 显示实名认证的弹窗
     */
    public void showRealNameAuthDialog(GameInfo gameInfo, Params params) {

        //弹出输入手机号框
        UserCertificationDialog certificationDialog = new UserCertificationDialog(activity, true);

        certificationDialog.setOnCallback(new UserCertificationDialog.OnUserCerificationCallbck() {
            @Override
            public void onUserCancel() {
                toggleSoftInput();
            }

            @Override
            public void onUserConfirm(String userName, String userIdCard, String userPhone) {
//                startUserAuth(userName, userIdCard, userPhone, gameInfo, params);
//                toggleSoftInput();
            }

            @Override
            public void onUserConfirm(String userPhone) {
                /*GameBox.getInstance().startLogin(activity, gameInfo, userPhone, new OnAuthCallback() {
                    @Override
                    public void onCerSuccess(String gid, String token) {
                        certificationDialog.dismiss();
                        Toast.makeText(activity, "认证成功", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCerError(int code, String errorStr) {
                        if (code == APIConstants.PHONE_NOT_AUTH){
                            certificationDialog.setInputPhone(false);
                        }else{
                            Toast.makeText(activity, errorStr, Toast.LENGTH_LONG).show();
                        }

                    }
                });*/
            }
        });

        certificationDialog.show();
        mHandler.postDelayed(this::toggleSoftInput, 200);
    }


    private void startUserAuth(String userName, String userIdCard, String userPhone, GameInfo gameInfo, Params params) {

        GameBox.getInstance().startCertification(activity, userName, userIdCard, userPhone, gameInfo, new OnAuthCallback() {
            @Override
            public void onCerSuccess(String gid, String token) {
                Toast.makeText(activity, "认证成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCerError(int code, String errorStr) {
                Toast.makeText(activity, errorStr, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleSoftInput() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private static final String testUserName = "丁文杰";
    private static final String testUserIdCard = "340203198007129355";
    private static final String testUserPhone = "15711485499";

    /**
     * 通过 GID 登录
     */
    public void loginByGid(GameInfo game) {
        //模拟三方直接调用后台认证接口
        GameBox.getInstance().startCertification(activity, testUserName, testUserIdCard, testUserPhone, game, new OnAuthCallback() {
            @Override
            public void onCerSuccess(String gid, String token) {
                GameBox.getInstance().playGame(activity,game,gid,token,testUserPhone);
            }

            @Override
            public void onCerError(int code, String errorStr) {
                //三方调用后台认证接口失败
                Toast.makeText(activity,errorStr,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
