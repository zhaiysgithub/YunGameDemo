package kptech.game.kit.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

import kptech.game.kit.manager.KpGameDownloadManger;

/**
 * 游戏相关工具类
 */
public class GameUtils {

    /**
     * 通过延迟ping值计算每秒的数据buffer
     *
     * @param ping 延迟数据值
     * @return 每秒接收的数据值  单位 byte
     * -1 延迟过大，不返回buffer值
     */
    public static long getBufByPing(int ping) {

        if (ping <= 50) {
            return KpGameDownloadManger.BUF_SIZE_H;
        } else if (ping <= 100) {
            return KpGameDownloadManger.BUF_SIZE_M;
        } else if (ping <= 150) {
            return KpGameDownloadManger.BUF_SIZE_L;
        } else {
            return -1;
        }
    }

    /**
     * 设置下载限速值
     * @param curPkgName 当前游戏包名
     * @param pkgNameStr 配置的游戏信息
     * @param dataBufStr 配置的限速值
     */
    public static void setDownSpeedLimit(String curPkgName,String pkgNameStr,String dataBufStr){
        try{
            if (curPkgName.isEmpty() || pkgNameStr.isEmpty() || dataBufStr.isEmpty() || !pkgNameStr.contains(curPkgName)){
                return;
            }
            String[] pkgArr = pkgNameStr.split(",");
            String[] dataBufArr = dataBufStr.split(",");
            int index = -1;
            for (int i = 0;i < pkgArr.length;i++){
                if (curPkgName.equals(pkgArr[i])){
                    index = i;
                    break;
                }
            }
            if (index >= 0 && index < dataBufArr.length){
                String dataBuf = dataBufArr[index];
                String[] curDataBufArr = dataBuf.split("-");
                if (curDataBufArr.length != 3){
                    return;
                }
                double dataBufH = Double.parseDouble(curDataBufArr[0]);
                double dataBufM = Double.parseDouble(curDataBufArr[1]);
                double dataBufL = Double.parseDouble(curDataBufArr[2]);
                long dataBufHValue = (long) (dataBufH * 1024 * 1024);
                long dataBufMValue = (long) (dataBufM * 1024 * 1024);
                long dataBufLValue = (long) (dataBufL * 1024 * 1024);
                KpGameDownloadManger.setSpeedLimitValue(dataBufHValue, dataBufMValue, dataBufLValue);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 文件md5值计算
     */
    public static String getMD5(File file) {
        if (file.isFile()) {
            MessageDigest digest;
            FileInputStream in = null;
            byte[] buffer = new byte[1024];
            try {
                digest = MessageDigest.getInstance("MD5");
                in = new FileInputStream(file);
                int len;
                while ((len = in.read(buffer, 0, 1024)) != -1) {
                    digest.update(buffer, 0, len);
                }
                return toHex(digest.digest());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(in);
            }
        }
        return null;
    }

    private static String toHex(byte[] md) {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int j = md.length;
        char[] str = new char[j * 2];

        for (int i = 0; i < j; ++i) {
            byte byte0 = md[i];
            str[2 * i] = hexDigits[byte0 >>> 4 & 15];
            str[i * 2 + 1] = hexDigits[byte0 & 15];
        }

        return new String(str);
    }

    private static void close(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
