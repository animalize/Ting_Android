package com.github.animalize.ting;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.animalize.ting.Data.TingConfig;
import com.github.animalize.ting.TTS.Setting;
import com.github.animalize.ting.TTS.TTSService;
import com.github.animalize.ting.TingTTS.TingSetting;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptionActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener {

    private TingConfig mConfig = TingConfig.getInstance();
    private TingSetting mTTSSetting;

    private EditText mFilters, mHost;

    private TextView volumeText, speedText, pitchText;
    private SeekBar volumeSeekbar, speedSeekbar, pitchSeekbar;
    private int mNowSpeaker, mNowMixMode;

    private TextView thresholdText, windowText, fenjuText;
    private SeekBar thresholdSeekbar, windowSeekbar, fenjuSeekbar;
    private int mNowThreshold, mNowWindow, mNowFenju;

    private TextView pageCharsText;
    private TextView verInfoText, newVerText;

    private Button checkUpdateButton;

    public static void actionStart(Activity activity, int requestCode) {
        Intent i = new Intent(activity, OptionActivity.class);
        activity.startActivityForResult(i, requestCode);
    }

    public static Spanned getFromHtml(String html) {
        Spanned s;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            s = Html.fromHtml(html,
                    Html.FROM_HTML_MODE_LEGACY);
        } else {
            s = Html.fromHtml(html);
        }
        return s;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        mTTSSetting = TingSetting.getInstance(this);

        // 普通参数
        volumeSeekbar = findViewById(R.id.volume_seekbar);
        volumeSeekbar.setProgress(mTTSSetting.getmVolume());
        volumeSeekbar.setOnSeekBarChangeListener(this);

        speedSeekbar = findViewById(R.id.speed_seekbar);
        speedSeekbar.setProgress(mTTSSetting.getmSpeed());
        speedSeekbar.setOnSeekBarChangeListener(this);

        pitchSeekbar = findViewById(R.id.pitch_seekbar);
        pitchSeekbar.setProgress(mTTSSetting.getmPitch());
        pitchSeekbar.setOnSeekBarChangeListener(this);

        volumeText = findViewById(R.id.volume_text);
        volumeText.setText("" + mTTSSetting.getmVolume());
        speedText = findViewById(R.id.speed_text);
        speedText.setText("" + mTTSSetting.getmSpeed());
        pitchText = findViewById(R.id.pitch_text);
        pitchText.setText("" + mTTSSetting.getmPitch());

        // 朗读者
        Spinner speakerSpinner = findViewById(R.id.speaker_spinner);
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
        Spinner mixModeSpinner = findViewById(R.id.mixmode_spinner);
        mixModeSpinner.setOnItemSelectedListener(this);

        arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Setting.getMixModeNameList());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mixModeSpinner.setAdapter(arrayAdapter);
        mNowMixMode = mTTSSetting.getmMixMode();
        mixModeSpinner.setSelection(mNowMixMode);

        // 服务设置

        // 阈值
        thresholdSeekbar = findViewById(R.id.threshold_seekbar);
        thresholdSeekbar.setMax(TingSetting.THRESHOLD_MAX - Setting.THRESHOLD_MIN);
        mNowThreshold = mTTSSetting.getmThreshold();
        thresholdSeekbar.setProgress(mNowThreshold - TingSetting.THRESHOLD_MIN);
        thresholdSeekbar.setOnSeekBarChangeListener(this);

        thresholdText = findViewById(R.id.threshold_text);
        thresholdText.setText("阈值 " + mTTSSetting.getmThreshold() + "：");

        // 窗口长度
        windowSeekbar = findViewById(R.id.window_seekbar);
        windowSeekbar.setMax(TingSetting.WINDOW_MAX - Setting.WINDOW_MIN);
        mNowWindow = mTTSSetting.getmWindow();
        windowSeekbar.setProgress(mNowWindow - TingSetting.WINDOW_MIN);
        windowSeekbar.setOnSeekBarChangeListener(this);

        windowText = findViewById(R.id.window_text);
        windowText.setText("窗口长度 " + mTTSSetting.getmWindow() + "：");

        // 分句字符数
        fenjuSeekbar = findViewById(R.id.fenju_seekbar);
        fenjuSeekbar.setMax(TingSetting.FENJU_MAX - Setting.FENJU_MIN);
        mNowFenju = mTTSSetting.getmFenJu();
        fenjuSeekbar.setProgress(mNowFenju - TingSetting.FENJU_MIN);
        fenjuSeekbar.setOnSeekBarChangeListener(this);

        fenjuText = findViewById(R.id.fenju_text);
        fenjuText.setText("分句字符数 " + mTTSSetting.getmFenJu() + "：");

        // 每页字符数
        pageCharsText = findViewById(R.id.page_chars_textview);
        pageCharsText.setText(
                "分页字符数：" + TTSService.PAGE_SIZE + "，约合" +
                        (int) (0.8566 * TTSService.PAGE_SIZE) +
                        "汉字，此数值为编译时固定。"
        );

        // 过滤器
        Set<String> filters = mConfig.getmFilters();
        String s = "";
        for (String temp : filters) {
            s += temp + " ";
        }
        mFilters = findViewById(R.id.filters);
        mFilters.setText(s);

        // 服务器地址
        mHost = findViewById(R.id.host_addr);
        mHost.setText(mConfig.getHost());

        // 版本
        verInfoText = findViewById(R.id.ver_info);
        String versionName = "程序版本：" + BuildConfig.VERSION_NAME + "\n";
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        DateFormat df = new SimpleDateFormat("编译于：yyyy-MM-dd E HH:mm", Locale.getDefault());
        verInfoText.setText(versionName + df.format(buildDate));

        TextView tv = findViewById(R.id.html_ver);
        Spanned span = getFromHtml(getString(R.string.about));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(span);

        newVerText = findViewById(R.id.new_ver);

        checkUpdateButton = findViewById(R.id.check_update);
        checkUpdateButton.setOnClickListener(this);

        // 按钮
        Button bt = findViewById(R.id.ok);
        bt.setOnClickListener(this);
        bt = findViewById(R.id.cancel);
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

                mConfig.setHost(mHost.getText().toString());

                mTTSSetting.setmVolume(volumeSeekbar.getProgress());
                mTTSSetting.setmSpeed(speedSeekbar.getProgress());
                mTTSSetting.setmPitch(pitchSeekbar.getProgress());
                mTTSSetting.setmSpeaker(mNowSpeaker);
                mTTSSetting.setmMixMode(mNowMixMode);

                mTTSSetting.setmThreshold(mNowThreshold);
                mTTSSetting.setmWindow(mNowWindow);
                mTTSSetting.setmFenJu(mNowFenju);

                // 保存到磁盘
                mTTSSetting.saveSetting(this);

                setResult(RESULT_OK, null);
                finish();
                break;

            case R.id.cancel:
                setResult(RESULT_CANCELED, null);
                finish();
                break;

            case R.id.check_update:
                checkUpdateButton.setEnabled(false);
                new CheckTask(this).execute();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.volume_seekbar:
                volumeText.setText("" + progress);
                break;

            case R.id.speed_seekbar:
                speedText.setText("" + progress);
                break;

            case R.id.pitch_seekbar:
                pitchText.setText("" + progress);
                break;

            case R.id.threshold_seekbar:
                mNowThreshold = progress + TingSetting.THRESHOLD_MIN;
                thresholdText.setText("阈值 " + mNowThreshold + "：");
                break;

            case R.id.window_seekbar:
                mNowWindow = progress + TingSetting.WINDOW_MIN;
                windowText.setText("窗口长度 " + mNowWindow + "：");
                break;

            case R.id.fenju_seekbar:
                progress = progress / 4;
                progress = progress * 4;

                mNowFenju = progress + TingSetting.FENJU_MIN;
                fenjuText.setText("分句字符数 " + mNowFenju + "：");
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

    private void updateUI(String s) {
        checkUpdateButton.setEnabled(true);

        newVerText.setText(s);
        newVerText.setVisibility(View.VISIBLE);
    }

    private static class CheckTask extends AsyncTask<Void, Void, String> {
        private static final String verURL =
                "https://raw.githubusercontent.com/animalize/Ting_Android/master/app/build.gradle";
        private WeakReference<OptionActivity> ref;

        public CheckTask(OptionActivity about) {
            ref = new WeakReference<>(about);
        }

        @Override
        protected String doInBackground(Void... params) {
            String html;

            try {
                URL url = new URL(verURL);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(10 * 1000);
                con.setReadTimeout(10 * 1000);
                InputStream in = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                html = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }

                reader.close();
            } catch (Exception e) {
                return null;
            }

            String p = "versionName\\s*\"(.*?)\"";

            Pattern pattern = Pattern.compile(p, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(html);
            if (!matcher.find()) {
                return null;
            }

            return "GitHub上最新版本：" + matcher.group(1);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            OptionActivity about = ref.get();
            if (about == null) {
                return;
            }

            if (s == null) {
                s = "获取信息失败";
            }
            about.updateUI(s);
        }
    }
}
