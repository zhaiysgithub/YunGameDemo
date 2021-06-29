package kptech.game.kit.pass.manager;

public class PassConstants {

    //无错误
    public static final int PASS_CODE_SUCCESS = 0;
    //CorpKey不存在
    public static final int PASS_CODE_ERROR_CORPKEY = 10000;
    //PkgName错误，即APP不存在
    public static final int PASS_CODE_ERROR_PKGNAME = 10001;
    //未授权的APP访问，即该APP未授权给该企业使用
    public static final int PASS_CODE_ERROR_AUTH = 10002;
    //无空闲设备（设备已经安装APP但设备都在使用中未释放）
    public static final int PASS_CODE_ERROR_DEVICEBUSY = 1003;
    //APP未安装到相关设备
    public static final int PASS_CODE_ERROR_APP = 10004;
    //该企业未分配设备
    public static final int PASS_CODE_ERROR_DEVICENO = 10005;
    //操作失败,请联系管理员(调用百度申请实例接口出错,百度方反馈此错误代表设备离线)
    public static final int PASS_CODE_ERROR_DEFAULT = 10006;

}
