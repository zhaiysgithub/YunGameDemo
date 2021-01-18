package kptech.game.kit.redfinger;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;


public class BaiDuUtils {

    private static BaiDuUtils _self = null;
    public final String BD_PAAS_HOST = "http://yunapp-api.baidu.com";
    private long _nonce = 0;

//    public final String BD_PAAS_AK_KP = "TOphL4quGn1a7dVRisS5ywU0";
//    public final String BD_PAAS_SK_KP = "foeGZYkV4NOICn9Qpuq507ElvagTMHybhrKPLX6S";

    public final String BD_PAAS_APPLY = "/api/v1/device/apply";

    public final String BD_PAAS_APPS = "/api/v1/apps";

    public static BaiDuUtils instance()
    {
        if(_self == null){
            _self = new BaiDuUtils();
        }

        return _self;
    }

    public void InitNonce()
    {
        this._nonce = new Date().getTime();
    }

    public static boolean isEmpty(String str) {
        if (str == null || str.trim() == "") {
            return true;
        }
        return false;
    }

    private String getUrlSign(Map<String, String> params, String secretKey) {
        Map<String, String> signParams = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            signParams.put(entry.getKey(), entry.getValue());
        }
        StringBuilder stripEmptyValueParams = new StringBuilder(); // 对于key1=&key2=xxx中得key1的值用-计算签名
        Set<Map.Entry<String, String>> paramsEntry = signParams.entrySet();
        for (Map.Entry<String, String> entry : paramsEntry) {
            String k = entry.getKey();
            String v = entry.getValue();
            stripEmptyValueParams.append(k).append(isEmpty(v) ? "-" : v);
        }
        stripEmptyValueParams.append(secretKey);

        return  md5(stripEmptyValueParams.toString());//DigestUtils.md5Hex(StringUtils.getBytesUtf8(stripEmptyValueParams.toString()));
    }

    public String getGuid(){
        class S4{
            private String getS4(){
                return Integer.toHexString((int)((Math.random()+1)*0x10000)).substring(1);
            }
        }
        S4 s4 = new S4();
        String result = s4.getS4() + s4.getS4() + "-" + s4.getS4() + "-" + s4.getS4() + "-" + s4.getS4() + "-" + s4.getS4() + s4.getS4() + s4.getS4();
        System.out.println(result);
        return result;
    }

    public String getUrl(String url,
                                String appKey,
                                String secretKey,
                                String apiPath,
                                Map<String, String> getData) {
        Map<String, String> params = new HashMap<>();
        params.put("auth_ver", "2");
        params.put("appkey", appKey);
        if (getData != null) {
            for (String key : getData.keySet()) {
                String val = getData.get(key);
                params.put(key,val);
            }
//            getData.forEach((setStatus, v) -> params.put(setStatus,v));
        }
        Long nonce = System.currentTimeMillis();
        params.put("nonce", String.valueOf(nonce));
        String s = getUrlSign(params, secretKey);
        String query = "s=" + s;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            query = query + "&" + entry.getKey() + "=" + entry.getValue();
        }
        url = url + apiPath + "?" + query;
        Log.i("BaiDuUtils","request paas "+apiPath+" url:" + url);
        return url;
    }

    public  String encryBody(String bodyParam,String sk)
    {
        Cipher cipher = null;
        try{
            cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");
            SecretKey secretKey  = skf.generateSecret(new DESedeKeySpec(sk.getBytes("utf-8")));
            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            byteBuffer.putLong(this._nonce);
            byte[] iv= byteBuffer.array();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] cipherText = cipher.doFinal(bodyParam.getBytes("utf-8"));
            String msg= Base64.encodeToString(cipherText, 0);
            Map map = new HashMap();
            map.put("createTime", this._nonce);
            map.put("msg", msg);
            return new JSONObject(map).toString();

        }catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException |
                UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException |
                InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static byte[] a(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        try {
            Cipher instance = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            instance.init(1, SecretKeyFactory.getInstance("DESede").generateSecret(new DESedeKeySpec(bArr)), new IvParameterSpec(bArr2));
            return instance.doFinal(bArr3);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    public static byte[] a(long j) {
        ByteBuffer allocate = ByteBuffer.allocate(8);
        allocate.order(ByteOrder.BIG_ENDIAN);
        allocate.putLong(j);
        return allocate.array();
    }

//    public static JSONObject getPostBody(String secretKey, JSONObject object) throws JSONException {
//        // return object;
//        String bodyStr = object.toString();
//        Long createTime = System.currentTimeMillis();
//        String msg = TripleDES.encryptWithBase64(secretKey, createTime, bodyStr);
//        JSONObject postBody = new JSONObject();
//        postBody.put("createTime", createTime);
//        postBody.put("msg", msg);
//        return postBody;
//    }

    public static String md5(String s) {
        try {
            MessageDigest sMd5MessageDigest = MessageDigest.getInstance("MD5");
            sMd5MessageDigest.reset();
            sMd5MessageDigest.update(s.getBytes());

            byte digest[] = sMd5MessageDigest.digest();

            StringBuilder sStringBuilder = new StringBuilder();

            sStringBuilder.setLength(0);
            for (int i = 0; i < digest.length; i++) {
                final int b = digest[i] & 255;
                if (b < 16) {
                    sStringBuilder.append('0');
                }
                sStringBuilder.append(Integer.toHexString(b));
            }

            return sStringBuilder.toString();
        }catch (Exception e){

        }
        return "";

    }
}
