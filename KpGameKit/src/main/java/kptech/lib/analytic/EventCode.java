package kptech.lib.analytic;

import com.kptach.lib.inter.game.APIConstants;

public class EventCode {
    //启动游戏界面
    public static final String DATA_ACTIVITY_PLAYGAME_ONCREATE = "DATA_ACTIVITY_PLAYGAME_DISPLAY";
    //关闭游戏界面
    public static final String DATA_ACTIVITY_PLAYGAME_DESTORY = "DATA_ACTIVITY_PLAYGAME_DESTORY";
    //游戏中边玩边下按钮
    public static final String DATA_ACTIVITY_PLAYGAME_DOWNLOAD = "DATA_ACTIVITY_PLAYGAME_DOWNLOAD";
    //边玩边下停止
    public static final String DATA_ACTIVITY_PLAYGAME_DOWNLOADSTOP = "DATA_ACTIVITY_PLAYGAME_DOWNLOADSTOP";
    //游戏错误界面下载
    public static final String DATA_ACTIVITY_PLAYERROR_DOWNLOAD = "DATA_ACTIVITY_PLAYERROR_DOWNLOAD";
    //游戏错误界面停止下载
    public static final String DATA_ACTIVITY_PLAYERROR_DOWNLOADSTOP = "DATA_ACTIVITY_PLAYERROR_DOWNLOADSTOP";
    //游戏错误界面重新加载
    public static final String DATA_ACTIVITY_PLAYERROR_RELOAD = "DATA_ACTIVITY_PLAYERROR_RELOAD";

    //接收到下载开始
    public static final String DATA_ACTIVITY_RECEIVE_DOWNLOADSTART= "DATA_ACTIVITY_RECEIVE_DOWNLOADSTART";
    //接收到下载停止
    public static final String DATA_ACTIVITY_RECEIVE_DOWNLOADSTOP= "DATA_ACTIVITY_RECEIVE_DOWNLOADSTOP";
    //接收到下载出错
    public static final String DATA_ACTIVITY_RECEIVE_DOWNLOADERROR = "DATA_ACTIVITY_RECEIVE_DOWNLOADERROR";

    //接收到网易云游戏发送消息广播
    public static final String DATA_ACTIVITY_ONMESSAGE_NETEASE = "DATA_ACTIVITY_ONMESSAGE_NETEASE";
    public static final String DATA_ACTIVITY_RECEIVE_NETEASEMSGSEND = "DATA_ACTIVITY_RECEIVE_NETEASEMSGSEND";

    //显示授权页面
    public static final String DATA_ACTIVITY_USERAUTH_DISPLAY= "DATA_ACTIVITY_USERAUTH_DISPLAY";
    //取消授权
    public static final String DATA_ACTIVITY_USERAUTH_CANCEL = "DATA_ACTIVITY_USERAUTH_CANCEL";
    //允许授权
    public static final String DATA_ACTIVITY_USERAUTH_APPROVE = "DATA_ACTIVITY_USERAUTH_APPROVE";

    //初始化SDK
    public static final String DATA_SDK_INIT_START = "DATA_SDK_INIT_START";
    //SDK初化成功
    public static final String DATA_SDK_INIT_OK = "DATA_SDK_INIT_OK";
    //SDK初化失败
    public static final String DATA_SDK_INIT_FAILED = "DATA_SDK_INIT_FAILED";

    //准备申请设备
    public static final String DATA_DEVICE_APPLY_START = "DATA_DEVICE_APPLY_START";
    //设备申请成功
    public static final String DATA_DEVICE_APPLY_OK = "DATA_DEVICE_APPLY_OK";
    //设备申请失败
    public static final String DATA_DEVICE_REQ_ERROR = "DATA_DEVICE_REQ_ERROR";
    //设备繁忙
    public static final String DATA_DEVICE_APPLY_BUSY = "DATA_DEVICE_APPLY_BUSY";
    //网络异常
    public static final String DATA_DEVICE_NET_ERROR = "DATA_DEVICE_NET_ERROR";
    //设备掉线
    public static final String DATA_DEVICE_APPLY_FAILED = "DATA_DEVICE_APPLY_FAILED";

    //准备开始游戏
    public static final String DATA_DEVICE_SEND_NOTICE = "DATA_DEVICE_SEND_NOTICE";

