# 云游戏Android SDK Paas3.0开发版 接入文档



[TOC]

## 1. 简介

​		<font size=4>**云游戏 Android SDK **主要功能是为您的 **APP** 赋予游戏试玩能力，用户在运营平台配置好试玩游戏之后，客户端 **APP** 通过接入 **云游戏SDK** 就可以进行试玩操作，提升游戏的分化转发率</font>

​		<font color=#000000 size=3>云游戏 Android SDK 文档接入，包括云游戏 Android SDK 发行版文档和完整的 SDKDemo 实例 </font>

<font color=#000000 size=3>以下使用 <strong><font style=blod size=4><SDK_PATH></font></strong> 表示SDK解压根目录</font>

- 示例项目源码： <SDK_PATH>/GamePaas3Demo , SDK接入的 Demo 项目，帮助您快速熟悉并运用云游戏 Android SDK
- 用户使用手册：本文档



## 2. 运行环境

- <font size=3>可运行于 <strong><font size=4>Android 7.0( API Level 24)及以上版本</font></strong> </font>
- <font size=3>AAR包体大小：<strong><font size=4>2.9M</font></strong></font>



## 3. SDK导入及配置

- #### 申请CorpKey

  <a name=3-1><font color=black>开发者请向服务商申请并获取 <strong><font size=4 >云游戏 SDK</font></strong> 使用的 <strong><font size=4 >CorpKey</font></strong></font></a>

- #### Maven方式引入(推荐)	

  1、SDK导入，在工程目录下的build.gradle文件中配置

  ```java
  allprojects {
      repositories {
          ......
  
          //添加maven库
          maven { url "https://maven.kuaipantech.com/repository/maven-releases/" }
      }
  }
  ```

  2、在app下的build.gradle文件中依赖中配置

  ```java
  android {
  	  ......
  	
  	  compileOptions {
          sourceCompatibility JavaVersion.VERSION_1_8
          targetCompatibility JavaVersion.VERSION_1_8
      }
  }
  
  dependencies {
      ......
  
      //添加依赖
      implementation "kptech.game.kit:gamebox:1.1.3.1”
  }
  ```

  

- #### SDK权限配置

  ```java
     	<!--必要权限-->
      <uses-permission android:name="android.permission.INTERNET" />
      <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
      <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  ```
  
  
  
- #### 代码混淆

  ```java
    # 云游戏相关
    -keep class kptech.game.kit.** { *; }
    -keep class kptech.cloud.kit.msg.** { *; }
    -keep class com.kptach.lib.game.huawei.** { *; }
    -keep class com.huawei.cloudgame.** { *; }
  ```
  
  

## 4. 初始化

在Application的onCreate方法中，调用**GameBoxManager**对象如下代码完成初始化：

```java
   /**
     * 初始化sdk
     * @param Application app
     * @param String corpKey  由服务商提供
     * @param IAPICallback callback 初始化状态回调
     */
    public void init(Application app, String corpkey, HashMap params, IAPICallback callback);

		//示例代码：
    public class CustomerApplication extends Application{

      //由服务商提供
      public static final String corpKey = "";

      @Override
        public void onCreate() {
            super.onCreate();

            //控制台是否输出
            GameBoxManager.setDebug(true);
          	SDKParams params = new SDKParams();
            //配置画质信息
            params.put("", );
          	//配置游戏信息
            params.put("", )
            //初始化
            GameBoxManager.getInstance().init(this, corpKey, params, null);
        }
    }

```

打开调试日志

```java
	 /**
	  * 打开调试日志，默认为false, 未开启
		*/
		GameBoxManager.setDebug(true);
```



## 5. 启动云游戏

- #### 申请试玩设备

  试玩某一款游戏时传入试玩游戏信息，使用 **GameBoxManager**申请设备。

  如申请设备成功则返回状态码：`APIConstants.API_CALL_SUCCESS`和DeviceControl用于控制设备，否则返回对应错误码。

  ```java
  	/**
  		* 申请云设备
  		* @param GameInfo game 云游戏信息，主传入包名
  		* @param APICallback callback 申请状态回调，申请成功后可以获取到DeviceControl对象，
  		* 申请失败时反回相应错误码。
  		*/
  		public void applyCloudDevice(Activity activity, String pkgName, APICallback<DeviceControl> callback);
  
  		//示例代码
  		GameBoxManager.getInstance().applyCloudDevice(activity, pkgName, 
      			new APICallback<DeviceControl>(){
                  @Override
              public void onAPICallback(DeviceControl deviceControl, int code) {
                  if (code == APIConstants.API_CALL_SUCCESS) {
                    	//申请成功
                    	deviceControl.startGame(activity, containerId);
                  }else{
                    	//申请云设备失败
                  }
              }
  		})
  ```

