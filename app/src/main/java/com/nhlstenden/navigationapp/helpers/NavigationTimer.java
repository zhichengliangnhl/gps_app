package com.nhlstenden.navigationapp.helpers;

import android.os.Handler;
import android.widget.TextView;

public class NavigationTimer {
    private long startTime = 0L;
    private long elapsedTimeBeforePause = 0L;
    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private TextView timerTextView;

    public NavigationTimer(TextView timerTextView) {
        this.timerTextView = timerTextView;
    }

    public void start(long baseTime) {
        this.startTime = baseTime;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                timerTextView.setText(formatTimer(elapsed));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    public void stop() {
        handler.removeCallbacks(timerRunnable);
    }

    public static String formatTimer(long millis) {
        long seconds = millis / 1000;
        long minutes = (seconds % 3600) / 60;
        long hours = seconds / 3600;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
} 