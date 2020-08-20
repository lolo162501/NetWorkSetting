package com.example.networksetting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class NetworkUtil {
    public static final int ETHERNET_STATE_DISABLED = 1;
    public static final int ETHERNET_STATE_ENABLED = 2;
    public static final int ETHERNET_STATE_UNKNOWN = 3;
    public static final int CONNECT_TYPE_INTERNET = 1;
    public static final int CONNECT_TYPE_ETHERNET = 9;

    public static int getConnectType(Context context) { // 取得連線狀態 ( 1 -> Internet , 9 -> Ethernet )
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null) {
            return connectivityManager.getActiveNetworkInfo().getType();
        }
        return -1;
    }

    private static Class<?> getEthernetManagerClass() {  // 取得連線狀態 ( 1 -> Internet , 9 -> Ethernet )
        Class<?> classz = null;
        try {
            classz = Class.forName("android.net.ethernet.EthernetManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classz;
    }

    private static Class<?> getIEthernetManagerClass() { // 許多方法都在這個class內 , (開關 ,狀態 ,模式)
        Class<?> classz = null;
        try {
            classz = Class.forName("android.net.ethernet.IEthernetManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classz;
    }

    private static Class<?> getEthernetDevInfoClass() { // Ethernet 設定 ip,mask,gateway,dns的方法都在這個class內
        Class<?> classz = null;
        try {
            classz = Class.forName("android.net.ethernet.EthernetDevInfo");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classz;
    }

    public static Object getEthernetManagerInstance() { // 取得 EthernetManager 的實例
        Object object = null;
        Method method = getEthernetMethod(getEthernetManagerClass(), "getInstance");
        if (method != null) {
            try {
                object = method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public static Object getEthernetServiceObject(Context context) { // 取得 EthernetManager 內的Service 實例
        Object ethernetServiceObject = null;
        try {
            String ETHERNET_SERVICE = (String) Context.class.getField("ETHERNET_SERVICE").get(null);
            Object ethernetManager = context.getSystemService(ETHERNET_SERVICE);
            Field mService = getEthernetManagerClass().getDeclaredField("mService");
            mService.setAccessible(true);
            ethernetServiceObject = mService.get(ethernetManager);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return ethernetServiceObject;
    }

    public static Method getEthernetMethod(Class<?> classz, String methodString) {
        Method method = null;
        try {
            method = classz.getDeclaredMethod(methodString);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    public static Method getEthernetMethod2(Class<?> classz, String methodString, Class... classes) {
        Method method = null;
        try {
            method = classz.getDeclaredMethod(methodString, classes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    public static void setEthernetEnabled(Context context, boolean enabled) { //  開關Ethernet
        Method method = getEthernetMethod2(getIEthernetManagerClass(), "setEthernetEnabled", boolean.class);
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                method.invoke(getEthernetServiceObject(context), enabled);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setWifiEnabled(Context context, boolean isEnabled) { //  開關WIFI
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(isEnabled);
    }

    public static int getEthernetState(Context context) { // 取得目前Ethernet 是否開啟
        Method method = getEthernetMethod(getIEthernetManagerClass(), "getEthernetState");
        int ethernetState = ETHERNET_STATE_UNKNOWN;
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                Object object = method.invoke(getEthernetServiceObject(context));
                ethernetState = (int) (object != null ? object : ETHERNET_STATE_UNKNOWN);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return ethernetState;
    }

    public static String getEthernetMode(Context context) { // 取得目前Ethernet為何種模式 ( dhcp , manual)
        Method method = getEthernetMethod(getIEthernetManagerClass(), "getEthernetMode");
        String ethernetMode = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                Object object = method.invoke(getEthernetServiceObject(context));
                ethernetMode = object != null ? (String) object : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return ethernetMode;
    }

    public static String getIpAddressFromDhcpEthernet(Context context) { //Ethernet dhcp 模式的 ip
        Method method = getEthernetMethod(getIEthernetManagerClass(), "getDhcpInfo");
        String ipAddress = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                DhcpInfo dhcpInfo = (DhcpInfo) method.invoke(getEthernetServiceObject(context));
                ipAddress = dhcpInfo != null ? Formatter.formatIpAddress(dhcpInfo.ipAddress) : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return ipAddress;
    }

    public static String getGatewayFromDhcpEthernet(Context context) {//Ethernet dhcp 模式的 gateway
        Method method = getEthernetMethod(getIEthernetManagerClass(), "getDhcpInfo");
        String gateway = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                DhcpInfo dhcpInfo = (DhcpInfo) method.invoke(getEthernetServiceObject(context));
                gateway = dhcpInfo != null ? Formatter.formatIpAddress(dhcpInfo.gateway) : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return gateway;
    }

    public static String getNetMaskFromDhcpEthernet(Context context) {//Ethernet dhcp 模式的 mask
        Method method = getEthernetMethod(getIEthernetManagerClass(), "getDhcpInfo");
        String netmask = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                DhcpInfo dhcpInfo = (DhcpInfo) method.invoke(getEthernetServiceObject(context));
                netmask = dhcpInfo != null ? Formatter.formatIpAddress(dhcpInfo.netmask) : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return netmask;
    }

    public static String getDns1FromDhcpEthernet(Context context) {//Ethernet dhcp 模式的 dns1
        Method method = getEthernetMethod(getIEthernetManagerClass(), "getDhcpInfo");
        String dns1 = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                DhcpInfo dhcpInfo = (DhcpInfo) method.invoke(getEthernetServiceObject(context));
                dns1 = dhcpInfo != null ? Formatter.formatIpAddress(dhcpInfo.dns1) : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return dns1;
    }

    public static String getDns2FromDhcpEthernet(Context context) {//Ethernet dhcp 模式的 dns2
        Method method = getEthernetMethod(getIEthernetManagerClass(), "getDhcpInfo");
        String dns2 = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                DhcpInfo dhcpInfo = (DhcpInfo) method.invoke(getEthernetServiceObject(context));
                dns2 = dhcpInfo != null ? Formatter.formatIpAddress(dhcpInfo.dns2) : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return dns2;
    }

    private static Object getSavedConfig() { // 要設定Ethernet 須先取得此實例
        Object object = null;
        Method method = getEthernetMethod(getEthernetManagerClass(), "getSavedConfig");
        if (method != null && getEthernetManagerInstance() != null) {
            try {
                object = method.invoke(getEthernetManagerInstance());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return object;
    }


    public static String getIpAddressFromManualEthernet(Context context) { //Ethernet static 模式的 ip
        Method method = getEthernetMethod(getEthernetDevInfoClass(), "getIpAddress");
        String ipAddress = "";
        if (method != null) {
            try {
                Object savedConfig = getSavedConfig();
                Object object = method.invoke(savedConfig);
                ipAddress = object != null ? (String) object : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return ipAddress;
    }

    public static void setEthernetMode(int value, String... strings) { // 變更模式 ( static -> dhcp  or dhcp -> static)
        try {
            Object object = getSavedConfig();
            Method setIpAddress = getEthernetMethod2(getEthernetDevInfoClass(), "setIpAddress", String.class);
            setIpAddress.invoke(object, strings[0]);
            getEthernetMethod2(getEthernetDevInfoClass(), "setGateWay", String.class).invoke(object, strings[1]);
            getEthernetMethod2(getEthernetDevInfoClass(), "setNetMask", String.class).invoke(object, strings[2]);
            getEthernetMethod2(getEthernetDevInfoClass(), "setDnsAddr", String.class).invoke(object, strings[3]);
            getEthernetMethod2(getEthernetDevInfoClass(), "setDns2Addr", String.class).invoke(object, strings[4]);
            getEthernetMethod2(getEthernetDevInfoClass(), "setMode", String.class).invoke(object, value == 1 ? "manual" : "dhcp");
            getEthernetMethod2(getEthernetManagerClass(), "updateDevInfo", object.getClass()).invoke(getEthernetManagerInstance(), object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static String getGatewayFromManualEthernet(Context context) { //Ethernet static 模式的 gateway
        Method method = getEthernetMethod(getEthernetDevInfoClass(), "getGateWay");
        String gateway = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                Object object = method.invoke(getSavedConfig());
                gateway = object != null ? (String) object : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return gateway;
    }

    public static String getNetMaskFromManualEthernet(Context context) {//Ethernet static 模式的 mask
        Method method = getEthernetMethod(getEthernetDevInfoClass(), "getNetMask");
        String netMask = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                Object object = method.invoke(getSavedConfig());
                netMask = object != null ? (String) object : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return netMask;
    }

    public static String getDnsAddressFromManualEthernet(Context context) {//Ethernet static 模式的 dns1
        Method method = getEthernetMethod(getEthernetDevInfoClass(), "getDnsAddr");
        String dnsAddress = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                Object object = method.invoke(getSavedConfig());
                dnsAddress = object != null ? (String) object : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return dnsAddress;
    }

    public static String getDns2AddressFromManualEthernet(Context context) {//Ethernet static 模式的 dns2
        Method method = getEthernetMethod(getEthernetDevInfoClass(), "getDns2Addr");
        String dnsAddress = "";
        if (method != null && getEthernetServiceObject(context) != null) {
            try {
                Object object = method.invoke(getSavedConfig());
                dnsAddress = object != null ? (String) object : "";
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return dnsAddress;
    }

    public static String getIpAddressFromInternet(Context context) {// wifi 模式的 ip
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return intToInetAddress(ipAddress).toString().replace("/", "");
        }
        return "";
    }

    public static String getNetMaskFromInternet(Context context) {// wifi 模式的 mask
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.getDhcpInfo() != null
                ? intToInetAddress(wifiManager.getDhcpInfo().netmask).toString().replace("/", "")
                : "null";
    }

    public static String getGatewayFromInternet(Context context) {// wifi 模式的 gateway
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.getDhcpInfo() != null
                ? intToInetAddress(wifiManager.getDhcpInfo().gateway).toString().replace("/", "")
                : "null";
    }

    public static String getDns1FromInternet(Context context) {// wifi 模式的 dns1
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.getDhcpInfo() != null
                ? intToInetAddress(wifiManager.getDhcpInfo().dns1).toString().replace("/", "")
                : "null";
    }

    public static String getDns2FromInternet(Context context) {// wifi 模式的 dns2
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null && wifiManager.getDhcpInfo() != null
                ? intToInetAddress(wifiManager.getDhcpInfo().dns2).toString().replace("/", "")
                : "null";
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }
}
