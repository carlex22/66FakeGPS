package com.carlex.drive;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.List;

public class TaskExecutor {
    private final MainActivity mainActivity;
    private final List<Long> timeValues;
    private final Handler handler;

    public TaskExecutor(MainActivity mainActivity, List<Long> timeValues) {
        this.mainActivity = mainActivity;
        this.timeValues = timeValues;
        HandlerThread handlerThread = new HandlerThread("TaskExecutorThread");
        handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());
    }

    public void start() {
        if (!timeValues.isEmpty()) {
       //     showStartToast();
            executeNextTask();
        }
    }


    public void stop() {
    // Remove all callbacks and messages
    handler.removeCallbacksAndMessages(null);
    
    // Quit the handler thread to stop it from running
    HandlerThread handlerThread = (HandlerThread) handler.getLooper().getThread();
    handlerThread.quitSafely();
    
    // Optionally, show a toast or perform other actions to indicate stopping
   // showEndToast();
    
    }
    private void executeNextTask() {
        if (!timeValues.isEmpty()) {
            long delay = timeValues.remove(0);
            handler.postDelayed(() -> {
                mainActivity.runOnUiThread(() -> {
                    mainActivity.centralizar();
                    executeNextTask();
                });
            }, delay);
        } else {
       //     showEndToast();
        }
    }
/*
    private void showStartToast() {
        mainActivity.runOnUiThread(() -> 
            mainActivity.showToast("Task started")
        );
    }

    private void showEndToast() {	
        mainActivity.runOnUiThread(() -> 
            mainActivity.showToast("Task ended")
        );
    }
    */
}

