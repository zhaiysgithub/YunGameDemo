package kptach.game.kit.lib.redfinger.utils.dx;

import java.io.File;
import java.io.FileInputStream;

/* compiled from: DXBFileUtils */
public class DXBFileUtils {
    public static byte[] a(File file) {
        FileInputStream fileInputStream;
        if (file != null && file.exists() && file.isFile()) {
            byte[] bArr = new byte[((int) file.length())];
            try {
                fileInputStream = new FileInputStream(file);
                try {
                    fileInputStream.read(bArr);
                    DXBCloseUtils.closeable(fileInputStream);
                    return bArr;
                } catch (Throwable th) {
                    DXBCloseUtils.closeable(fileInputStream);
                    return new byte[0];
                }
            } catch (Throwable th2) {
                fileInputStream = null;
                DXBCloseUtils.closeable(fileInputStream);
                return new byte[0];
            }
        }
        return new byte[0];
    }

    public static byte[] a(String str) {
        return a(new File(str));
    }
}