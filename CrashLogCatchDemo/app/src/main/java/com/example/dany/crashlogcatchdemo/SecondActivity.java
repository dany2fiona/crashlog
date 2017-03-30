package com.example.dany.crashlogcatchdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sencond);
        int a = Integer.parseInt("10o");
        ((TextView)findViewById(R.id.tv)).setText(a);
    }
}
