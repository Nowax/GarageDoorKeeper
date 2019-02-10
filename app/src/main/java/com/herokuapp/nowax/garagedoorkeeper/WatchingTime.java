package com.herokuapp.nowax.garagedoorkeeper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import java.util.Timer;
import java.util.TimerTask;

public class WatchingTime extends Activity {
    private static final String EXTRA_START_DELAY = "com.herokuapp.nowax.garagedoorkeeper.delay";
    private static final String EXTRA_START_PHONE_NUMBER = "com.herokuapp.nowax.garagedoorkeeper.phone_number";
    private static final String EXTRA_START_CALLS_NUMBER = "com.herokuapp.nowax.garagedoorkeeper.calls_number";
    private Timer timer;
    private MyTimerTask myTimerTask;
    private Boolean has_been_already_started = false;
    private Integer noOfCalls = 0;

    @Override
    public void onResume() {
        if (has_been_already_started) {
            Intent intent = getIntent();
            Context context = getApplicationContext();
            Integer calls = noOfCalls + 1;
            String number = ((EditText)findViewById(R.id.editTextCallNumber)).getText().toString();
            String delay = ((EditText)findViewById(R.id.editTextDelay)).getText().toString();
            finish();
            restart(context, intent, calls, number, delay);
        }
        has_been_already_started=true;
        super.onResume();
    }

    public static void restart(Context context, Intent intent, int noOfCalls, String phoneNumber, String delay) {
        intent.putExtra(EXTRA_START_CALLS_NUMBER, noOfCalls);
        intent.putExtra(EXTRA_START_PHONE_NUMBER, phoneNumber);
        intent.putExtra(EXTRA_START_DELAY, delay);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_watching_time);
        initiateBrightnessChangeMode();
        setUpInitialPrimValues(intent);
        setUpImageButton();
    }

    private void setUpInitialPrimValues(Intent intent) {
        noOfCalls = intent.getIntExtra(EXTRA_START_CALLS_NUMBER, noOfCalls);

        TextView v1 = (TextView) findViewById(R.id.noOfCallsView);
        v1.setText(noOfCalls.toString());

        String number = intent.getStringExtra(EXTRA_START_PHONE_NUMBER);
        if (number != null) {
            TextView v2 = (EditText) findViewById(R.id.editTextCallNumber);
            v2.setText(number.toString());
        }

        String delay = intent.getStringExtra(EXTRA_START_DELAY);
        if (delay != null) {
            TextView v3 = (EditText) findViewById(R.id.editTextDelay);
            v3.setText(delay.toString());
        }
    }

    private void setUpImageButton() {
        ImageButton im_button = (ImageButton) findViewById(R.id.imageButton);
        im_button.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    handleGarageOpening();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    handleGarageClosing();
                }

                return true;
            }
        });
    }

    private void initiateBrightnessChangeMode() {
        Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    handleGarageOpening();
                    return true;
                case KeyEvent.ACTION_UP:
                    handleGarageClosing();
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public void handleGarageClosing() {
        ImageButton im_button = (ImageButton) findViewById(R.id.imageButton);
        im_button.setBackgroundResource(R.drawable.garage_closed);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void handleGarageOpening() {
        ImageButton im_button = (ImageButton) findViewById(R.id.imageButton);
        im_button.setBackgroundResource(R.drawable.garage_opened);
        if (timer == null) {
            timer = new Timer();
            myTimerTask = new MyTimerTask(this);
            EditText delayInSec = (EditText)findViewById(R.id.editTextDelay);
            timer.schedule(myTimerTask, Integer.valueOf(delayInSec.getText().toString())*1000);
        }
    }

    public void onClickedCallRequest(View v) {
        callToSpecificNumber();
    }

    public void onTurnOffScreen(View v) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 0;// 100 / 100.0f;
        getWindow().setAttributes(lp);
    }

    public void callToSpecificNumber() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        String phoneNumber = "tel:" + ((EditText)findViewById(R.id.editTextCallNumber)).getText().toString();
        callIntent.setData(Uri.parse(phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        }
    }
}

class MyTimerTask extends TimerTask {

    private WatchingTime watchingTimeHandle;

    public MyTimerTask(WatchingTime w) {
        watchingTimeHandle = w;
    }

    @Override
    public void run() {
        watchingTimeHandle.callToSpecificNumber();
    }

}