    //准备开始游戏
    public static final String DATA_VIDEO_READY_RECVING = "DATA_VIDEO_READY_RECVING";
    //成功开始游戏，接收视频
    public static final String DATA_VIDEO_START_RECVING = "DATA_VIDEO_START_RECVING";
    //开始游戏出错
    public static final String DATA_VIDEO_START_RECVINGERR = "DATA_VIDEO_START_RECVINGERR";
    //关闭游戏
    public static final String DATA_VIDEO_CLOSE = "DATA_VIDEO_CLOSE";

    //用户长时间未操作
    public static final String DATA_VIDEO_USER_LEAVE = "DATA_VIDEO_USER_LEAVE";

    //初始化广告成功
    public static final String DATA_AD_INIT_OK = "DATA_AD_INIT_OK";
    //初始化广告失败
    public static final String DATA_AD_INIT_FAILED = "DATA_AD_INIT_FAILED";

    //显示广告窗口
    public static final String DATA_AD_DIALOG_DISPLAY = "DATA_AD_DIALOG_DISPLAY";
    //取消广告，关闭窗口
    public static final String DATA_AD_DIALOG_CANCEL = "DATA_AD_DIALOG_CANCEL";
    //显示广告，关闭窗口
    public static final String DATA_AD_DIALOG_SUBMIT = "DATA_AD_DIALOG_SUBMIT";

    //广告行为
    public static final String DATA_AD_REWARD_LOADING = "DATA_AD_REWARD_LOADING";
    public static final String DATA_AD_REWARD_EMPTY = "DATA_AD_REWARD_EMPTY";
    public static final String DATA_AD_REWARD_READY = "DATA_AD_REWARD_READY";
    public static final String DATA_AD_REWARD_DISPLAY = "DATA_AD_REWARD_DISPLAY";
    public static final String DATA_AD_REWARD_VERIFY = "DATA_AD_REWARD_VERIFY";
    public static final String DATA_AD_REWARD_CLOSED = "DATA_AD_REWARD_CLOSED";
    public static final String DATA_AD_REWARD_CLICK = "DATA_AD_REWARD_CLICK";
    public static final String DATA_AD_REWARD_PLAYCOMPLETE = "DATA_AD_REWARD_PLAYCOMPLETE";

    public static final String DATA_AD_EXT_LOADING = "DATA_AD_EXT_LOADING";
    public static final String DATA_AD_EXT_EMPTY = "DATA_AD_EXT_EMPTY";
    public static final String DATA_AD_EXT_READY = "DATA_AD_EXT_READY";
    public static final String DATA_AD_EXT_DISPLAY = "DATA_AD_EXT_DISPLAY";
    public static final String DATA_AD_EXT_CLOSED = "DATA_AD_EXT_CLOSED";
    public static final String DATA_AD_EXT_CLICK = "DATA_AD_EXT_CLICK";

    public static final String DATA_AD_FEED_LOADING = "DATA_AD_FEED_LOADING";
    public static final String DATA_AD_FEED_EMPTY = "DATA_AD_FEED_EMPTY";
    public static final String DATA_AD_FEED_READY = "DATA_AD_FEED_READY";
    public static final String DATA_AD_FEED_DISPLAY = "DATA_AD_FEED_DISPLAY";
    public static final String DATA_AD_FEED_CLOSED = "DATA_AD_FEED_CLOSED";
    public static final String DATA_AD_FEED_CLICK = "DATA_AD_FEED_CLICK";

    public static final String DATA_GAME_PLAY_TIME = "DATA_GAME_PLAY_TIME";

    //耗时统计点
    public static final String DATA_TMDATA_SDKINIT_END = "DATA_TMDATA_TM1";
    public static final String DATA_TMDATA_DEVICE_START = "DATA_TMDATA_TM2";
    public static final String DATA_TMDATA_DEVICE_END = "DATA_TMDATA_TM3";
    public static final String DATA_TMDATA_VIDEO_START = "DATA_TMDATA_TM4";
    public static final String DATA_TMDATA_VIDEO_END = "DATA_TMDATA_TM5";
    //用户注册
    public static final String DATA_USER_REGIST_START = "DATA_USER_REGIST_START";
    public static final String DATA_USER_REGIST_SUCCESS = "DATA_USER_REGIST_SUCCESS";
    public static final String DATA_USER_REGIST_FAILED = "DATA_USER_REGIST_FAILED";

    //用户登录
    public static final String DATA_USER_LOGIN_START = "DATA_USER_LOGIN_START";
    public static final String DATA_USER_LOGIN_SUCCESS = "DATA_USER_LOGIN_SUCCESS";
    public static final String DATA_USER_LOGIN_FAILED = "DATA_USER_LOGIN_FAILED";
    public static final String DATA_USER_LOGIN_CACHE = "DATA_USER_LOGIN_CACHE";

