package kptech.game.kit.manager;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.activity.GamePlay;
import kptech.game.kit.callback.CloudLoadingStatListener;
import kptech.game.kit.callback.IGameObservable;

public class KpGameManager {

    private final List<IGameObservable> mObservables = new ArrayList<>();
    private WeakReference<GamePlay> observerActivity;

    private KpGameManager() {
    }

    private static class ObserverHelperHolder {
        private static final KpGameManager helper = new KpGameManager();
    }

    public static KpGameManager instance() {
        return ObserverHelperHolder.helper;
    }

    public void setWeakReferenceActivity(WeakReference<GamePlay> weakReferenceActivity){
        if (weakReferenceActivity != null && !weakReferenceActivity.get().isFinishing()){
            observerActivity = weakReferenceActivity;
        }
    }

    public void removeWeakReferenceActivity(){
        if (observerActivity != null){
            observerActivity.clear();
        }
    }

    public void addObservable(IGameObservable observable) {
        if (mObservables.contains(observable)) {
            return;
        }
        mObservables.add(observable);
    }

    public void removeObservable(IGameObservable observable) {
        if (!mObservables.contains(observable)) {
            return;
        }
        mObservables.remove(observable);
    }

    public void onExitGamePlay(){
        if (mObservables.size() > 0){
            for(IGameObservable observable : mObservables){
                observable.onGamePlayExit();
            }
        }
    }

    /**
     * 对外提供
     * 注册 loading 页面的监听
     */
    public void registerCloudLoadingStatListener(CloudLoadingStatListener listener){
        if (listener != null && observerActivity != null && observerActivity.get() != null){
            observerActivity.get().registerCloudLoadingStatListener(listener);
        }
    }

}
