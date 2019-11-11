package edu.amd.spbstu.colorglue.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;

import edu.amd.spbstu.colorglue.ActivityMain;


public class ViewGame extends ViewGameBase {
	private Paint _paintTextResult;

	public ViewGame(ActivityMain app) {
		super(app);

		_paintTextResult = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
		_paintTextResult.setColor(0x00FFFF);
		_paintTextResult.setStyle(Style.FILL);
		_paintTextResult.setTextAlign(Align.CENTER);
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int	opacityBackground;

		if (_gameState > GAME_STATE_PLAY) {
			opacityBackground = 255;
			if (_gameState == GAME_STATE_WIN_APPEAR || _gameState == GAME_STATE_LOSE_APPEAR) {
				opacityBackground = getOpacityBackground(TIME_DIALOG_APPEAR);
			}
			drawResult(canvas, opacityBackground);
		}
	}

	protected void restart() {
		super.restart();

		_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR);
		_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR);
	}

	protected void prepareScreenValues(Canvas canvas) {
		super.prepareScreenValues(canvas);
		_paintTextResult.setTextSize(_scrW * 0.1f);
	}

	private void drawResult(Canvas canvas, int opacityBackground) {
	    float xc = _scrW * 0.5f, yc = _yFieldUp + _scrW / 2.0f;
        Rect rText = new Rect();

        _paintBitmap.setAlpha(2 * opacityBackground / 3);
        drawBitmap(canvas, _bitmapBack, 0, (int)(_yFieldUp), _scrW, (int)(_yFieldLo));

        _paintTextResult.setAlpha(opacityBackground);

        _paintTextResult.getTextBounds(_strResult, 0, _strResult.length(), rText);
        canvas.drawText(_strResult, xc, yc - rText.height(), _paintTextResult);

        _paintTextResult.getTextBounds(_strScoreResult, 0, _strScoreResult.length(), rText);
        canvas.drawText(_strScoreResult, xc, yc + 2 * rText.height(), _paintTextResult);
    }
}