    //获取验证码失败
    public static final String DATA_GET_CREDIT_FAILED = "DATA_GET_CREDIT_FAILED";

    //用户手机号登录
    public static final String DATA_USER_LOGINPHONE_START = "DATA_USER_LOGINPHONE_START";
    public static final String DATA_USER_LOGINPHONE_SUCCESS = "DATA_USER_LOGINPHONE_SUCCESS";
    public static final String DATA_USER_LOGINPHONE_SUCCESSREG = "DATA_USER_LOGINPHONE_SUCCESSREG";
    public static final String DATA_USER_LOGINPHONE_FAILED = "DATA_USER_LOGINPHONE_FAILED";

    //第三方帐号登录
    public static final String DATA_USER_LOGINUSIGN_START = "DATA_USER_LOGINUSIGN_START";
    public static final String DATA_USER_LOGINUSIGN_SUCCESS = "DATA_USER_LOGINUSIGN_SUCCESS";
    public static final String DATA_USER_LOGINUSIGN_SUCCESSREG = "DATA_USER_LOGINUSIGN_SUCCESSREG";
    public static final String DATA_USER_LOGINUSIGN_FAILED = "DATA_USER_LOGINUSIGN_FAILED";

    //三方实名认证账户登录
    public static final String DATA_REALNAME_AUTH_START = "DATA_REALNAME_AUTH_START";
    public static final String DATA_REALNAME_AUTH_SUCCESS = "DATA_REALNAME_AUTH_SUCCESS";
    public static final String DATA_REALNAME_AUTH_FAILED = "DATA_REALNAME_AUTH_FAILED";
    public static final String DATA_REALNAME_AUTH_ENTER = "DATA_REALNAME_AUTH_ENTER";

    //支付生成订单
    public static final String DATA_PAY_MAKETRADE_START = "DATA_PAY_MAKETRADE_START";
    public static final String DATA_PAY_MAKETRADE_SUCCESS = "DATA_PAY_MAKETRADE_SUCCESS";
    public static final String DATA_PAY_MAKETRADE_FAILED = "DATA_PAY_MAKETRADE_FAILED";

    //支付
    public static final String DATA_PAY_APP_FINISH = "DATA_PAY_APP_FINISH";

    //调用支付
    public static final String DATA_PAY_APP_START = "DATA_PAY_APP_START";
    public static final String DATA_PAY_APP_WXSTART = "DATA_PAY_APP_WXSTART";
    public static final String DATA_PAY_APP_ZFBSTART = "DATA_PAY_APP_ZFBSTART";
    public static final String DATA_PAY_APP_FAILED = "DATA_PAY_APP_FAILED";
    public static final String DATA_PAY_APP_WXFAILED = "DATA_PAY_APP_WXFAILED";
    public static final String DATA_PAY_APP_ZFBFAILED = "DATA_PAY_APP_ZFBFAILED";
    public static final String DATA_PAY_WEB_FAILED = "DATA_PAY_WEB_FAILED";


    //帐号界面显示
    public static final String DATA_DIALOG_PHONELOGIN_DISPLAY = "DATA_DIALOG_PHLOGIN_DISPLAY";
    public static final String DATA_DIALOG_PWDLOGIN_DISPLAY = "DATA_DIALOG_PWLOGIN_DISPLAY";
    public static final String DATA_DIALOG_REGIST_DISPLAY = "DATA_DIALOG_REGIST_DISPLAY";
    public static final String DATA_DIALOG_FORGET_DISPLAY = "DATA_DIALOG_FORGET_DISPLAY";
    //帐号界面关闭
    public static final String DATA_DIALOG_ACCOUNT_CLOSE = "DATA_DIALOG_ACCOUNT_CLOSE";

    //支付界面
    public static final String DATA_DIALOG_PAY_DISPLAY = "DATA_DIALOG_PAY_DISPLAY";
    public static final String DATA_DIALOG_PAY_CLOSE = "DATA_DIALOG_PAY_CLOSE";

    //挽留弹窗
    public static final String DATA_DIALOG_EXITLIST_DISPLAY = "DATA_DIALOG_EXITLIST_DISPLAY";
    public static final String DATA_DIALOG_EXITLIST_EXITBTN = "DATA_DIALOG_EXITLIST_EXITBTN";
    public static final String DATA_DIALOG_EXITLIST_CANCELBTN = "DATA_DIALOG_EXITLIST_CANCELBTN";
    public static final String DATA_DIALOG_EXITLIST_CHANGEGAME = "DATA_DIALOG_EXITLIST_CHANGEGAME";

