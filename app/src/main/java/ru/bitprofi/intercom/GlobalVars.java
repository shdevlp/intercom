package ru.bitprofi.intercom;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;

/**
 * Created by Дмитрий on 22.04.2015.
 */
public class GlobalVars {
    public static Context  context  = null;
    public static Activity activity = null;

    public static final int IS_ON = 1;
    public static final int IS_OFF = 0;
    public static int currentProgramState = 0;

    public static String currentDeviceName  = null;  //Текущее имя Bluetooth устройства
    public static String oldDeviceName      = null;  //Старое имя Bluetooth устройства
    public static String connectDeviceName  = null;  //Сопрягаемое имя устройства
    public static String connectDeviceAddrs = null;  //Сопрягаемый адрес устройства
    public static String currentAddress     = null;  //Текущий адрес Bluetooth устройства

    public static boolean isServer = false;
    public static int MIN_BUFFER_SIZE = 0;

    public static final int MIC_MSG_DATA = 0;        //Для опознования получаемых данных для микрофона
    public static final int SPEAKER_MSG_DATA = 1;    //Для опознования получаемых данных для динамика
    public static final int PLAYER_MSG_DATA = 2;     //Заглушка

    public static final int CLIENT_MSG_DATA = 2;     //Для опознования получаемых данных от клиента
    public static final int SERVER_MSG_DATA = 3;     //Для опознования получаемых данных от сервера

    public static final int BUFFER_COUNT = 32;       //Размер очереди буфера
    public static final int BUFFER_ELEMENTS = 10000; //Максимальный размер буфера получаемых данных с микрофона
    public static final int BYTES_PER_ELEMENT = 2;   //Байт в элементе

    public static final String PREFIX_DEVICE_NAME = "INTERCOM_"; //Префикс для Bluetooth устройств
    public static final int AUDIO_SAMPLERATE = 44100; //Дискретизация звука
}
