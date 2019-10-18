package edu.amd.spbstu.colorglue;

import android.view.MotionEvent;


class Constants {
    static final int VIEW_INTRO = 0;
    static final int VIEW_GAME = 1;

    static final String LOG_TAG = "COLOR_GLUE";

    static final int TOUCH_DOWN = 0;
    static final int TOUCH_MOVE = 1;
    static final int TOUCH_UP = 2;

    static int getTouchType(MotionEvent evt) {
        switch (evt.getAction()) {
            case MotionEvent.ACTION_MOVE:
                return TOUCH_MOVE;
            case MotionEvent.ACTION_UP:
                return TOUCH_UP;
            case MotionEvent.ACTION_DOWN:
            default:
                return TOUCH_DOWN;
        }
    }
}
