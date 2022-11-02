package com.myx.circlemenu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private CircleMenu main_cm;
    private TextView main_tv_select, main_tv_click;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView(){
        main_tv_select = findViewById(R.id.main_tv_select);
        main_tv_click = findViewById(R.id.main_tv_click);

        main_cm = findViewById(R.id.main_cm);
        main_cm.setDivCount(4);
        main_cm.setCircleImage(R.mipmap.menu_0);
        main_cm.setCircleChangeListener(new CircleMenu.CircleChangeListener() {
            @Override
            public void onSelectionChange(int selectedPosition) {
                main_tv_select.setText("select="+selectedPosition);
                switch (selectedPosition){
                    case 0:
                        main_cm.modifyCircleImage(R.mipmap.menu_0);
                        break;
                    case 1:
                        main_cm.modifyCircleImage(R.mipmap.menu_1);
                        break;
                    case 2:
                        main_cm.modifyCircleImage(R.mipmap.menu_2);
                        break;
                    case 3:
                        main_cm.modifyCircleImage(R.mipmap.menu_3);
                        break;
                }
            }

            @Override
            public void onClickedChange(int clickedPosition) {
                main_tv_click.setText("click="+clickedPosition);
                switch (clickedPosition){
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                }
            }
        });
    }
}