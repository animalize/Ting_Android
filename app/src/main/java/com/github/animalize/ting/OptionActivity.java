package com.github.animalize.ting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.animalize.ting.Data.TingConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener {

    private TingConfig mConfig = TingConfig.getInstance();
    private EditText mFilters;

    public static void actionStart(Context context) {
        Intent i = new Intent(context, OptionActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        Set<String> filters = mConfig.getmFilters();
        String s = "";
        for (String temp : filters) {
            s += temp + " ";
        }
        mFilters = (EditText) findViewById(R.id.filters);
        mFilters.setText(s);

        Button bt = (Button) findViewById(R.id.ok);
        bt.setOnClickListener(this);
        bt = (Button) findViewById(R.id.cancel);
        bt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                String s = mFilters.getText().toString();
                String[] parts = s.split("\\s+");
                Set<String> set = new HashSet<>(Arrays.asList(parts));
                mConfig.setmFilters(set);

                finish();
                break;

            case R.id.cancel:
                finish();
                break;
        }
    }
}
