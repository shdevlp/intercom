package ru.bitprofi.intercom;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Дмитрий on 22.04.2015.
 */
public class GlobalVars {
    public static Context  context  = null;
    public static Context  contextFragment  = null;
    public static Activity activity = null;

    public static BluetoothDevice serverDevice = null; //Обнаруженное устройство сервера

    public static int oldAudioMode = AudioManager.MODE_NORMAL;
    public static int oldRingerMode = AudioManager.RINGER_MODE_NORMAL;
    public static boolean isSpeakerPhoneOn = false;

    public static String currentDeviceUUID    = null;
    public static String currentDeviceName    = null;  //Текущее имя Bluetooth устройства
    public static String currentDeviceAddress = null;  //Текущий адрес Bluetooth устройства

    public static String oldDeviceName = null;  //Старое имя Bluetooth устройства

    public static String connectDeviceName  = null;  //Сопрягаемое имя устройства
    public static String connectDeviceUUID  = null;  //Сопрягаемое UUID устройства
    public static String connectDeviceAddrs = null;  //Сопрягаемый адрес устройства

    public static boolean isServer = false;
    public static Boolean isBluetoothDiscoveryFinished = false; //Поиск устройств закончен

    public static int MIN_BUFFER_SIZE = 0;
    public static final int BYTES_PER_ELEMENT = 2;   //Байт в элементе
    public static final int BUFFER_COUNT = 32;

    public static final int MIC_MSG_DATA     = 0;
    public static final int SPEAKER_MSG_DATA = 1;

    public static final String PREFIX_DEVICE_NAME = "INTERCOM_"; //Префикс для Bluetooth устройств
    public static final int AUDIO_SAMPLERATE = 44100; //Дискретизация звука

    public static final int BUTTON_IS_ON = 1;
    public static final int BUTTON_IS_OFF = 0;
    public static int buttonState = BUTTON_IS_OFF;

    public static BluetoothAdapter bluetoothAdapter = null;
}
