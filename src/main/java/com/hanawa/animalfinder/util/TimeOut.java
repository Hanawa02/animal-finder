package com.hanawa.animalfinder.util;

import java.util.Timer;
import java.util.TimerTask;

public class TimeOut {

    private final Timer timer;
    private static TimeOut INSTANCE;

    private TimeOut() {
        timer = new Timer();
    }

    public static TimeOut getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TimeOut();
        }

        return INSTANCE;
    }

    public void setTimeout(Runnable runnable, int delay){
        TimerTask task = new TimerTask() {
            public void run() {
               runnable.run();
            }
        };

        timer.schedule(task, delay);
    }
}
