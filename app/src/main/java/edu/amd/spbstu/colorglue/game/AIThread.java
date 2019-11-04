package edu.amd.spbstu.colorglue.game;

import android.support.annotation.NonNull;
import android.util.Log;

import static edu.amd.spbstu.colorglue.Constants.LOG_TAG;

class AIThread extends Thread {
    private static final int TIME_SLEEP = 30;

    private boolean _isRunning, _isNeedMove, _isCalculated;
    private Field _field;
    private Integer _bestDir;

    AIThread() {
        _isRunning = _isNeedMove = _isCalculated = false;
        _field = null;
        _bestDir = null;
    }

    public void run() {
        while (_isRunning) {
            if (_isNeedMove) {
                _bestDir = null;
                _bestDir = AI.getBest(_field);
                _isCalculated = true;
               _isNeedMove = false;
            }
            try {
                Thread.sleep(TIME_SLEEP);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted in ai thread while sleeping");
            }
        }
    }

    synchronized Integer getBest() {
        return _bestDir;
    }

    synchronized boolean isNeedMove() {
        return _isNeedMove;
    }

    synchronized void startMove(@NonNull Field field) {
        _isNeedMove = true;
        _field = field;
        notCalculated();
    }

    synchronized boolean isCalculated() {
        return _isCalculated;
    }

    synchronized void notCalculated() {
        _isCalculated = false;
    }

    synchronized boolean isRunning() {
        return _isRunning;
    }

    synchronized void isRunning(boolean isRunning) {
        _isRunning = isRunning;
    }
}
