package kptach.game.kit.lib.baidu;

import android.os.Parcel;
import android.os.Parcelable;

import kptech.game.kit.GameInfo;

public class QueueRankInfo implements Parcelable {

    public static final Creator<QueueRankInfo> CREATOR = new Creator<QueueRankInfo>() {
        public QueueRankInfo createFromParcel(Parcel var1) {
            return new QueueRankInfo(var1);
        }

        public QueueRankInfo[] newArray(int var1) {
            return new QueueRankInfo[var1];
        }
    };
    //游戏信息
    public GameInfo gameInf;
    //当前排名
    public int queueRanking;
    //预估时间，单位分钟，不准确
    public int queueWaitTime;

    public QueueRankInfo() {
    }

    protected QueueRankInfo(Parcel var1) {
        this.gameInf = (GameInfo)var1.readParcelable(GameInfo.class.getClassLoader());
        this.queueRanking = var1.readInt();
        this.queueWaitTime = var1.readInt();
    }

    public String toString() {
        StringBuffer var1 = new StringBuffer("QueueRankInfo{");
        var1.append("gameInf=").append(this.gameInf);
        var1.append(", queueRanking=").append(this.queueRanking);
        var1.append(", queueWaitTime=").append(this.queueWaitTime);
        var1.append('}');
        return var1.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeParcelable(this.gameInf, var2);
        var1.writeInt(this.queueRanking);
        var1.writeInt(this.queueWaitTime);
    }

    protected com.yd.yunapp.gameboxlib.QueueRankInfo getLibQueueRankInfo(){
//        com.yd.yunapp.gameboxlib.QueueRankInfo inf = new com.yd.yunapp.gameboxlib.QueueRankInfo();
//        inf.gameInfo = this.gameInf.getLibGameInfo();
//        inf.queueRanking = this.queueRanking;
//        inf.queueWaitTime = this.queueWaitTime;
//        return inf;
        return null;
    }

    protected QueueRankInfo(com.yd.yunapp.gameboxlib.QueueRankInfo info){
//        this.gameInf = new GameInfo(info.gameInfo);
//        this.queueRanking = info.queueRanking;
//        this.queueWaitTime = info.queueWaitTime;
    }
}