- #### 启动游戏试玩

  在设备申请成功后，可使用返回的**DeviceControl**对象启动游戏试玩。

  游戏试玩界面需要添加到一个Activity中，需要传入**ViewGroup**的id供添加试玩View。

  ```java
  /**
  	* 启动云游戏
  	* @param int containerId 视频ViewGroup
  	*/
  	public void startGame(@NonNull Activity activity, @IdRes int containerId )
  ```
  
- #### 退出游戏试玩

  在退出试玩时调用，否则无法进行下一次试玩，建议在**Activity onDestory**中调用，**DeviceControl**对象方法

  ```java
      /**
       * 停止试玩，在退出试玩的时候必须回调，否则无法进行下一次试玩
       */
      public void stopGame();
  ```

  

## 6. 云游戏设置

​		申请到云设备后，可以通过调用相关设置方法，对游戏相关功能进行配置或通过监听器接收到相关数据。以下方法为DeviceControl对象方法。

- #### 游戏画质切换

  设置试玩清晰度，更高的清晰度试玩效果更加，但是相对消耗流量也更多。
  
  ```java
  /**
       * 调整试玩的码率
       * @param level 等级，目前支持4档
       * {@link APIConstants#DEVICE_VIDEO_QUALITY_AUTO} 自动
       * {@link APIConstants#DEVICE_VIDEO_QUALITY_HD} 高清
       * {@link APIConstants#DEVICE_VIDEO_QUALITY_ORDINARY} 标清
       * {@link APIConstants#DEVICE_VIDEO_QUALITY_LS} 流畅
       */
      public void switchQuality(@APIConstants.VideoQuality String level);
  
      /**
       * 获取当前画面质量
       */
      public String getVideoQuality();
  ```
  
- #### 游戏声音开关

  设置静音/取消静音。

  ```java
     /**
       * 试玩声音打开或关闭
       * @param audioSwitch， true:开启声音、false:关闭声音
       */
      public void setAudioSwitch(boolean audioSwitch);
  
      /**
       * 获取声音是否打开
       */
      public boolean isAudioEnable();
  ```

- #### 使用真机输入法

  云机有输入事件时，会拉起真机的输入法完成输入。
  
  ```java
   /**
    * 使用客户端本地输入法
    * @param enable true:使用本地输入法，false使用云端设备输入法，默认为true
    */
    public void enableRemoteIme(boolean enable);
  ```
  
- #### 状态监听器

  通过注册状态监听器，实现接入状态的监听，状态码请参考云手游状态码。

  ```java
   /**
     * 设置状态监听
     * @param listener    PlayListener 回调实现类
     */
    public void registerPlayStateListener(PlayStateListener listener);
  
  
    /**
     * 状态监听接口
     */
    public interface PlayStateListener {
        /**
         * 状态变化
         * @param code 状态码，参考附录
         * @param msg 消息
         */
        void onNotify(int code, String msg);
  
    }
  ```

- #### 数据监听器

  通过注册云游戏数据监听器，获取云游戏发送到真机侧的数据。

  ```java
   /**
     * 设置游戏数据监听
     * @param listener    PlayDataListener 回调实现类
     */
    public void registerPlayDataListener(PlayDataListener listener);
  
  
    /**
     * 游戏数据监听器
     */
    public interface PlayDataListener {
        
  
        /**
         * 无操作超时回调
         * @param type 类型。1为后台，2为前台
         * @param timeout 超时时长，单位s
         */
        boolean onNoOpsTimeout(int type, long timeout);
  
        /**
         * 视频流信息
         * {"refresh_fps":"30","refresh_ping":"10","refresh_bitrate":"10000000"}
         */
        void onDataInfo(String dataInfo);
    }
  ```
  
- #### 屏幕显示监听器

  通过注册云游戏画面方向变化监听器，获取游戏画面的变化。
  
  ```java
   /**
     * 设置屏幕变化监听
     * @param listener    PlayScreenListener 回调实现类
     */
    public void registerPlayScreenListener(PlayScreenListener listener);
  
  
    /**
     * 屏幕变化监听器
     */
    public interface PlayScreenListener {
        /**
           * 分辨率
           * @param width
           * @param height
           */
          void onVideoSizeChange(int width, int height);
  
          /**
           * 屏幕方向
           * @param orientation
           */
          void onOrientationChange (int orientation);
    }
  ```



## 7. 数据通道

​		`数据通道`是指在用户手机和云手机之间的连接，用于文本数据上下行数据传输，方便开发者进行和远程App的通讯。可用于远程登录、远程支付（H5）、消息传递等。

- #### 客户端发送消息

  在申请设备成功之后，通过`DeviceControl`发送数据

  ```java
      /**
       * 发送消息
       * @param event
       * @param data
       */
      void sendCloudMessage(String event, String data);
  ```