    //录屏
    public static final String DATA_RECORD_CLICK_STARTBTN = "DATA_RECORD_CLICK_STARTBTN";
    public static final String DATA_RECORD_CLICK_STOPBTN = "DATA_RECORD_CLICK_STOPBTN";
    public static final String DATA_RECORD_API_START = "DATA_RECORD_API_START";
    public static final String DATA_RECORD_API_PAUSE = "DATA_RECORD_API_PAUSE";
    public static final String DATA_RECORD_API_RESUME = "DATA_RECORD_API_RESUME";
    public static final String DATA_RECORD_API_STOP = "DATA_RECORD_API_STOP";
    public static final String DATA_RECORD_API_UPLOAD = "DATA_RECORD_API_UPLOAD";
    public static final String DATA_RECORD_API_PUBLISH = "DATA_RECORD_API_PUBLISH";
    public static final String DATA_RECORD_API_SUCCESS = "DATA_RECORD_API_SUCCESS";
    public static final String DATA_RECORD_API_ERR = "DATA_RECORD_API_ERR";
    public static final String DATA_RECORD_MINTIME_DISPLAY = "DATA_RECORD_MINTIME_DISPLAY";
    public static final String DATA_RECORD_FINISHED_DISPLAY = "DATA_RECORD_FINISHED_DISPLAY";
    public static final String DATA_RECORD_FINISHED_SUBBTN = "DATA_RECORD_FINISHED_SUBBTN";
    public static final String DATA_RECORD_FINISHED_CALBTN = "DATA_RECORD_FINISHED_CALBTN";
    public static final String DATA_RECORD_PUBLISH_DISPLAY = "DATA_RECORD_PUBLISH_DISPLAY";
    public static final String DATA_RECORD_PUBLISH_PUBBTN = "DATA_RECORD_PUBLISH_PUBBTN";
    public static final String DATA_RECORD_PUBLISH_BACKBTN = "DATA_RECORD_PUBLISH_BACKBTN";
    public static final String DATA_RECORD_PUBLISH_DESTORY = "DATA_RECORD_PUBLISH_DESTORY";
    public static final String DATA_RECORD_PUBLISH_SUCCTOAST = "DATA_RECORD_PUBLISH_SUCCTOAST";

    //录屏云服务
    public static final String DATA_CLOUDSER_REC_REQUEST = "DATA_CLOUDSER_REC_REQUEST";
    public static final String DATA_CLOUDSER_REC_START = "DATA_CLOUDSER_REC_START";
    public static final String DATA_CLOUDSER_REC_STOP = "DATA_CLOUDSER_REC_STOP";
    public static final String DATA_CLOUDSER_UPLOAD_VIDEO = "DATA_CLOUDSER_UPLOAD_VIDEO";
    public static final String DATA_CLOUDSER_UPLOAD_INFO = "DATA_CLOUDSER_UPLOAD_INFO";

    //接收到广播强制关闭云游戏
    public static final String DATA_BROADCAST_EXIT_GAME = "DATA_BROADCAST_EXIT_GAME";

    public static String getDeviceEventCode(int code){
        if (code == APIConstants.APPLY_DEVICE_SUCCESS){
            return DATA_DEVICE_APPLY_OK;
        }else if(code == APIConstants.ERROR_DEVICE_BUSY){
            return DATA_DEVICE_APPLY_BUSY;
        }else if(code == APIConstants.ERROR_NETWORK){
            return DATA_DEVICE_NET_ERROR;
        }

        //设备请求错误
        return DATA_DEVICE_REQ_ERROR;
    }

    public static String getGameEventCode(int code){
        if (code == APIConstants.CONNECT_DEVICE_SUCCESS || code == APIConstants.RECONNECT_DEVICE_SUCCESS){
            return DATA_VIDEO_START_RECVING;
        }else if(code == APIConstants.RELEASE_SUCCESS ){
            return DATA_VIDEO_CLOSE;
        }
        return DATA_VIDEO_START_RECVINGERR;
    }


