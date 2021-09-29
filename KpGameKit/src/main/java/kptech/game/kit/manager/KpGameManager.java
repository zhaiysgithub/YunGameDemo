package kptech.game.kit.manager;

import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.callback.IGameObservable;

public class KpGameManager {

    private final List<IGameObservable> mObservables = new ArrayList<>();

    private KpGameManager() {
    }

    private static class ObserverHelperHolder {
        private static final KpGameManager helper = new KpGameManager();
    }

    public static KpGameManager instance() {
        return ObserverHelperHolder.helper;
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

    public void clearObservable() {
        mObservables.clear();
    }

    public void sendBackObserver(boolean isExit) {
        for (IGameObservable obs : mObservables) {
            obs.onBackListener(isExit);
        }
    }

    public void sendReloadObserver() {
        for (IGameObservable obs : mObservables) {
            obs.onReloadListener();
        }
    }

    public void sendDownloadObserver(){
        for (IGameObservable obs : mObservables) {
            obs.onDownloadListener();
        }
    }

    public void sendCopyInfoObserver(String info){
        for (IGameObservable obs : mObservables) {
            obs.onCopyInfoListener(info);
        }
    }

    public void sendAuthObserver(boolean isAuthPass){
        for (IGameObservable obs : mObservables) {
            obs.onAuthListener(isAuthPass);
        }
    }

}
