package com.kptach.lib.game.redfinger.utils.dx;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: DigestEncodingUtils */
public class DigestEncodingUtils {
    private static final char[] a = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String a(byte[] bArr) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(bArr);
            return b(instance.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.w("DigestEncodingUtils", "Should never happen!", e);
            return null;
        }
    }

    public static String b(byte[] bArr) {
        StringBuilder sb = new StringBuilder(bArr.length * 2);
        for (int i = 0; i < bArr.length; i++) {
            sb.append(a[(bArr[i] & 240) >>> 4]);
            sb.append(a[bArr[i] & 15]);
        }
        return sb.toString();
    }

    public static String a(String str) {
        try {
            return a(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.w("DigestEncodingUtils", "should never happen!", e);
            return null;
        }
    }
}