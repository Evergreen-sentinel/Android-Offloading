package com.example.offloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Simple BroadcastReceiver wrapper that calls back when battery low is broadcast.
 */
public class BatteryReceiver extends BroadcastReceiver {
    public interface BatteryListener {
        void onBatteryLow();
    }

    private final BatteryListener listener;

    public BatteryReceiver(BatteryListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            if (listener != null) listener.onBatteryLow();
        }
    }
}