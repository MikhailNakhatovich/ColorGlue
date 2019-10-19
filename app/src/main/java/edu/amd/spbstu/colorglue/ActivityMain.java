package edu.amd.spbstu.colorglue;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import edu.amd.spbstu.colorglue.intro.AppIntro;
import edu.amd.spbstu.colorglue.intro.ViewIntro;

import static edu.amd.spbstu.colorglue.Constants.*;


public class ActivityMain extends Activity implements View.OnTouchListener {

	int _viewCur = -1;
	
	AppIntro _app;
	ViewIntro _viewIntro;
    // ViewGame _viewGame;

	// screen dim
	int _screenW, _screenH;

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // No Status bar
        final Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Application is never sleeps
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        _screenW = point.x;
        _screenH = point.y;
        
        Log.d(LOG_TAG, "Screen size is " + _screenW + " * " +  _screenH);

        // Create application
        _app = new AppIntro(this);
        // Create view
        setView(VIEW_INTRO);
	}

	public void setView(int viewID) {
		if (_viewCur == viewID) {
			Log.d(LOG_TAG, "setView: already set");
			return;
		}

		_viewCur = viewID;
		switch (viewID) {
			case VIEW_INTRO:
				Log.d(LOG_TAG, "Switch to intro");
				_viewIntro = new ViewIntro(this);
				setContentView(_viewIntro);
				break;
			case VIEW_GAME:
				Log.d(LOG_TAG, "Switch to game");
//				_viewGame = new ViewGame(this);
//				setContentView(_viewGame);
//				_viewGame.start();
				break;
			default:
				Log.d(LOG_TAG, "Undefined id of view" );
				break;
		}
	}

	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}
	
    public boolean onTouch(View v, MotionEvent evt) {
    	int x = (int)evt.getX();
    	int y = (int)evt.getY();
    	int touchType = getTouchType(evt);
		
		if (_viewCur == VIEW_INTRO)
    	  return _viewIntro.onTouch(x, y, touchType);
		// if (_viewCur == VIEW_GAME)
		//   return _viewGame.onTouch(x, y, touchType);
		return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent evt) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//Log.d("THREE", "Back key pressed");
			//boolean wantKill = _app.onKey(Application.KEY_BACK);
			//if (wantKill)
		    //		finish();
			//return true;
		}
        return super.onKeyDown(keyCode, evt);
    }

    public AppIntro getApp() {
    	return _app;
    }
    
	protected void onResume() {
		super.onResume();
		switch (_viewCur) {
            case VIEW_INTRO:
                _viewIntro.start();
                break;
            case VIEW_GAME:
                // _viewGame.start();
                break;
            default:
                Log.d(LOG_TAG, "onResume: Invalid id of view");
                break;
        }
	}

	protected void onPause() {
	    // stop anims
        switch (_viewCur) {
            case VIEW_INTRO:
                _viewIntro.stop();
                break;
            case VIEW_GAME:
                // _viewGame.stop();
                break;
            default:
                break;
        }
	
	    // complete system
		super.onPause();
	}

	protected void onDestroy() {
		// if (_viewCur == VIEW_GAME)
		//     _viewGame.onDestroy();
		super.onDestroy();
	}
}

