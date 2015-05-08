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
    public static volatile Context  context  = null;
    public static volatile Context  contextFragment  = null;
    public static volatile Activity activity = null;

    public static volatile BluetoothDevice serverDevice = null; //Обнаруженное устройство сервера

    public static final int BUTTON_IS_ON = 1;
    public static final int BUTTON_IS_OFF = 0;
    public static volatile int buttonState = BUTTON_IS_OFF;

    public static volatile int oldAudioMode = AudioManager.MODE_NORMAL;
    public static volatile int oldRingerMode = AudioManager.RINGER_MODE_NORMAL;
    public static volatile boolean isSpeakerPhoneOn = false;

    public static volatile String currentDeviceUUID    = null;
    public static volatile String currentDeviceName    = null;  //Текущее имя Bluetooth устройства
    public static volatile String currentDeviceAddress = null;  //Текущий адрес Bluetooth устройства

    public static volatile String oldDeviceName = null;  //Старое имя Bluetooth устройства

    public static volatile String connectDeviceName  = null;  //Сопрягаемое имя устройства
    public static volatile String connectDeviceUUID  = null;  //Сопрягаемое UUID устройства
    public static volatile String connectDeviceAddrs = null;  //Сопрягаемый адрес устройства

    public static volatile boolean isServer = false;
    public static volatile boolean isServiceThread = false;
    public static volatile Boolean isBluetoothDiscoveryFinished = false; //Поиск устройств закончен

    public static volatile int MIN_BUFFER_SIZE = 0;
    public static final int BYTES_PER_ELEMENT = 2;   //Байт в элементе

    public static final String PREFIX_DEVICE_NAME = "INTERCOM_"; //Префикс для Bluetooth устройств
    public static final int AUDIO_SAMPLERATE = 44100; //Дискретизация звука
}
