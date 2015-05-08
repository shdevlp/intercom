package ru.bitprofi.intercom;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.HashMap;

import com.dd.CircularProgressButton;

import java.util.List;
import java.util.Map;
import java.util.Set;
import android.os.*;
import java.util.logging.LogRecord;

import at.markushi.ui.CircleButton;

public class PlaceholderFragment extends Fragment {
    private CircleButton _btnGo;       //Кнопка на все случаи жизни
    private ProgressBar _progressBar;  //Показывает длительность процесса
    private Intent _mainService; //Фоновая служба

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        prepeareForWork(rootView);

        GlobalVars.contextFragment = PlaceholderFragment.this.getActivity();
        _mainService = new Intent(GlobalVars.contextFragment, BackgroundService.class);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Подготовка к работе, настройка gui
     */
    private void prepeareForWork(View v) {
        _btnGo = (CircleButton) v.findViewById(R.id.btnGo);
        _progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        _progressBar.setVisibility(View.INVISIBLE);

        //Обработка нажатий
        View.OnClickListener onClickBtns = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnGo:
                        btnGoClicked();
                        break;
                }
            }
        };

        _btnGo.setOnClickListener(onClickBtns);
    }

    /**
     * Изменить цвет кнопки в зависимости от состояния Bluetooth
     */
    private void changeBtnColor(final boolean enable) {
        if (enable) {
            _btnGo.setColor(getResources().getColor(R.color.crimson));
        } else {
            _btnGo.setColor(getResources().getColor(R.color.seagreen));
        }
    }

    /**
     * Обработка нажатия
     */
    private void btnGoClicked() {
        //Программа уже работает - надо выключить
        if (GlobalVars.currentProgramState == GlobalVars.IS_ON) {
            changeBtnColor(false);
            GlobalVars.contextFragment.stopService(_mainService);
            return;
        }

        //Включаем работу
        if (GlobalVars.currentProgramState == GlobalVars.IS_OFF) {
            changeBtnColor(true);
            GlobalVars.contextFragment.startService(_mainService);
            return;
        }
   }
}