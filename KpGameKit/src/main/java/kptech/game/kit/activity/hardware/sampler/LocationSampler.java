package kptech.game.kit.activity.hardware.sampler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import kptech.game.kit.SensorConstants;
import kptech.game.kit.activity.permission.PermissionHelper;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by zhouzhiyong on 20-4-14.
 * Project: GameBoxSDK
 */
public class LocationSampler extends Sampler {

    Activity mActivity;

    public LocationSampler(Activity context, SamplingCallback callback) {
        super(context, callback);
        this.mActivity = context;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {
        sendLocation();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public String[] getRequestPermission() {
        return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    }


    @Override
    public void waitGrantPermission() {

    }

    private void sendLocation(){
        try {
            if (callback != null){
                Location l =  getLocation();
                callback.onSensorSamplerData(SensorConstants.HARDWARE_ID_LOCATION,
                        SensorConstants.HARDWARE_ID_LOCATION,
                        (float)l.getLongitude(),// longitude
                        (float)l.getLatitude(),// latitude
                        (float)l.getAltitude(),// altitude
                        0.0f,// floor
                        0.0f,// horizontalaccuracy
                        0.0f, // speed
                        0.0f // direction
                );
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 定位：得到位置对象
     * @return
     */
    @SuppressLint("MissingPermission")
    private Location getLocation() {
        //获取地理位置管理器
        LocationManager mLocationManager = (LocationManager) mActivity.getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if ( PermissionHelper.hasPermissions(mActivity, getRequestPermission())) {
               Location l = mLocationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
        }
        return bestLocation;
    }


}
