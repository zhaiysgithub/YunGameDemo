package com.kptach.lib.game.bdsdk.utils.dx;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: DXBHashUtils */
public class DXBHashUtils {
    public static String a(String str) {
        return b("MD5", str.getBytes());
    }

    private static String b(String str, byte[] bArr) {
        return DigestEncodingUtils.b(a(str, bArr));
    }

    private static byte[] a(String str, byte[] bArr) {
        try {
            MessageDigest instance = MessageDigest.getInstance(str);
            instance.reset();
            instance.update(bArr);
            return instance.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}