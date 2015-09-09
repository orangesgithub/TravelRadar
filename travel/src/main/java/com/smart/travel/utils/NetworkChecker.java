package com.smart.travel.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by yfan10x on 2015/9/9.
 */
public class NetworkChecker {

    /**
     * 判断移动网络是否开启
     *
     * @param context
     * @return
     */
    public static boolean isNetEnabled(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            if (tm.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN) {
                Log.i("sjf", "移动网络已经开启");
                return true;
            }
        }
        Log.i("sjf", "移动网络还未开启");
        return false;
    }

    /**
     * 判断WIFI网络是否开启
     *
     * @param context
     * @return
     */
    public static boolean isWifiEnabled(Context context) {
        WifiManager wm = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (wm != null && wm.isWifiEnabled()) {
            Log.i("sjf", "Wifi网络已经开启");
            return true;
        }
        Log.i("sjf", "Wifi网络还未开启");
        return false;
    }

    /**
     * 判断移动网络是否连接成功
     *
     * @param context
     * @return
     */
    public static boolean isNetContected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (cm != null && info != null && info.isConnected()) {
            Log.i("sjf", "移动网络连接成功");
            return true;
        }
        Log.i("sjf", "移动网络连接失败");
        return false;
    }

    /**
     * 判断WIFI是否连接成功
     *
     * @param context
     * @return
     */
    public static boolean isWifiContected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (info != null && info.isConnected()) {
            Log.i("sjf", "Wifi网络连接成功");
            return true;
        }
        Log.i("sjf", "Wifi网络连接失败");
        return false;
    }

    /**
     * 判断移动网络和WIFI是否开启
     *
     * @param context
     * @return
     */
    public static boolean isNetWorkEnabled(Context context) {
        return (isNetEnabled(context) || isWifiEnabled(context));
    }

    /**
     * 判断移动网络和WIFI是否连接成功
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        return (isWifiContected(context) || isNetContected(context));
    }

}
