package com.readboy.mytreeview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.gson.Gson;
import com.readboy.mytreeview.utils.AssertsUtil;
import com.readboy.mytreeview.utils.log.LogUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       text();
    }

    private void text(){
        Gson gson = new Gson();
        String json = AssertsUtil.getFromAssets(MainActivity.this,"json.txt");
        LogUtils.i("js调用了安卓的方法" + json);

    }
}