package kptech.game.kit.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 游戏初始化配置参数
 */
public class GameBoxConfig implements Parcelable {

    //码率
    public String bitrate;

    //分辨率
    public String gameResolution;

    //是否启动本地输入法
    public boolean enableRemoteIme = true;

    //其他信息
    public String extraInfo;

    public GameBoxConfig(){

    }


    protected GameBoxConfig(Parcel in) {
        bitrate = in.readString();
        gameResolution = in.readString();
        enableRemoteIme = in.readByte() != 0;
        extraInfo = in.readString();
    }

    public static final Creator<GameBoxConfig> CREATOR = new Creator<GameBoxConfig>() {
        @Override
        public GameBoxConfig createFromParcel(Parcel in) {
            return new GameBoxConfig(in);
        }

        @Override
        public GameBoxConfig[] newArray(int size) {
            return new GameBoxConfig[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bitrate);
        dest.writeString(gameResolution);
        dest.writeByte((byte) (enableRemoteIme ? 1 : 0));
        dest.writeString(extraInfo);
    }
}
