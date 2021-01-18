package kptech.game.kit.redfinger.fragment;

import android.os.Parcel;
import android.os.Parcelable;

/* compiled from: ErrorInfo */
public class p implements Parcelable {
    public static final Parcelable.Creator<p> CREATOR = new a();
    private String a;
    private int b;
    public boolean c;
    public int d;
    public int e;
    public int f;

    /* compiled from: ErrorInfo */
    static class a implements Parcelable.Creator<p> {
        a() {
        }

        @Override // android.os.Parcelable.Creator
        public p createFromParcel(Parcel parcel) {
            return new p(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public p[] newArray(int i) {
            return new p[i];
        }
    }

    public p(int i, String str, boolean z, int i2, int i3, int i4) {
        this.a = str;
        this.b = i;
        this.c = z;
        this.d = i2;
        this.e = i3;
        this.f = i4;
    }

    public int a() {
        return this.f;
    }

    public int b() {
        return this.b;
    }

    public String c() {
        return this.a;
    }

    public int d() {
        return this.e;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"isFirstConnect\"").append(":").append(this.c ? "true" : "false").append(",");
        sb.append("\"netState\"").append(":").append(this.d).append(",");
        sb.append("\"errorCode\"").append(":").append(this.b).append(",");
        sb.append("\"reconnectCount\"").append(":").append(this.e).append(",");
        sb.append("\"disconnectCount\"").append(":").append(this.f).append(",");
        sb.append("\"errorDetails\"").append(":").append("\"").append(this.a).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeInt(this.b);
        parcel.writeInt(this.c ? 1 : 0);
        parcel.writeInt(this.d);
        parcel.writeInt(this.e);
        parcel.writeInt(this.f);
    }

    protected p(Parcel parcel) {
        boolean z = true;
        this.a = parcel.readString();
        this.b = parcel.readInt();
        this.c = parcel.readInt() != 1 ? false : z;
        this.d = parcel.readInt();
        this.e = parcel.readInt();
        this.f = parcel.readInt();
    }
}