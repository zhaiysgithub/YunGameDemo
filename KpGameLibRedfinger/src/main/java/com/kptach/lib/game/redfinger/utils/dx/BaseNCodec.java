package com.kptach.lib.game.redfinger.utils.dx;

/* compiled from: BaseNCodec */
public abstract class BaseNCodec {
    protected final int a;
    private final int b;
    private final int c;
    private final int d;
    protected byte[] e;
    protected int f;
    protected boolean g;
    protected int h;
    protected int i;
    private int j;

    protected BaseNCodec(int i2, int i3, int i4, int i5) {
        this.b = i2;
        this.c = i3;
        this.a = (i4 <= 0 || i5 <= 0) ? 0 : (i4 / i3) * i3;
        this.d = i5;
    }

    private void c() {
        this.e = null;
        this.f = 0;
        this.j = 0;
        this.h = 0;
        this.i = 0;
        this.g = false;
    }

    private void d() {
        byte[] bArr = this.e;
        if (bArr == null) {
            this.e = new byte[b()];
            this.f = 0;
            this.j = 0;
            return;
        }
        byte[] bArr2 = new byte[(bArr.length * 2)];
        System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        this.e = bArr2;
    }

    /* access modifiers changed from: package-private */
    public int a() {
        if (this.e != null) {
            return this.f - this.j;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public abstract void a(byte[] bArr, int i2, int i3);

    /* access modifiers changed from: protected */
    public abstract boolean a(byte b2);
    public static final int CONTROL_SERVICE_CONNECT_SUCCESS = 8192;
    /* access modifiers changed from: protected */
    public int b() {
        return CONTROL_SERVICE_CONNECT_SUCCESS;
    }

    /* access modifiers changed from: package-private */
    public int b(byte[] bArr, int i2, int i3) {
        if (this.e == null) {
            return this.g ? -1 : 0;
        }
        int min = Math.min(a(), i3);
        System.arraycopy(this.e, this.j, bArr, i2, min);
        int i4 = this.j + min;
        this.j = i4;
        if (i4 < this.f) {
            return min;
        }
        this.e = null;
        return min;
    }

    /* access modifiers changed from: protected */
    public void a(int i2) {
        byte[] bArr = this.e;
        if (bArr == null || bArr.length < this.f + i2) {
            d();
        }
    }

    /* access modifiers changed from: protected */
    public boolean a(byte[] bArr) {
        if (bArr == null) {
            return false;
        }
        for (int i2 = 0; i2 < bArr.length; i2++) {
            if (61 == bArr[i2] || a(bArr[i2])) {
                return true;
            }
        }
        return false;
    }

    public long c(byte[] bArr) {
        int length = bArr.length;
        int i2 = this.b;
        long j2 = ((long) (((length + i2) - 1) / i2)) * ((long) this.c);
        int i3 = this.a;
        return i3 > 0 ? j2 + ((((((long) i3) + j2) - 1) / ((long) i3)) * ((long) this.d)) : j2;
    }

    public byte[] b(byte[] bArr) {
        c();
        if (bArr == null || bArr.length == 0) {
            return bArr;
        }
        a(bArr, 0, bArr.length);
        a(bArr, 0, -1);
        int i2 = this.f - this.j;
        byte[] bArr2 = new byte[i2];
        b(bArr2, 0, i2);
        return bArr2;
    }
}