package ru.bitprofi.intercom;

import android.app.Activity;
import android.content.Context;

/**
 * Created by Дмитрий on 22.04.2015.
 */
public class GlobalVars {
    public static Context  context  = null;
    public static Activity activity = null;
    public static final String prefixDeviceName = "INTERCOM_";
    public static String currentDeviceName = null; //Текущее имя Bluetooth устройства
    public static String oldDeviceName = null;     //Старое имя Bluetooth устройства
    public static String currentAddress = null;    //Текущий адрес Bluetooth устройства
    public static String connectDeviceName = null; //Сопрягаемое имя устройства
    public static String connectDeviceAddrs = null;//Сопрягаемый адрес устройства
}
