package kptech.game.kit.ad.loader;

import android.app.Activity;

public interface IAdLoader {
    void loadAd(Activity activity);
    void showAd();
    void setLoaderCallback(IAdLoaderCallback callback);
    void destory();

    void setPkgName(String pkgName);
}
