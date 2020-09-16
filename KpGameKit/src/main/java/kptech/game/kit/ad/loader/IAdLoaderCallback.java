package kptech.game.kit.ad.loader;

public interface IAdLoaderCallback {
    void onAdReady();
    void onAdClose();
    void onAdFail();
}
