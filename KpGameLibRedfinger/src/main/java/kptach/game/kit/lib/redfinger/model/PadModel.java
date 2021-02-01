package kptach.game.kit.lib.redfinger.model;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import kptach.game.kit.lib.redfinger.utils.DeviceUtils;

public class PadModel {
    private int mParamsNum = 0;
    public String brand;
    public String model;
    public String manufacturer;
    public String wifimac;
    public String serialno;
    public String baseband;
    public String displayId;
    public String device;
    public String fingerprint;
    public String productName;
    public String buildId;
    public String buildHost;
    public String bootloader;
    public String buildTags;
    public String buildType;
    public String buildVersionInc;
    public String buildDateUtc;
    public String buildDescription;
    public String imei;
    public String phonenum;
    public String iccid;
    public String imsi;
    public String lac;
    public String cid;
    public String mcc;
    public String mnc;
    public String bsss;
    public String simserial;
    public String networkor;
    public String simopename;
    public String countrycode;
    public String simstate;
    public String imeisv;
    public String esn;
    public String meid;
    public String spn;
    public String wifiname;
    public String androidid;
    public String board;
    public String bssid;
    public String rawData;

    private PadModel() {
    }

    public static PadModel createPadModel(Context context){
        PadModel padModel = null;
        try {
            padModel = new PadModel();
            padModel.imei = DeviceUtils.getIMEI(context);
            padModel.androidid = DeviceUtils.getAndroidId(context);

            padModel.brand = DeviceUtils.getDeviceBrand();
            padModel.model = DeviceUtils.getDeviceModel();
            padModel.manufacturer = DeviceUtils.getDeviceManufacturer();
            padModel.bootloader = DeviceUtils.getDeviceBootloader();

            padModel.serialno = DeviceUtils.getSERIAL();

            padModel.board = DeviceUtils.getDeviceBoard();
            padModel.device = DeviceUtils.getDeviceDevice();
            padModel.fingerprint = DeviceUtils.getDeviceFingerprint();
            padModel.productName = DeviceUtils.getDeviceProduct();

            padModel.imsi = DeviceUtils.getIMSI(context);
            padModel.wifimac = DeviceUtils.getWifiMacAddress(context);
            padModel.wifiname = DeviceUtils.getWifiName(context);
            padModel.bssid = DeviceUtils.getBSSID(context);

            padModel.buildId = DeviceUtils.getBuildId();
            padModel.buildHost = DeviceUtils.getBuildHost();
            padModel.buildTags = DeviceUtils.getBuildTags();
            padModel.buildType = DeviceUtils.getBuildType();

            padModel.buildDescription = DeviceUtils.getVersionInc();
        }catch (Exception e){
            e.printStackTrace();
        }
        return padModel;
    }

    public JSONObject combPadModel() {
        PadModel model = this;
        JSONObject obj = null;
        if (model != null) {
            obj = new JSONObject();
            this.mParamsNum = 0;
            this.handlerPadModelParams(obj, "brand", model.brand);
            this.handlerPadModelParams(obj, "model", model.model);
            this.handlerPadModelParams(obj, "manufacturer", model.manufacturer);
            this.handlerPadModelParams(obj, "wifimac", model.wifimac);
            this.handlerPadModelParams(obj, "serialno", model.serialno);
            this.handlerPadModelParams(obj, "baseband", model.baseband);
            this.handlerPadModelParams(obj, "displayId", model.displayId);
            this.handlerPadModelParams(obj, "device", model.device);
            this.handlerPadModelParams(obj, "fingerprint", model.fingerprint);
            this.handlerPadModelParams(obj, "productName", model.productName);
            this.handlerPadModelParams(obj, "buildId", model.buildId);
            this.handlerPadModelParams(obj, "buildHost", model.buildHost);
            this.handlerPadModelParams(obj, "bootloader", model.bootloader);
            this.handlerPadModelParams(obj, "buildTags", model.buildTags);
            this.handlerPadModelParams(obj, "buildType", model.buildType);
            this.handlerPadModelParams(obj, "buildVersionInc", model.buildVersionInc);
            this.handlerPadModelParams(obj, "buildDateUtc", model.buildDateUtc);
            this.handlerPadModelParams(obj, "buildDescription", model.buildDescription);
            this.handlerPadModelParams(obj, "imei", model.imei);
            this.handlerPadModelParams(obj, "phonenum", model.phonenum);
            this.handlerPadModelParams(obj, "iccid", model.iccid);
            this.handlerPadModelParams(obj, "imsi", model.imsi);
            this.handlerPadModelParams(obj, "lac", model.lac);
            this.handlerPadModelParams(obj, "cid", model.cid);
            this.handlerPadModelParams(obj, "mcc", model.mcc);
            this.handlerPadModelParams(obj, "mnc", model.mnc);
            this.handlerPadModelParams(obj, "bsss", model.bsss);
            this.handlerPadModelParams(obj, "simserial", model.simserial);
            this.handlerPadModelParams(obj, "networkor", model.networkor);
            this.handlerPadModelParams(obj, "simopename", model.simopename);
            this.handlerPadModelParams(obj, "countrycode", model.countrycode);
            this.handlerPadModelParams(obj, "simstate", model.simstate);
            this.handlerPadModelParams(obj, "imeisv", model.imeisv);
            this.handlerPadModelParams(obj, "esn", model.esn);
            this.handlerPadModelParams(obj, "meid", model.meid);
            this.handlerPadModelParams(obj, "spn", model.spn);
            this.handlerPadModelParams(obj, "wifiname", model.wifiname);
            this.handlerPadModelParams(obj, "androidid", model.androidid);
            this.handlerPadModelParams(obj, "board", model.board);
            this.handlerPadModelParams(obj, "bssid", model.bssid);
        }

        return obj;
    }

    private void handlerPadModelParams(JSONObject var1, String var2, String var3) {
        if (var1 != null && !TextUtils.isEmpty(var2) && !TextUtils.isEmpty(var3)) {
            try {
                var1.put(var2, var3);
                ++this.mParamsNum;
            } catch (JSONException var5) {
                var5.printStackTrace();
            }
        }

    }
}
