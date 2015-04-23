package ru.bitprofi.intercom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dd.CircularProgressButton;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    private CircularProgressButton _btnGo; //Кнопка на все случаи жизни
    private BluetoothHelper _bluetooth;

    public PlaceholderFragment() {
        _bluetooth = new BluetoothHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        prepeareForWork(rootView);
        return rootView;
    }

    /**
     * Подготовка к работе, настройка gui
     */
    private void prepeareForWork(View v) {
        _btnGo = (CircularProgressButton) v.findViewById(R.id.btnGo);

        //Обработка нажатий

        View.OnClickListener onClickBtns = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnGo:
                        break;
                }
            }
        };

        _btnGo.setOnClickListener(onClickBtns);
    }
}