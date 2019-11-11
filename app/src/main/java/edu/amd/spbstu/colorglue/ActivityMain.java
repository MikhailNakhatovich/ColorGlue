package edu.amd.spbstu.colorglue;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import edu.amd.spbstu.colorglue.game.ViewGame;
import edu.amd.spbstu.colorglue.game.ViewTutorial;
import edu.amd.spbstu.colorglue.intro.AppIntro;
import edu.amd.spbstu.colorglue.intro.ViewIntro;

import static edu.amd.spbstu.colorglue.Constants.*;


public class ActivityMain extends Activity implements View.OnTouchListener {

	private int _viewCur = -1;

	private AppIntro _app;
	private ViewIntro _viewIntro;
	private ViewGame _viewGame;
	private ViewTutorial _viewTutorial;

	private boolean _isFirstGame;

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

		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		_isFirstGame = sharedPref.getBoolean(getString(R.string.str_saved_is_first_game), true);

        _app = new AppIntro(this);
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
			case VIEW_TUTORIAL:
				Log.d(LOG_TAG, "Switch to tutorial");
				_viewTutorial = new ViewTutorial(this);
				setContentView(_viewTutorial);
				_viewTutorial.start();
				break;
			case VIEW_GAME:
				Log.d(LOG_TAG, "Switch to game");
				_viewGame = new ViewGame(this);
				setContentView(_viewGame);
				_viewGame.start();
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

    	switch (_viewCur) {
			case VIEW_INTRO:
				return _viewIntro.onTouch(x, y, touchType);
			case VIEW_TUTORIAL:
				return _viewTutorial.onTouch(x, y, touchType);
			case VIEW_GAME:
				return _viewGame.onTouch(x, y, touchType);
			default:
				return false;
		}
    }

    public void startGame() {
		if (_isFirstGame) {
			setView(VIEW_TUTORIAL);
		} else {
			setView(VIEW_GAME);
		}
	}

	public void endTutorial() {
		_isFirstGame = false;
		SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.putBoolean(getString(R.string.str_saved_is_first_game), _isFirstGame);
		editor.apply();
		setView(VIEW_GAME);
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
			case VIEW_TUTORIAL:
				_viewTutorial.start();
				break;
            case VIEW_GAME:
                _viewGame.start();
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
			case VIEW_TUTORIAL:
				_viewTutorial.onPause();
				break;
            case VIEW_GAME:
            	_viewGame.onPause();
                break;
            default:
                break;
        }
	
	    // complete system
		super.onPause();
	}
}

