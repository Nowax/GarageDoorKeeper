package com.herokuapp.nowax.garagedoorkeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class RemoteControlReceiver extends BroadcastReceiver {
    private WatchingTime watchingTime;

    public void setMainActivity(WatchingTime w) {
        watchingTime = w;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("calling me "," !!!");
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_HEADSETHOOK == event.getKeyCode()) {
                if (watchingTime != null) {
                    TextView view = (TextView) watchingTime.findViewById(R.id.editTextDelay);
                    view.setText("key down");
                }
            }
        }
    }
}
