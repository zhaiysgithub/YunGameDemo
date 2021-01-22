package kptach.game.kit.lib.redfinger.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FilePathUtils {
    public static final String libDir = "kp_lib";
    public static final String libFileName = "libmci.so";

    public static String getLibMciFilePath(Context context){
        String path = getFilesPath(context) + File.separator + libDir;
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
        }
        file = new File(file, libFileName);
        return file.getAbsolutePath();
    }

    private static String getFilesPath( Context context ){
        String filePath ;
//        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
//                || !Environment.isExternalStorageRemovable()) {
//            //外部存储可用
//            filePath = context.getExternalFilesDir(null).getPath();
//        }else {
            //外部存储不可用
            filePath = context.getFilesDir().getParent() ;
//        }
        return filePath ;
    }
}
