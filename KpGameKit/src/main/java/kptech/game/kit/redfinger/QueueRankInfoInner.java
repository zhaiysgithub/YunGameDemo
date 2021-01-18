package kptech.game.kit.redfinger;

/* compiled from: QueueRankInfoInner */
public class QueueRankInfoInner {
    public int queueRanking;
    public int queueWaitTime;
    public int playQueueCount;
    public boolean supportPlayQueue;

    public QueueRankInfoInner() {
    }

    public String toString() {
        return "RankInfo{queueRanking=" + this.queueRanking + ", queueWaitTime=" + this.queueWaitTime + ", gameInfo=" + '}';
    }

    public QueueRankInfoInner(int i, int i2) {
        this.queueRanking = i;
        this.queueWaitTime = i2;
    }
}