    // 新定义事件
    //正常流程事件
    public static final int TYPE_TRACE_PROCE_NORMAL = 1;
    //非正常流程事件，失败类型
    public static final int TYPE_TRACE_PROCE_FAIL = 2;
    //非正常流程事件，错误类型
    public static final int TYPE_TRACE_PROCE_ERROR = 3;
    //正常流程事件
    //SDK被成功加载
    public static final String DATA_SDK_LOADED_TRACE = "DATA_SDK_LOADED";
    //SDK调⽤初始化函数
    public static final String DATA_SDK_INIT_TRACE = "DATA_SDK_INIT";
    //调⽤初始化函数完成,可以申请设备
    public static final String DATA_SDK_INITSUCC_TRACE = "DATA_SDK_INITSUCC";
    //开始申请设备
    public static final String DATA_DEVICE_APPLY_START_TRACE = "DATA_DEVICE_APPLY_START";
    //申请设备成功
    public static final String DATA_DEVICE_APPLY_OK_TRACE = "DATA_DEVICE_APPLY_OK";
    //开始申请视频流
    public static final String DATA_VIDEO_READY_RECVING_TRACE = "DATA_VIDEO_READY_RECVING";
    //成功获取视频流
    public static final String DATA_VIDEO_START_RECVING_TRACE = "DATA_VIDEO_START_RECVING";
    //关闭视频流
    public static final String DATA_VIDEO_CLOSE_TRACE = "DATA_VIDEO_CLOSE";

    //⾮正常流程事件 -- 失败类型
    //SDK调⽤初始化函数失败或出现错误
    public static final String DATA_SDK_INITERROR_TRACE = "DATA_SDK_INITERROR";
    //申请设备超时
    public static final String DATA_DEVICE_APPLY_TIMEOUT_TRACE = "DATA_DEVICE_APPLY_TIMEOUT";

    //⾮正常流程事件 -- 错误类型
    //设备申请失败
    public static final String DATA_DEVICE_APPLY_FAILED_TRACE = "DATA_DEVICE_APPLY_FAILED";
    //CorpKey不存在，对应设备申请接⼝错误码：10000
    public static final String DATA_CHECK_FAILED_CORPKEY_TRACE = "DATA_CHECK_FAILED_CORPKEY";
    //pkgname错误，APP不存在，对应设备申请接⼝错 误码：10001PkgName错误，即APP不存在
    public static final String DATA_DEVICE_APP_UNDEFINE_TRACE = "DATA_DEVICE_APP_UNDEFINE";
    //未授权的APP访问，即该APP未授权给该企业使⽤， 对应设备申请接⼝错误码：10002
    public static final String DATA_DEVICE_APP_NOTMATCH_TRACE = "DATA_DEVICE_APP_NOTMATCH";
    //⽆空闲设备（设备已经安装APP但设备都在使⽤中未释放），对应设备申请接⼝错误码：10003
    public static final String DATA_DEVICE_APPLY_BUSY_TRACE = "DATA_DEVICE_APPLY_BUSY";
    //APP未安装到相关设备，对应设备申请接⼝错误码： 10004
    public static final String DATA_DEVICE_APP_NOTINSTALL_TRACE = "DATA_DEVICE_APP_NOTINSTALL";
    //该企业未分配设备，对应设备申请接⼝错误码： 10005
    public static final String DATA_DEVICE_ERROR_NODEVICE_TRACE = "DATA_DEVICE_ERROR_NODEVICE";
    //IAAS调⽤错误，对应设备申请接⼝错误码：10006
    public static final String DATA_DEVICE_ERROR_IAAS_TRACE = "DATA_DEVICE_ERROR_IAAS";
    //未定义错误，对应设备申请接⼝未定义的错误码
    public static final String DATA_DEVICE_ERROR_UNKNOWN_TRACE = "DATA_DEVICE_ERROR_UNKNOWN";
    //游戏启动错误
    public static final String DATA_DEVICE_APP_FAILED_TRACE = "DATA_DEVICE_APP_FAILED";
    //调⽤底层IaaS供应商SDK的播放视频函数错误
    public static final String DATA_RUNTIME_SDK_ERROR_CALLPLAY_TRACE = "DATA_RUNTIME_SDK_ERROR_CALLPLAY";
    //视频播放中⽹络出错
    public static final String DATA_VIDEO_NET_CONN_ERROR_TRACE = "DATA_VIDEO_NET_CONN_ERROR";
    //视频播放中出现了⾮⽹络错误，是IaaS供应商已定义的错误
    public static final String DATA_VIDEO_FAILED_PLAYING_TRACE = "DATA_VIDEO_FAILED_PLAYING";
    //视频播放中出现了⾮⽹络错误，是IaaS供应商未定义 的错误
    public static final String DATA_VIDEO_FAILED_UNKNOWN_TRACE = "DATA_VIDEO_FAILED_UNKNOWN";


}