- #### 客户端接收消息

  在申请设备成功之后，通过`DeviceControl`注册`CloudMessageListener`监听，接收云手机发送的数据。
  
  ```java
      /**
       * 注册消息接收品
       * @param receiver
       */
      void registerCloudMessageListener(CloudMessageListener receiver);
  
  		/**
  		 * 消息接收回调接口
  		 */
      public interface CloudMessageListener {
         /**
          * 接收到消息
        	*/
          void onMessage(String event, String data);
      }
  ```



## 8. 排队机制

​		排队机制是在原有设备申请接口上新增的机制，在设备使用高峰期可以帮助产品挽留用户。排队过程中只能同时一款游戏进行排队。

- #### 申请排队

  申请设备时`playQueue`为`true`在设备不足的情况下会进入排队状态，返回状态码`APIConstants.WAITING_QUEUE`，此时无法调用`DeviceControl.startGame()`。同一时间只能有一款游戏进行排队，如在排队过程中有其他游戏进入队列则返回`APIConstants.ERROR_OTHER_DEVICE_WAITING`

  ```java
      /**
       * 根据游戏信息申请试玩设备
       *
       * @param gameInfo  游戏信息
       * @param playQueue 如果没有设备是否自动进入队列
       * @param callback  异步回调
       */
  GameBoxManager.getInstance().applyCloudDevice(@NonNull String pkgName, boolean playQueue, @NonNull new APICallback<DeviceControl>(){
                  @Override
              public void onAPICallback(DeviceControl deviceControl, int code) {
                  //回调, 返回状态码APIConstants.WAITING_QUEUE, 已进入队列
                
              }
      })
  ```

- #### 排队进度回调

  在游戏成功进入排队以后，可以定时检测队列状态

  ```java
  		/**
       * 加入队列，获取队列状态
       *
       * @param pkgName      游戏信息
       * @param checkInterval 队列检测时间间隔，单位秒
       * @param callback      回调
       */
       GameManager.getInstance().setQueueCallback(String pkgName, int checkInterval, APICallback<QueueRankInfo> callback);
       
  ```

  可以通过监听回调，获取当前队列的状态进度和事件队列信息：

  ```java
  	/** 
  		* 队列信息 
  		*/
  		public class QueueRankInfo {
  			  //游戏信息
  		    public GameInfo gameInfo;
  		    //当前排名
  		    public int queueRanking;
  		    //预估时间，单位秒，不准确
  		    public int queueWaitTime;
  		}
  ```

  队列事件：

  ```java
  	  /** 队列更新 */
      public static final int QUEUE_UPDATE = 1004;
      /** 排队成功 */
      public static final int QUEUE_SUCCESS = 1007;
      /** 队列退出 */
      public static final int QUEUE_EXIT = 1008;
      /** 排队失败 */
      public static final int QUEUE_FAILED = 1009;
      /** 不存在队列，直接申请*/
      public static final int QUEUE_NO_QUEUE = 1010;
  ```

- #### 退出队列

  退出队列接口，如果退出试玩，一定要调用此接口，否则肯会内存泄漏

  ```java
      /**
       * 退出队列
       */
      GameManager.getInstance().exitQueue() 
  ```

  

## 9. SDK更新记录

- 20200818: v1.1.1.0, 首次提交

- 20200821: v1.1.1.6, 增加数据打点

- 20200827: v1.1.1.7, 优化广告加载

- 20200827: v1.1.1.8, 优化数据打点、代码逻辑

- 20200901: v1.1.1.9, 增加下载功能.

- 20200921: v1.1.2.0, 修改信息流广告，修改Bug

- 20200921: v1.1.2.9, 增加联运登录和支付功能

- 20210420: v1.2.1.0, 优化联运登录和游戏体验

  

## 附录

**状态码说明**

| 状态码（10 进制） | 值                       | 状态信息         |
| ----------------- | ------------------------ | ---------------- |
| 1000              | APPLY_DEVICE_SUCCESS     | 申请设备成功     |
| 1001              | ERROR_APPLY_DEVICE       | 申请设备失败     |
| 1002              | CONNECT_DEVICE_SUCCESS   | 连接设备成功     |
| 1003              | RECONNECT_DEVICE_SUCCESS | 重连设备成功     |
| 1004              | ERROR_CONNECT_DEVICE     | 连接设备失败     |
| 1005              | RELEASE_SUCCESS          | 释放资源成功     |
| 1006              | ERROR_DEVICE_BUSY        | 设备占用         |
| 1007              | ERROR_NETWORK            | 网络异常         |
| 1008              | ERROR_SDK_INIT           | SDK初始化失败    |
| 1009              | ERROR_GAME_INFO          | 游戏信息错误     |
| 1012              | GAME_LOADING             | 游戏加载中       |
| 1013              | RECOVER_DATA_LOADING     | 数据恢复中       |
| 1014              | TIMEOUT_AVAILABLE_TIME   | 可用时间超时     |
| 1019              | ERROR_SDK_INNER          | 游戏数据内部错误 |
| 1020              | ERROR_CALL_API           | 调用API错误      |
| 65535             | ERROR_OTHER              | 其他错误         |
