package edu.amd.spbstu.colorglue;

import android.view.MotionEvent;


public class Constants {
    public static final int VIEW_INTRO = 0;
    public static final int VIEW_TUTORIAL = 1;
    public static final int VIEW_GAME = 2;

    public static final String LOG_TAG = "COLOR_GLUE";

    public static final int TOUCH_DOWN = 0;
    public static final int TOUCH_MOVE = 1;
    public static final int TOUCH_UP = 2;

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
