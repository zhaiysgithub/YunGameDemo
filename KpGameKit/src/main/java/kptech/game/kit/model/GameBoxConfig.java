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
    public GameResolution gameResolution;

    //其他信息
    public String extraInfo;

    protected GameBoxConfig(Parcel in) {
        bitrate = in.readString();
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
        dest.writeString(extraInfo);
    }
}
