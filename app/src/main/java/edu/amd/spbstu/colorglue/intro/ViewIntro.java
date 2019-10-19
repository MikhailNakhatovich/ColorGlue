package edu.amd.spbstu.colorglue.intro;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import edu.amd.spbstu.colorglue.ActivityMain;


class RedrawHandler extends Handler {
	private ViewIntro _viewIntro;
	
	RedrawHandler(ViewIntro v) {
		_viewIntro = v;
	}

	public void handleMessage(Message msg) {
		_viewIntro.update();
		_viewIntro.invalidate();
	}
	
	void sleep(long delayMillis) {
		this.removeMessages(0);
	    sendMessageDelayed(obtainMessage(0), delayMillis);
	}
}

public class ViewIntro extends View {
	// CONST
	private static final int UPDATE_TIME_MS = 30;

	// DATA
	ActivityMain _app;
	RedrawHandler _handler;
	boolean _active;
	
	// METHODS
	public ViewIntro(ActivityMain app) {
		super(app);
		_app = app;
		
		_handler = new RedrawHandler(this);
		_active = false;
		setOnTouchListener(app);
	}
	
	public void start() {
		_active = true;
		_handler.sleep(UPDATE_TIME_MS);
	}

	public void stop() {
		_active = false;
	}
	
	public void update() {
		if (!_active)
			  return;
		// send next update to game
		if (_active)
			_handler.sleep(UPDATE_TIME_MS);
	}

	public boolean onTouch(int x, int y, int evtType) {
		AppIntro app = _app.getApp();
		return app.onTouch(x,  y, evtType);
	}

	public void onDraw(Canvas canvas) {
		AppIntro app = _app.getApp();
		app.drawCanvas(canvas);
	}
}
