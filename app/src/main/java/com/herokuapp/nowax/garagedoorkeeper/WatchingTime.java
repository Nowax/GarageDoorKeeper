package com.herokuapp.nowax.garagedoorkeeper;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class WatchingTime extends Activity {

    ImageButton button;
    Timer timer;
    MyTimerTask myTimerTask;
    private int brightness;
    RemoteControlReceiver receiver;

    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        registerReceiver(receiver, filter);
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watching_time);
        button = (ImageButton) findViewById(R.id.imageButton);
        receiver = new RemoteControlReceiver();
        receiver.setMainActivity(this);
        initiateMainButtonBehaviour();
        initiateBrightnessChangeMode();
    }

    private void initiateBrightnessChangeMode() {
        try{
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            brightness = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        }
        catch(Settings.SettingNotFoundException e){
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }
    }

    private void initiateMainButtonBehaviour() {
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handleGarageClosing();
            }
        });

        button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                handleGarageOpening();
                return false;
            }
        });
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

    private void remeberKeyEventDuringCall() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };
        AtomicReference<Thread> myThread = new AtomicReference<>(new Thread(runnable));
        myThread.get().start();
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


