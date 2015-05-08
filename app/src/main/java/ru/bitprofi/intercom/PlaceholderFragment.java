package ru.bitprofi.intercom;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.markushi.ui.CircleButton;

public class PlaceholderFragment extends Fragment {
    private CircleButton _btnGo;   //Кнопка на все случаи жизни
    private Intent _mainService;   //Фоновая служба

    private ATask _at = null;

    private class ATask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                if (GlobalVars.isServiceThread) {
                    Utils.getInstance().setBtnColor(getResources().getColor(R.color.seagreen));
                    Utils.getInstance().setBtnEnabled(true);
                    if (GlobalVars.isServer) {
                        Utils.getInstance().addStatusText("Это устройство СЕРВЕР");
                    } else {
                        Utils.getInstance().addStatusText("Это устройство КЛИЕНТ");
                    }
                    break;
                }
            }
            return null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        prepeareForWork(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        GlobalVars.contextFragment = PlaceholderFragment.this.getActivity();
        _mainService = new Intent(GlobalVars.contextFragment, BackgroundService.class);

        _at = new ATask();
        _at.execute();

        GlobalVars.contextFragment.startService(_mainService);
    }

    /**
     * Подготовка к работе, настройка gui
     */
    private void prepeareForWork(View v) {
        _btnGo = (CircleButton) v.findViewById(R.id.btnGo);
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

        Utils.getInstance().setBtnColor(getResources().getColor(R.color.yellow));
        Utils.getInstance().setBtnEnabled(false);
    }

    /**
     * Обработка нажатия
     */
    private void btnGoClicked() {
        //Программа уже работает - надо выключить
        if (GlobalVars.buttonState == GlobalVars.BUTTON_IS_ON) {
            _btnGo.setColor(getResources().getColor(R.color.seagreen));
            //GlobalVars.contextFragment.stopService(_mainService);
            GlobalVars.buttonState = GlobalVars.BUTTON_IS_OFF;
            return;
        }

        //Включаем работу
        if (GlobalVars.buttonState == GlobalVars.BUTTON_IS_OFF) {
            _btnGo.setColor(getResources().getColor(R.color.crimson));
            GlobalVars.buttonState = GlobalVars.BUTTON_IS_ON;
            return;
        }
   }
}