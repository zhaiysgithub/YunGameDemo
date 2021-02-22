package com.kptach.lib.game.redfinger.utils.dx;

/* compiled from: Base64 */
public class Base64 extends BaseNCodec {
    static final byte[] p = {13, 10};
    private static final byte[] q = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
    private static final byte[] r = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
    private static final byte[] s = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};
    private final byte[] k;
    private final byte[] l;
    private final byte[] m;
    private final int n;
    private int o;

    public Base64(boolean z) {
        this(76, p, z);
    }

    public static byte[] a(byte[] bArr, boolean z) {
        return a(bArr, z, false);
    }

    public static byte[] d(byte[] bArr) {
        return a(bArr, false);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Base64(int i, byte[] bArr, boolean z) {
        super(3, 4, i, bArr == null ? 0 : bArr.length);
        this.l = s;
        if (bArr == null) {
            this.n = 4;
            this.m = null;
        } else if (a(bArr)) {
            throw new IllegalArgumentException("lineSeparator must not contain base64 characters: [" + bArr + "]");
        } else if (i > 0) {
            this.n = bArr.length + 4;
            byte[] bArr2 = new byte[bArr.length];
            this.m = bArr2;
            System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        } else {
            this.n = 4;
            this.m = null;
        }
        this.k = z ? r : q;
    }

    public static byte[] a(byte[] bArr, boolean z, boolean z2) {
        return a(bArr, z, z2, Integer.MAX_VALUE);
    }

    public static byte[] a(byte[] bArr, boolean z, boolean z2, int i) {
        if (bArr == null || bArr.length == 0) {
            return bArr;
        }
        Base64 h1Var = z ? new Base64(z2) : new Base64(0, p, z2);
        long c = h1Var.c(bArr);
        if (c <= ((long) i)) {
            return h1Var.b(bArr);
        }
        throw new IllegalArgumentException("Input array too big, the output array would be bigger (" + c + ") than the specified maximum size of " + i);
    }

    /* access modifiers changed from: package-private */
    @Override // yunapp.gamebox.i1
    public void a(byte[] bArr, int i, int i2) {
        if (!this.g) {
            if (i2 < 0) {
                this.g = true;
                if (this.i != 0 || this.a != 0) {
                    a(this.n);
                    int i3 = this.f;
                    int i4 = this.i;
                    if (i4 == 1) {
                        byte[] bArr2 = this.e;
                        int i5 = i3 + 1;
                        this.f = i5;
                        byte[] bArr3 = this.k;
                        int i6 = this.o;
                        bArr2[i3] = bArr3[(i6 >> 2) & 63];
                        int i7 = i5 + 1;
                        this.f = i7;
                        bArr2[i5] = bArr3[(i6 << 4) & 63];
                        if (bArr3 == q) {
                            int i8 = i7 + 1;
                            this.f = i8;
                            bArr2[i7] = 61;
                            this.f = i8 + 1;
                            bArr2[i8] = 61;
                        }
                    } else if (i4 == 2) {
                        byte[] bArr4 = this.e;
                        int i9 = i3 + 1;
                        this.f = i9;
                        byte[] bArr5 = this.k;
                        int i10 = this.o;
                        bArr4[i3] = bArr5[(i10 >> 10) & 63];
                        int i11 = i9 + 1;
                        this.f = i11;
                        bArr4[i9] = bArr5[(i10 >> 4) & 63];
                        int i12 = i11 + 1;
                        this.f = i12;
                        bArr4[i11] = bArr5[(i10 << 2) & 63];
                        if (bArr5 == q) {
                            this.f = i12 + 1;
                            bArr4[i12] = 61;
                        }
                    }
                    int i13 = this.h;
                    int i14 = this.f;
                    int i15 = (i14 - i3) + i13;
                    this.h = i15;
                    if (this.a > 0 && i15 > 0) {
                        byte[] bArr6 = this.m;
                        System.arraycopy(bArr6, 0, this.e, i14, bArr6.length);
                        this.f += this.m.length;
                        return;
                    }
                    return;
                }
                return;
            }
            int i16 = 0;
            while (i16 < i2) {
                a(this.n);
                this.i = (this.i + 1) % 3;
                int i17 = i + 1;
                byte b = bArr[i];
                int i18 = b;
                if (b < 0) {
                    i18 = b + 256;
                }
                int i19 = (i18 == 1 ? 1 : 0) + (this.o << 8);
                this.o = i19;
                if (this.i == 0) {
                    byte[] bArr7 = this.e;
                    int i20 = this.f;
                    int i21 = i20 + 1;
                    this.f = i21;
                    byte[] bArr8 = this.k;
                    bArr7[i20] = bArr8[(i19 >> 18) & 63];
                    int i22 = i21 + 1;
                    this.f = i22;
                    bArr7[i21] = bArr8[(i19 >> 12) & 63];
                    int i23 = i22 + 1;
                    this.f = i23;
                    bArr7[i22] = bArr8[(i19 >> 6) & 63];
                    int i24 = i23 + 1;
                    this.f = i24;
                    bArr7[i23] = bArr8[i19 & 63];
                    int i25 = this.h + 4;
                    this.h = i25;
                    int i26 = this.a;
                    if (i26 > 0 && i26 <= i25) {
                        byte[] bArr9 = this.m;
                        System.arraycopy(bArr9, 0, bArr7, i24, bArr9.length);
                        this.f += this.m.length;
                        this.h = 0;
                    }
                }
                i16++;
                i = i17;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // yunapp.gamebox.i1
    public boolean a(byte b) {
        if (b >= 0) {
            byte[] bArr = this.l;
            return b < bArr.length && bArr[b] != -1;
        }
        return false;
    }
}
