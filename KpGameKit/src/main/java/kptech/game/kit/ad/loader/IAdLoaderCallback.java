package kptech.game.kit.ad.loader;

public interface IAdLoaderCallback {

    void onAdClosed(IAdLoader loader, String posId, String info);
    void onRewardVerify(IAdLoader loader, String posId, boolean rewardVerify, int rewardAmount, String rewardName);
    void onPlayComplete(IAdLoader loader, String posId, String info);
    void onAdShow(IAdLoader loader, String posId, String info);
    void onAdClick(IAdLoader loader, String posId, String info);
    void onAdReady(IAdLoader loader, String posId, int count, String info);
    void onAdEmpty(IAdLoader loader, String posId, String info);

}
