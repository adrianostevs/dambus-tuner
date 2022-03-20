package com.example.datun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.datun.audio.CaptureThread;
import com.example.datun.audio.CaptureTask;
import com.example.datun.graphics.DialView;
import com.example.datun.graphics.DialSurfaceView;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private TextView t, nilaiKunci;
    private DialView dial;
    //private CaptureTask capture;
    private float targetFrequency;
    private com.example.datun.audio.CaptureThread mCapture;
    private Handler mHandler;
    private void checkRecordPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 123);
        }
    }
    Button d,g,c,f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dial = (DialView) findViewById(R.id.dial);
        t = (TextView) findViewById(R.id.hasil);
        nilaiKunci = (TextView) findViewById(R.id.kunci);
        checkRecordPermission();
        updateTargetFrequency(); // Get radio button selection

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message m) {
                updateDisplay(m.getData().getFloat("Freq"));
            }
        };
        mCapture = new CaptureThread(mHandler);
                mCapture.setRunning(true);
                mCapture.start();


        d = (Button) findViewById(R.id.dbunyi);
        final MediaPlayer dp = MediaPlayer.create(this, R.raw.d);
        d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dp.start();
            }
        });

        g = (Button) findViewById(R.id.gbunyi);
        final MediaPlayer gp = MediaPlayer.create(this, R.raw.g);
        g.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gp.start();
            }
        });

        c = (Button) findViewById(R.id.cbunyi);
        final MediaPlayer cp = MediaPlayer.create(this, R.raw.c);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cp.start();
            }
        });

        f = (Button) findViewById(R.id.fbunyi);
        final MediaPlayer fp = MediaPlayer.create(this, R.raw.f);
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fp.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCapture != null) {
            mCapture.setRunning(false);
            mCapture = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCapture.setRunning(false);
    }

    @Override
    protected void onResume(){
        super.onResume();

        updateTargetFrequency(); // Get radio button selection

        //Log.d("PTuneActivity", "onResume called.");
    }


    public void onOptionsItemSelected() {
        RadioButton rb;
        // Handle item selection
                rb = (RadioButton) findViewById(R.id.radio1);
                rb.setText("D3");
                rb.setTag("146.83");
                rb = (RadioButton) findViewById(R.id.radio2);
                rb.setText("G3");
                rb.setTag("195.99");
                rb = (RadioButton) findViewById(R.id.radio3);
                rb.setText("C4");
                rb.setTag("261.62");
                rb = (RadioButton) findViewById(R.id.radio4);
                rb.setText("F4");
                rb.setTag("349.22");
                updateTargetFrequency();
    }

    private void updateTargetFrequency() {
        // Grab the selected radio button tag.
        RadioGroup rg = (RadioGroup) findViewById(R.id.rg);
        int selected = rg.getCheckedRadioButtonId();
        RadioButton rb = (RadioButton) findViewById(selected);
        targetFrequency = Float.parseFloat((String)rb.getTag());
    }

    public void updateDisplay(float frequency) {
        // Calculate difference between target and measured relativeFrequency,
        // given that the measured relativeFrequency can be a factor of target.


        float difference = 0;
        //if (frequency > targetFrequency) {
        //int divisions = (int) (frequency / targetFrequency);
        //float modified = targetFrequency * (float) divisions;
        //if (frequency - modified > targetFrequency / 2) {
        //    modified += targetFrequency;
        //    divisions++;
        //}
        //difference = (frequency - modified) / (float) divisions;
        //} else {
        //    // If target is greater than measured, just use difference.
        //    difference = targetFrequency - frequency;
        //}

        float relativeFrequency = frequency;
        System.out.printf("Frekuenci", relativeFrequency);
        int frek = (int) relativeFrequency;
        Log.e("Frek", String.valueOf(frek));

        difference = relativeFrequency - targetFrequency;
        System.out.printf("Selisih", difference);
        int dif = (int) difference;
        Log.i("Beda", String.valueOf(dif));


                // Update TextView
                if (relativeFrequency < 1000f) {
                    t.setText(String.format("%.1f Hz", relativeFrequency));
                } else {
                    t.setText(String.format("%.2f kHz", relativeFrequency / 1000));
                }

        // Update DialView
        float value = difference / (targetFrequency / 2) * 90;
                dial.update(value);

        //set kunci
                if (relativeFrequency <= 16.85 && relativeFrequency >= 15.85) {
                    nilaiKunci.setText(R.string.c0);
                } else if (relativeFrequency <= 17.85 && relativeFrequency >= 16.85) {
                    nilaiKunci.setText("C#0");
                } else if (relativeFrequency <= 18.85 && relativeFrequency >= 17.85) {
                    nilaiKunci.setText(R.string.d0);
                } else if (relativeFrequency <= 20.04 && relativeFrequency >= 18.85) {
                    nilaiKunci.setText("D#0");
                } else if (relativeFrequency <= 21.23 && relativeFrequency >= 20.04) {
                    nilaiKunci.setText(R.string.e0);
                } else if (relativeFrequency <= 22.48 && relativeFrequency >= 21.23) {
                    nilaiKunci.setText("F0");
                } else if (relativeFrequency <= 24.05 && relativeFrequency >= 22.48) {
                    nilaiKunci.setText("F#0");
                } else if (relativeFrequency <= 25.45 && relativeFrequency >= 24.05) {
                    nilaiKunci.setText("G0");
                } else if (relativeFrequency <= 26.75 && relativeFrequency >= 25.45) {
                    nilaiKunci.setText("G#0");
                } else if (relativeFrequency <= 28.30 && relativeFrequency >= 26.75) {
                    nilaiKunci.setText("A0");
                } else if (relativeFrequency <= 29.98 && relativeFrequency >= 28.30) {
                    nilaiKunci.setText("A#0");
                } else if (relativeFrequency <= 31.78 && relativeFrequency >= 29.98) {
                    nilaiKunci.setText("B0");
                } else if (relativeFrequency <= 33.68 && relativeFrequency >= 31.78) {
                    nilaiKunci.setText("C1");
                } else if (relativeFrequency <= 35.67 && relativeFrequency >= 33.68) {
                    nilaiKunci.setText("C#1");
                } else if (relativeFrequency <= 37.79 && relativeFrequency >= 35.67) {
                    nilaiKunci.setText("D1");
                } else if (relativeFrequency <= 40.04 && relativeFrequency >= 37.79) {
                    nilaiKunci.setText("D#1");
                } else if (relativeFrequency <= 42.42 && relativeFrequency >= 40.04) {
                    nilaiKunci.setText("E1");
                } else if (relativeFrequency <= 44.95 && relativeFrequency >= 42.42) {
                    nilaiKunci.setText("F1");
                } else if (relativeFrequency <= 47.60 && relativeFrequency >= 44.95) {
                    nilaiKunci.setText("F#1");
                } else if (relativeFrequency <= 49.95 && relativeFrequency >= 47.60) {
                    nilaiKunci.setText("G1");
                } else if (relativeFrequency <= 53.41 && relativeFrequency >= 49.95) {
                    nilaiKunci.setText("G#1");
                } else if (relativeFrequency <= 56.60 && relativeFrequency >= 53.41) {
                    nilaiKunci.setText("A1");
                } else if (relativeFrequency <= 60.07 && relativeFrequency >= 56.60) {
                    nilaiKunci.setText("A#1");
                } else if (relativeFrequency <= 63.58 && relativeFrequency >= 160.07) {
                    nilaiKunci.setText("B1");
                } else if (relativeFrequency <= 67.35 && relativeFrequency >= 63.58) {
                    nilaiKunci.setText("C2");
                } else if (relativeFrequency <= 71.35 && relativeFrequency >= 67.35) {
                    nilaiKunci.setText("C#2");
                } else if (relativeFrequency <= 75.59 && relativeFrequency >= 71.35) {
                    nilaiKunci.setText("D2");
                } else if (relativeFrequency <= 80.18 && relativeFrequency >= 75.59) {
                    nilaiKunci.setText("D#2");
                } else if (relativeFrequency <= 84.81 && relativeFrequency >= 80.18) {
                    nilaiKunci.setText("E2");
                } else if (relativeFrequency <= 89.90 && relativeFrequency >= 84.81) {
                    nilaiKunci.setText("F2");
                } else if (relativeFrequency <= 95.24 && relativeFrequency >= 89.90) {
                    nilaiKunci.setText("F#2");
                } else if (relativeFrequency <= 100.99 && relativeFrequency >= 95.24) {
                    nilaiKunci.setText("G2");
                } else if (relativeFrequency <= 106.82 && relativeFrequency >= 100.99) {
                    nilaiKunci.setText("G#2");
                } else if (relativeFrequency <= 113.27 && relativeFrequency >= 106.82) {
                    nilaiKunci.setText("A2");
                } else if (relativeFrequency <= 120.04 && relativeFrequency >= 113.27) {
                    nilaiKunci.setText("A#2");
                } else if (relativeFrequency <= 127.17 && relativeFrequency >= 120.04) {
                    nilaiKunci.setText("B2");
                } else if (relativeFrequency <= 134.81 && relativeFrequency >= 127.17) {
                    nilaiKunci.setText("C3");
                } else if (relativeFrequency <= 142.59 && relativeFrequency >= 134.81) {
                    nilaiKunci.setText("C#3");
                } else if (relativeFrequency <= 151.18 && relativeFrequency >= 142.59) {
                    nilaiKunci.setText("D3");
                } else if (relativeFrequency <= 160.06 && relativeFrequency >= 151.18) {
                    nilaiKunci.setText("D#3");
                } else if (relativeFrequency <= 169.71 && relativeFrequency >= 160.06) {
                    nilaiKunci.setText("E3");
                } else if (relativeFrequency <= 179.80 && relativeFrequency >= 169.71) {
                    nilaiKunci.setText("F3");
                } else if (relativeFrequency <= 190.49 && relativeFrequency >= 179.80) {
                    nilaiKunci.setText("F#3");
                } else if (relativeFrequency <= 201.30 && relativeFrequency >= 190.49) {
                    nilaiKunci.setText("G3");
                } else if (relativeFrequency <= 213.85 && relativeFrequency >= 201.30) {
                    nilaiKunci.setText("G#3");
                } else if (relativeFrequency <= 226.50 && relativeFrequency >= 213.85) {
                    nilaiKunci.setText("A3");
                } else if (relativeFrequency <= 239.99 && relativeFrequency >= 226.50) {
                    nilaiKunci.setText("A#3");
                } else if (relativeFrequency <= 254.44 && relativeFrequency >= 239.99) {
                    nilaiKunci.setText("B3");
                } else if (relativeFrequency <= 269.40 && relativeFrequency >= 254.44) {
                    nilaiKunci.setText("C4");
                } else if (relativeFrequency <= 285.42 && relativeFrequency >= 269.40) {
                    nilaiKunci.setText("C#4");
                } else if (relativeFrequency <= 302.66 && relativeFrequency >= 285.42) {
                    nilaiKunci.setText("D4");
                } else if (relativeFrequency <= 320.37 && relativeFrequency >= 302.66) {
                    nilaiKunci.setText("D#4");
                } else if (relativeFrequency <= 339.62 && relativeFrequency >= 320.37) {
                    nilaiKunci.setText("E4");
                } else if (relativeFrequency <= 359.55 && relativeFrequency >= 339.62) {
                    nilaiKunci.setText("F4");
                } else if (relativeFrequency <= 380.99 && relativeFrequency >= 359.55) {
                    nilaiKunci.setText("F#4");
                } else if (relativeFrequency <= 403.99 && relativeFrequency >= 380.99) {
                    nilaiKunci.setText("G4");
                } else if (relativeFrequency <= 427.80 && relativeFrequency >= 403.99) {
                    nilaiKunci.setText("G#4");
                } else if (relativeFrequency <= 453.00 && relativeFrequency >= 427.80) {
                    nilaiKunci.setText("A4");
                } else if (relativeFrequency <= 479.66 && relativeFrequency >= 453.00) {
                    nilaiKunci.setText("A#4");
                } else if (relativeFrequency <= 508.88 && relativeFrequency >= 479.66) {
                    nilaiKunci.setText("B4");
                } else if (relativeFrequency <= 538.25 && relativeFrequency >= 508.88) {
                    nilaiKunci.setText("C5");
                } else if (relativeFrequency <= 570.36 && relativeFrequency >= 538.25) {
                    nilaiKunci.setText("C#5");
                } else if (relativeFrequency <= 604.83 && relativeFrequency >= 570.36) {
                    nilaiKunci.setText("D5");
                } else if (relativeFrequency <= 640.75 && relativeFrequency >= 604.83) {
                    nilaiKunci.setText("D#5");
                } else if (relativeFrequency <= 679.25 && relativeFrequency >= 640.75) {
                    nilaiKunci.setText("E5");
                } else if (relativeFrequency <= 718.45 && relativeFrequency >= 679.25) {
                    nilaiKunci.setText("F5");
                } else if (relativeFrequency <= 761.98 && relativeFrequency >= 718.45) {
                    nilaiKunci.setText("F#5");
                } else if (relativeFrequency <= 807.49 && relativeFrequency >= 761.98) {
                    nilaiKunci.setText("G5");
                } else if (relativeFrequency <= 855.60 && relativeFrequency >= 807.49) {
                    nilaiKunci.setText("G#5");
                } else if (relativeFrequency <= 906.00 && relativeFrequency >= 855.60) {
                    nilaiKunci.setText("A5");
                } else if (relativeFrequency <= 959.82 && relativeFrequency >= 906.00) {
                    nilaiKunci.setText("A#5");
                } else if (relativeFrequency <= 1017.76 && relativeFrequency >= 959.82) {
                    nilaiKunci.setText("B5");
                } else if (relativeFrequency <= 1077.50 && relativeFrequency >= 1017.76) {
                    nilaiKunci.setText("C6");
                } else if (relativeFrequency <= 1141.65 && relativeFrequency >= 1077.50) {
                    nilaiKunci.setText("C#6");
                } else if (relativeFrequency <= 1209.55 && relativeFrequency >= 1141.65) {
                    nilaiKunci.setText("D6");
                } else if (relativeFrequency <= 1281.5 && relativeFrequency >= 1209.55) {
                    nilaiKunci.setText("D#6");
                } else if (relativeFrequency <= 1357.7 && relativeFrequency >= 1281.5) {
                    nilaiKunci.setText("E6");
                } else if (relativeFrequency <= 1438.4 && relativeFrequency >= 1357.7) {
                    nilaiKunci.setText("F6");
                } else if (relativeFrequency <= 1523.9 && relativeFrequency >= 1438.4) {
                    nilaiKunci.setText("F#6");
                } else if (relativeFrequency <= 1614.55 && relativeFrequency >= 1523.9) {
                    nilaiKunci.setText("G6");
                } else if (relativeFrequency <= 1710.6 && relativeFrequency >= 1614.55) {
                    nilaiKunci.setText("G#6");
                } else if (relativeFrequency <= 1812.3 && relativeFrequency >= 1710.6) {
                    nilaiKunci.setText("A6");
                } else if (relativeFrequency <= 1920.05 && relativeFrequency >= 1812.3) {
                    nilaiKunci.setText("A#6");
                } else if (relativeFrequency <= 2034.25 && relativeFrequency >= 1920.05) {
                    nilaiKunci.setText("B6");
                } else if (relativeFrequency <= 2155.2 && relativeFrequency >= 2034.25) {
                    nilaiKunci.setText("C7");
                } else if (relativeFrequency <= 2283.35 && relativeFrequency >= 2155.2) {
                    nilaiKunci.setText("C#7");
                } else if (relativeFrequency <= 2419.15 && relativeFrequency >= 2283.35) {
                    nilaiKunci.setText("D7");
                } else if (relativeFrequency <= 2563 && relativeFrequency >= 2419.15) {
                    nilaiKunci.setText("D#7");
                } else if (relativeFrequency <= 2715.4 && relativeFrequency >= 2563) {
                    nilaiKunci.setText("E7");
                } else if (relativeFrequency <= 2876.85 && relativeFrequency >= 2715.4) {
                    nilaiKunci.setText("F7");
                } else if (relativeFrequency <= 3047.9 && relativeFrequency >= 2876.85) {
                    nilaiKunci.setText("F#7");
                } else if (relativeFrequency <= 3229.15 && relativeFrequency >= 3047.9) {
                    nilaiKunci.setText("G7");
                } else if (relativeFrequency <= 3421.2 && relativeFrequency >= 3229.15) {
                    nilaiKunci.setText("G#7");
                } else if (relativeFrequency <= 3624.65 && relativeFrequency >= 3421.2) {
                    nilaiKunci.setText("A7");
                } else if (relativeFrequency <= 3840.5 && relativeFrequency >= 3624.65) {
                    nilaiKunci.setText("A#7");
                } else if (relativeFrequency <= 4068.5 && relativeFrequency >= 3840.5) {
                    nilaiKunci.setText("B7");
                } else if (relativeFrequency <= 4310.46 && relativeFrequency >= 4068.5) {
                    nilaiKunci.setText("C8");
                } else if (relativeFrequency <= 4566.46 && relativeFrequency >= 4310.46) {
                    nilaiKunci.setText("C#8");
                } else if (relativeFrequency <= 4837.74 && relativeFrequency >= 4566.46) {
                    nilaiKunci.setText("D8");
                } else if (relativeFrequency <= 5126 && relativeFrequency >= 4837.74) {
                    nilaiKunci.setText("D#8");
                } else if (relativeFrequency <= 5430.5 && relativeFrequency >= 5126) {
                    nilaiKunci.setText("E8");
                } else if (relativeFrequency <= 5753.45 && relativeFrequency >= 5430.5) {
                    nilaiKunci.setText("F8");
                } else if (relativeFrequency <= 6095.5 && relativeFrequency >= 5753.45) {
                    nilaiKunci.setText("F#8");
                } else if (relativeFrequency <= 6458.39 && relativeFrequency >= 6095.5) {
                    nilaiKunci.setText("G8");
                } else if (relativeFrequency <= 6842.44 && relativeFrequency >= 6458.39) {
                    nilaiKunci.setText("G#8");
                } else if (relativeFrequency <= 7249.3 && relativeFrequency >= 6842.44) {
                    nilaiKunci.setText("A8");
                } else if (relativeFrequency <= 7680.35 && relativeFrequency >= 7249.3) {
                    nilaiKunci.setText("A#8");
                } else if (relativeFrequency <= 8137 && relativeFrequency >= 7680.35) {
                    nilaiKunci.setText("B8");
                } else if (relativeFrequency <= 8620.5 && relativeFrequency >= 8137) {
                    nilaiKunci.setText("C9");
                } else if (relativeFrequency <= 9133.5 && relativeFrequency >= 8620.5) {
                    nilaiKunci.setText("C#9");
                } else if (relativeFrequency <= 9676.6 && relativeFrequency >= 9133.5) {
                    nilaiKunci.setText("D9");
                } else if (relativeFrequency <= 10252 && relativeFrequency >= 9676.6) {
                    nilaiKunci.setText("D#9");
                } else if (relativeFrequency <= 10861.65 && relativeFrequency >= 10252) {
                    nilaiKunci.setText("E9");
                } else if (relativeFrequency <= 11507.55 && relativeFrequency >= 10861.65) {
                    nilaiKunci.setText("F9");
                } else if (relativeFrequency <= 12191.8 && relativeFrequency >= 11507.55) {
                    nilaiKunci.setText("F#9");
                } else if (relativeFrequency <= 12916.75 && relativeFrequency >= 12191.8) {
                    nilaiKunci.setText("G9");
                } else if (relativeFrequency <= 13684.85 && relativeFrequency >= 12916.75) {
                    nilaiKunci.setText("G#9");
                } else if (relativeFrequency <= 14498.6 && relativeFrequency >= 13684.85) {
                    nilaiKunci.setText("A9");
                } else if (relativeFrequency <= 15360.7 && relativeFrequency >= 14498.6) {
                    nilaiKunci.setText("A#9");
                } else if (relativeFrequency <= 16274.1 && relativeFrequency >= 15360.7) {
                    nilaiKunci.setText("B9");
                } else if (relativeFrequency <= 17241.85 && relativeFrequency >= 16274.1) {
                    nilaiKunci.setText("C10");
                } else if (relativeFrequency <= 18267.1 && relativeFrequency >= 17241.85) {
                    nilaiKunci.setText("C#10");
                } else if (relativeFrequency <= 19353.3 && relativeFrequency >= 18267.1) {
                    nilaiKunci.setText("D10");
                } else if (relativeFrequency <= 20504.1 && relativeFrequency >= 19353.3) {
                    nilaiKunci.setText("D#10");
                } else if (relativeFrequency <= 21723.5 && relativeFrequency >= 20504.1) {
                    nilaiKunci.setText("E10");
                } else {
                    nilaiKunci.setText("-");
                }

        //warna text
        if (relativeFrequency <= (targetFrequency - (0.3 * targetFrequency))) {
            nilaiKunci.setTextColor(getResources().getColor(R.color.merah));
        } else if (relativeFrequency < (targetFrequency - (0.2 * targetFrequency)) && relativeFrequency > (targetFrequency - (0.3 * targetFrequency))) {
            nilaiKunci.setTextColor(getResources().getColor(R.color.oranye));
        } else if (relativeFrequency < (targetFrequency - (0.1 * targetFrequency)) && relativeFrequency > (targetFrequency - (0.2 * targetFrequency))) {
            nilaiKunci.setTextColor(getResources().getColor(R.color.kuning));
        } else if (relativeFrequency < (targetFrequency - (0.01 * targetFrequency)) && relativeFrequency > (targetFrequency - (0.1 * targetFrequency))) {
            nilaiKunci.setTextColor(getResources().getColor(R.color.tua));
        } else if (relativeFrequency < (targetFrequency + (0.01 * targetFrequency)) && relativeFrequency > (targetFrequency - (0.01 * targetFrequency))) {
            nilaiKunci.setTextColor(getResources().getColor(R.color.ijo));
        } else if (relativeFrequency < (targetFrequency + (0.1 * targetFrequency)) && relativeFrequency > (targetFrequency + (0.01 * targetFrequency))) {
            nilaiKunci.setTextColor(getResources().getColor(R.color.tua));
        } else if (relativeFrequency < (targetFrequency + (0.2 * targetFrequency)) && relativeFrequency > (targetFrequency + (0.1 * targetFrequency))) {
            nilaiKunci.setTextColor(getResources().getColor(R.color.kuning));
        } else if (relativeFrequency < (targetFrequency + (0.3 * targetFrequency)) && relativeFrequency > (targetFrequency + (0.2 * targetFrequency))) {
            nilaiKunci.setTextColor(getResources().getColor(R.color.oranye));
        } else if (relativeFrequency >= (targetFrequency + (0.3 * targetFrequency))) {
            nilaiKunci.setTextColor(getResources().getColor(R.color.merah));
        }
    }


    public void onRadioButtonClicked(View v) {
        // Perform action on clicks
        RadioButton rb = (RadioButton) v;
        Toast.makeText(MainActivity.this, rb.getText(), Toast.LENGTH_SHORT).show();
        targetFrequency = Float.parseFloat((String)rb.getTag());
    }
}
