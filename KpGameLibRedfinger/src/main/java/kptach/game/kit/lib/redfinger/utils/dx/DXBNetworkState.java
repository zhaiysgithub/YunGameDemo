package kptach.game.kit.lib.redfinger.utils.dx;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/* compiled from: DXBNetworkState */
public class DXBNetworkState {
    public static String toUrlEncodeString(HashMap<String, String> hashMap, String str) {
        String a;
        StringBuilder sb = new StringBuilder();
        for (String str2 : hashMap.keySet()) {
            String a2 = urlEncode(str2, str);
            String str3 = hashMap.get(a2);
            if (str3 == null) {
                a = "";
            } else {
                a = urlEncode(str3, str);
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(a2);
            sb.append("=");
            sb.append(a);
        }
        return sb.toString();
    }

    private static String urlEncode(String str, String str2) {
        if (str2 == null) {
            str2 = "ISO-8859-1";
        }
        try {
            return URLEncoder.encode(str, str2);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void putMap(String str, String str2, HashMap<String, String> hashMap) {
        if (!TextUtils.isEmpty(str2)) {
            hashMap.put(str, str2);
        }
    }
}