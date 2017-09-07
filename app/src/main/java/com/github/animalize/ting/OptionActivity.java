package com.github.animalize.ting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.animalize.ting.Data.TingConfig;
import com.github.animalize.ting.TTS.Setting;
import com.github.animalize.ting.TTS.TingSetting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener {

    private TingConfig mConfig = TingConfig.getInstance();
    private TingSetting mTTSSetting = TingSetting.getInstance();

    private EditText mFilters;

    private TextView volumeText, speedText, pitchText;
    private SeekBar volumeSeekbar, speedSeekbar, pitchSeekbar;

    private int mNowSpeaker, mNowMixMode;

    public static void actionStart(Activity activity, int requestCode) {
        Intent i = new Intent(activity, OptionActivity.class);
        activity.startActivityForResult(i, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        // 普通参数
        volumeSeekbar = (SeekBar) findViewById(R.id.volume_seekbar);
        volumeSeekbar.setProgress(mTTSSetting.getmVolume());
        volumeSeekbar.setOnSeekBarChangeListener(this);

        speedSeekbar = (SeekBar) findViewById(R.id.speed_seekbar);
        speedSeekbar.setProgress(mTTSSetting.getmSpeed());
        speedSeekbar.setOnSeekBarChangeListener(this);

        pitchSeekbar = (SeekBar) findViewById(R.id.pitch_seekbar);
        pitchSeekbar.setProgress(mTTSSetting.getmPitch());
        pitchSeekbar.setOnSeekBarChangeListener(this);

        volumeText = (TextView) findViewById(R.id.volume_text);
        volumeText.setText("音量 " + mTTSSetting.getmVolume() + "：");
        speedText = (TextView) findViewById(R.id.speed_text);
        speedText.setText("语速 " + mTTSSetting.getmSpeed() + "：");
        pitchText = (TextView) findViewById(R.id.pitch_text);
        pitchText.setText("语调 " + mTTSSetting.getmPitch() + "：");

        // 朗读者
        Spinner speakerSpinner = (Spinner) findViewById(R.id.speaker_spinner);
        speakerSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Setting.getSpeakerNameList());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speakerSpinner.setAdapter(arrayAdapter);
        mNowSpeaker = mTTSSetting.getmSpeaker();
        speakerSpinner.setSelection(mNowSpeaker);

        // 模式
        Spinner mixModeSpinner = (Spinner) findViewById(R.id.mixmode_spinner);
        mixModeSpinner.setOnItemSelectedListener(this);

        arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Setting.getMixModeNameList());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mixModeSpinner.setAdapter(arrayAdapter);
        mNowMixMode = mTTSSetting.getmMixMode();
        mixModeSpinner.setSelection(mNowMixMode);

        // 过滤器
        Set<String> filters = mConfig.getmFilters();
        String s = "";
        for (String temp : filters) {
            s += temp + " ";
        }
        mFilters = (EditText) findViewById(R.id.filters);
        mFilters.setText(s);

        // 按钮
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

                mTTSSetting.setmVolume(volumeSeekbar.getProgress());
                mTTSSetting.setmSpeed(speedSeekbar.getProgress());
                mTTSSetting.setmPitch(pitchSeekbar.getProgress());
                mTTSSetting.setmSpeaker(mNowSpeaker);
                mTTSSetting.setmMixMode(mNowMixMode);

                // 保存到磁盘
                mTTSSetting.saveSetting();

                setResult(RESULT_OK, null);
                finish();
                break;

            case R.id.cancel:
                setResult(RESULT_CANCELED, null);
                finish();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.volume_seekbar:
                volumeText.setText("音量 " + progress + "：");
                break;

            case R.id.speed_seekbar:
                speedText.setText("语速 " + progress + "：");
                break;

            case R.id.pitch_seekbar:
                pitchText.setText("语调 " + progress + "：");
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.speaker_spinner:
                mNowSpeaker = position;
                break;

            case R.id.mixmode_spinner:
                mNowMixMode = position;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
