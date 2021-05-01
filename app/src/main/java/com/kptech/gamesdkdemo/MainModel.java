package com.kptech.gamesdkdemo;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kptech.game.kit.GameBox;
import kptech.game.kit.GameBoxManager;
import kptech.game.kit.GameInfo;
import kptech.game.kit.Params;

public class MainModel {

    private final AppCompatActivity mActivity;
    private final MainCallback mCallback;

    public interface MainCallback {

        void onNext(@NonNull List<GameInfo> gameInfos);

        void onError(@NonNull Throwable e);

        void onComplete();
    }

    public MainModel(AppCompatActivity activity, MainCallback callback) {
        mActivity = activity;
        this.mCallback = callback;
    }

    public String getAppId() {
        return Enviroment.getInstance().getmCropKey();
    }

    public String getTitleStr() {
        String appName = AppUtils.getAppName(mActivity);
        String appVersionName = AppUtils.getVersionName(mActivity);
        return appName + " " + appVersionName;
    }

    public void getGameInfos() {

        Observable.create((ObservableOnSubscribe<List<GameInfo>>) emitter -> {
            try {

                if (!emitter.isDisposed()) {
                    List<GameInfo> infos = GameBoxManager.getInstance().queryGameList(0, 50);
                    emitter.onNext(infos);
                }
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<GameInfo>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<GameInfo> gameInfos) {
                        if (mCallback != null) {
                            mCallback.onNext(gameInfos);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (mCallback != null) {
                            mCallback.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (mCallback != null) {
                            mCallback.onComplete();
                        }
                    }
                });
    }

    /**
     * 启动游戏
     */
    public void startGame(GameInfo gameInfo) {
        GameBox.getInstance().playGame(mActivity, gameInfo);
    }

    /**
     * 启动游戏
     * params : 配置 联运用户帐号
     */
    public void startGame(GameInfo gameInfo, Params params) {
        GameBox.getInstance().playGame(mActivity, gameInfo, params);
    }
}
