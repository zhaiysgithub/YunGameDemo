package kptech.game.kit;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GameInfo implements Parcelable {
    public static int GAME_AD_SHOW_AUTO = 0;    //按服务器配置信息显示
    public static int GAME_AD_SHOW_ON = 1;    //显示广告
    public static int GAME_AD_SHOW_OFF = 2;    //关闭广告



    public static final Creator<GameInfo> CREATOR = new Creator<GameInfo>() {
        public GameInfo createFromParcel(Parcel var1) {
            return new GameInfo(var1);
        }

        public GameInfo[] newArray(int var1) {
            return new GameInfo[var1];
        }
    };

    public int gid;
    public String pkgName;
    public String name;
    public String iconUrl;
    public String downloadUrl;
    public int playCount;
    public int totalTime;
    public int usedTime;
    public long size;
    public String kpGameId;
    public int showAd = GAME_AD_SHOW_AUTO;
    //自动本地IMEI\AndroidID到云设备，0不添加，1添加
    public int addMockInfo = 1;
    public HashMap<String,String> ext;

    public GameInfo() {
    }

    protected GameInfo(Parcel var1) {
        this.gid = var1.readInt();
        this.pkgName = var1.readString();
        this.name = var1.readString();
        this.iconUrl = var1.readString();
        this.downloadUrl = var1.readString();
        this.playCount = var1.readInt();
        this.totalTime = var1.readInt();
        this.usedTime = var1.readInt();
        this.size = var1.readLong();
        this.showAd = var1.readInt();
        this.addMockInfo = var1.readInt();
        this.ext = var1.readHashMap(HashMap.class.getClassLoader());
    }

    public int getEffectTime() {
        return this.totalTime - this.usedTime;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeInt(this.gid);
        var1.writeString(this.pkgName);
        var1.writeString(this.name);
        var1.writeString(this.iconUrl);
        var1.writeString(this.downloadUrl);
        var1.writeInt(this.playCount);
        var1.writeInt(this.totalTime);
        var1.writeInt(this.usedTime);
        var1.writeLong(this.size);
        var1.writeInt(this.showAd);
        var1.writeInt(this.addMockInfo);
        var1.writeMap(this.ext);
    }


    protected com.yd.yunapp.gameboxlib.GameInfo getLibGameInfo(){
        com.yd.yunapp.gameboxlib.GameInfo inf = new com.yd.yunapp.gameboxlib.GameInfo();
        inf.gid = this.gid;
        inf.pkgName = this.pkgName;
        inf.name = this.name;
        inf.iconUrl = this.iconUrl;
        inf.downloadUrl = this.downloadUrl;
        inf.playCount = this.playCount;
        inf.totalTime = this.totalTime;
        inf.usedTime = this.usedTime;
        inf.size = this.size;
        return inf;
    }

    protected GameInfo(com.yd.yunapp.gameboxlib.GameInfo info){
        this.gid = info.gid;
        this.pkgName = info.pkgName;
        this.name = info.name;
        this.iconUrl = info.iconUrl;
        this.downloadUrl = info.downloadUrl;
        this.playCount = info.playCount;
        this.totalTime = info.totalTime;
        this.usedTime = info.usedTime;
        this.size = info.size;
    }

    @Override
    public String toString() {
        String str = "gid:" + this.gid+",pkgName:" + this.pkgName + ",name:" + this.name
                +",iconUrl:" + this.iconUrl + ",downloadUrl:" + this.downloadUrl
                +",playCount:" + this.playCount + ",totalTime:" + this.totalTime
                +",usedTime:" + this.usedTime + ",size:" + this.size
                +",showAd:" + this.showAd;
        return super.toString();
    }
}
