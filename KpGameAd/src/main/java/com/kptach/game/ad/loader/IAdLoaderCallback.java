package com.kptach.game.ad.loader;

public interface IAdLoaderCallback {
    void onAdReady();
    void onAdClose(boolean verify);
    void onAdFail();

//    void onAdClosed(String posId, String info);
//    void onRewardVerify(String posId, boolean rewardVerify, int rewardAmount, String rewardName);
//    void onPlayComplete(String posId, String info);
//    void onAdShow(String posId, String info);
//    void onAdClick(String posId, String info);
//    void onAdReady(String posId, int count, String info);
//    void onAdEmpty(String posId, String info);
//
//
//    void onAdShow(String posId, String info);
//    void onAdClick(String posId, String info);
//    void onAdReady(String posId, int count, String info);
//    void onAdEmpty(String posId, String info);


}
