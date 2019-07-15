package com.fifthera.ecmall.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.util.DisplayMetrics;

import java.io.File;
import java.net.NetworkInterface;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

/**
 * 设备相关工具类
 *
 * @author LeoWang
 * @date 2018/2/1
 */
public final class DeviceUtils {

    private DeviceUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    private static String mMacAddress = "";

    /**
     * 判断设备是否 root
     *
     * @return the boolean{@code true}: 是<br>{@code false}: 否
     */
    public static boolean isDeviceRooted() {
        String su = "su";
        String[] locations = {
                "/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/"
        };
        for (String location : locations) {
            if (new File(location + su).exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取设备系统版本号
     *
     * @return 设备系统版本号
     */
    public static String getSDKVersionName() {
        String result = "";
        try {
            result = Build.VERSION.RELEASE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtil.isEmpty(result) ? "" : result;
    }

    /**
     * 获取设备系统版本码
     *
     * @return 设备系统版本码
     */
    public static int getSDKVersionCode() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取设备 AndroidID
     *
     * @return AndroidID
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID() {
        return Settings.Secure.getString(
                ContextUtil.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    /**
     * 获取设备 MAC 地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />}</p>
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET" />}</p>
     *
     * @return MAC 地址
     */
    public static String getMacAddress() {
        if (StringUtil.isEmpty(mMacAddress)
                || StringUtil.equals(mMacAddress, "02:00:00:00:00:00")) {
            String macAddress = getMacAddressByWifiInfo();
            if (!"02:00:00:00:00:00".equals(macAddress)) {
                return mMacAddress;
            }
            macAddress = getMacAddressByNetworkInterface();
            if (!"02:00:00:00:00:00".equals(macAddress)) {
                mMacAddress = macAddress;
                return mMacAddress;
            }
            macAddress = getMacAddressByFile();
            if (!"02:00:00:00:00:00".equals(macAddress)) {
                mMacAddress = macAddress;
                return mMacAddress;
            }
        }
        return mMacAddress;
    }

    /**
     * 获取设备 MAC 地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />}</p>
     *
     * @return MAC 地址
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    private static String getMacAddressByWifiInfo() {
        try {
            @SuppressLint("WifiManagerLeak")
            WifiManager wifi =
                    (WifiManager) ContextUtil.getContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi != null) {
                WifiInfo info = wifi.getConnectionInfo();
                if (info != null) return info.getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取设备 MAC 地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET" />}</p>
     *
     * @return MAC 地址
     */
    private static String getMacAddressByNetworkInterface() {
        try {
            List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nis) {
                if (!ni.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = ni.getHardwareAddress();
                if (macBytes != null && macBytes.length > 0) {
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02x:", b));
                    }
                    return res1.deleteCharAt(res1.length() - 1).toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取设备 MAC 地址
     *
     * @return MAC 地址
     */
    private static String getMacAddressByFile() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("getprop wifi.interface", false);
        if (result.result == 0) {
            String name = result.successMsg;
            if (name != null) {
                result = ShellUtils.execCmd("cat /sys/class/net/" + name + "/address", false);
                if (result.result == 0) {
                    if (result.successMsg != null) {
                        return result.successMsg;
                    }
                }
            }
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 根据 获取 ip 地址
     *
     * @return ip 地址
     */
    @SuppressLint("MissingPermission")
    public static String getIpAddress() {
        String result = "";
        try {
            WifiManager wm = (WifiManager) ContextUtil.getContext().getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInf = null;
            if (wm != null) {
                wifiInf = wm.getConnectionInfo();
                result = intToIp(wifiInf.getIpAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtil.isEmpty(result) ? "" : result;
    }

    private static String intToIp(int ipInt) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return String.valueOf(((ipInt >> 24) & 0xff))
                    + '.'
                    + ((ipInt >> 16) & 0xff)
                    + '.'
                    + ((ipInt >> 8) & 0xff)
                    + '.'
                    + (ipInt & 0xff);
        }
        return String.valueOf(ipInt & 0xff)
                + '.'
                + ((ipInt >> 8) & 0xff)
                + '.'
                + ((ipInt >> 16) & 0xff)
                + '.'
                + ((ipInt >> 24) & 0xff);
    }

    /**
     * 获取设备厂商
     * <p>如 Xiaomi</p>
     *
     * @return 设备厂商
     */

    public static String getManufacturer() {
        String result = "";
        try {
            result = Build.MANUFACTURER;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtil.isEmpty(result) ? "" : result;
    }

    /**
     * 获取设备型号
     * <p>如 MI2SC</p>
     *
     * @return 设备型号
     */
    public static String getModel() {
        String model = Build.MODEL;
        if (model != null) {
            model = model.trim().replaceAll("\\s*", "");
        } else {
            model = "";
        }
        return model;
    }

    /**
     * 获取 设备厂商 + 设备型号
     * <p>如 vivo x20</p>
     *
     * @return 设备厂商 + 设备型号
     */
    public static String getDeviceModelAndManufacturerName() {
        String result = "";
        try {
            String manufacturer = getManufacturer();
            String model = getModel();
            if (model.startsWith(manufacturer)) {
                result = model;
            } else {
                result = manufacturer + model;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取 手机品牌
     *
     * @return 手机品牌
     */
    public static String getDeviceBrand() {
        String result = "";
        try {
            result = Build.BRAND;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtil.isEmpty(result) ? "" : result;
    }

    /**
     * 获取 蓝牙
     *
     * @return 蓝牙
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getBlueMac() {
        try {
            BluetoothAdapter bluetoothAdapter = null; // Local Bluetooth adapter
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            return bluetoothAdapter != null
                    || StringUtil.isNotEmpty(bluetoothAdapter.getAddress())
                    ? bluetoothAdapter.getAddress() : "";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 从 property 获取制造商信息
     *
     * @return 制造商信息
     */
    public static String getManufactureFromProp() {
        return ShellUtils.execCmd("getprop ro.product.brand", false).successMsg;
    }

    //获取存储的总大小
    public static long getTotalInternalMemorySize() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static String getScreenSize() {
        DisplayMetrics dm = ContextUtil.getContext().getResources().getDisplayMetrics();
        return dm.widthPixels + "X" + dm.heightPixels;
    }

    /**
     * 获取 设备序列号
     *
     * @return 设备序列号
     */
    public static String getSerialNumber() {
        @SuppressLint("HardwareIds") String result = Build.SERIAL;
        return StringUtil.isEmpty(result) ? "" : result;
    }

    /**
     * 获取cpu型号
     *
     * @return cpu型号
     */
    public static String getCpuModel() {
        String result = "";
        try {
            result = Build.CPU_ABI;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtil.isEmpty(result) ? "" : result;
    }

    public static String getMarkId() {
        String markId = "";
        try {
            markId = "35"
                    + Build.BOARD.length() % 10
                    + Build.BRAND.length() % 10
                    + Build.CPU_ABI.length() % 10
                    + Build.DEVICE.length() % 10
                    + Build.DISPLAY.length() % 10
                    + Build.HOST.length() % 10
                    + Build.ID.length() % 10
                    + Build.MANUFACTURER.length() % 10
                    + Build.MODEL.length() % 10
                    + Build.PRODUCT.length() % 10
                    + Build.TAGS.length() % 10
                    + Build.TYPE.length() % 10
                    + Build.USER.length() % 10;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return markId;
    }

    /**
     * 关机
     * <p>需要 root 权限或者系统权限 {@code <android:sharedUserId="android.uid.system" />}</p>
     */
    public static void shutdown() {
        ShellUtils.execCmd("reboot -p", true);
        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        ContextUtil.getContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 重启
     * <p>需要 root 权限或者系统权限 {@code <android:sharedUserId="android.uid.system" />}</p>
     */
    public static void reboot() {
        ShellUtils.execCmd("reboot", true);
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        ContextUtil.getContext().sendBroadcast(intent);
    }

    /**
     * 重启
     * <p>需系统权限 {@code <android:sharedUserId="android.uid.system" />}</p>
     *
     * @param reason 传递给内核来请求特殊的引导模式，如"recovery"
     */
    public static void reboot(final String reason) {
        PowerManager mPowerManager =
                (PowerManager) ContextUtil.getContext().getSystemService(Context.POWER_SERVICE);
        try {
            if (mPowerManager == null) return;
            mPowerManager.reboot(reason);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重启到 recovery
     * <p>需要 root 权限</p>
     */
    public static void reboot2Recovery() {
        ShellUtils.execCmd("reboot recovery", true);
    }

    /**
     * 重启到 bootloader
     * <p>需要 root 权限</p>
     */
    public static void reboot2Bootloader() {
        ShellUtils.execCmd("reboot bootloader", true);
    }
}
