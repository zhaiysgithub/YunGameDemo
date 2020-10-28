package kptach.game.kit.inter.ad;

import android.app.Activity;

public interface IAdLoader {
    void loadAd(Activity activity);
    void showAd();
    void setLoaderCallback(IAdLoaderCallback callback);
    void destory();

    String getAdType();
    String getAdCode();
}
