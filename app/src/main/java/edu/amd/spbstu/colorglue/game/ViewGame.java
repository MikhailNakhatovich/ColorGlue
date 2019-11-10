package edu.amd.spbstu.colorglue.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;

import edu.amd.spbstu.colorglue.ActivityMain;
import edu.amd.spbstu.colorglue.R;

import static edu.amd.spbstu.colorglue.Constants.*;
import static edu.amd.spbstu.colorglue.game.GameConstants.*;


class RefreshHandler extends Handler {
	private ViewGame _viewGame;
	
	RefreshHandler(ViewGame v) {
		_viewGame = v;
	}

	public void handleMessage(Message msg) {
		_viewGame.update();
		_viewGame.invalidate();
	}
	
	void sleep(long delayMillis) {
		this.removeMessages(0);
	    sendMessageDelayed(obtainMessage(0), delayMillis);
	}
}


public class ViewGame extends View {
	private static final int TIME_GAME_STATE_APPEAR = 600;
	private static final int TIME_BACKGROUND_STATE_CHANGE = 600;
	private static final int TIME_SQUARE_MOVE = 200;
	private static final int TIME_SQUARE_PULSE = 100;
	private static final int TIME_SQUARE_APPEAR = 100;
	private static final int TIME_TOUCH_AGAIN = 200;
	private static final int TIME_DIALOG_APPEAR = 600;
	
	private static final int UPDATE_TIME_MS = 30;

	private static final int PLAY_USER = 0;
	private static final int PLAY_AUTO = 1;
	
	private static final int GAME_STATE_FIELD_APPEAR = 0;
	private static final int GAME_STATE_PLAY = 1;
	private static final int GAME_STATE_WIN_APPEAR = 2;
	private static final int GAME_STATE_WIN = 3;
	private static final int GAME_STATE_LOSE_APPEAR = 4;
	private static final int GAME_STATE_LOSE = 5;

	private static final int BACKGROUND_STATE_CHANGE = 0;
	private static final int BACKGROUND_STATE_SIT = 1;

	private static final float BAR_DELTA = 1.0f / (SQUARE_COUNT - 1);

	private boolean _active = false;
	private ActivityMain _app;
	private RefreshHandler _refresh;
	
	private String _strRestart, _strScore, _strBestScore, _strResult, _strScoreResult, _strAuto;
	
	private int _timeCur, _timePrev, _timeStateStart, _timeBackStateStart, _timeTouch;

	private int _gameState, _backgroundState, _curColor, _topClickCount, _whoPlays;
	private int _moves, _moveDirection;
	private int[] _colors, _barColors;
	private float[] _grads;
	private Field _gameField;
	private AIThread _aiThread;

	private Bitmap _bitmapBack;
	private Bitmap[] _bitmapSquare;

	private Paint _paintBitmap;
	private Paint _paintLine;
	private Paint _paintRectButton;
	private Paint _paintTextButton, _paintTextResult;

	private Rect _rectSrc, _rectDst, _rectText;
	private RectF _rectButtonRestart, _rectTopResult;

	private int _touchState;
	private boolean _isMoving;
	private int _touchX, _touchY;

	private float _yFieldUp, _yFieldLo;
	private float _yButtonsLo, _yBarLo;
	private float _cellSide;
	private int _scrW, _scrH;
	private float _xScale, _yScale;

	private int _gameScore, _gameBestScore;
	
	public ViewGame(ActivityMain app) {
		super(app);
		_app = app;
		_refresh = new RefreshHandler(this);
		setOnTouchListener(_app);
		
		_strRestart = app.getString(R.string.str_restart);
		_strScore = app.getString(R.string.str_score);
		_strBestScore = app.getString(R.string.str_best_score);
		_strAuto = app.getString(R.string.str_auto_mode);

		_scrW = -1;
		_timePrev = -1;
		
		_gameField = new Field();

		_paintBitmap = new Paint(Paint.ANTI_ALIAS_FLAG);
		_paintBitmap.setColor(0xFFFFFFFF);
		_paintBitmap.setStyle(Style.FILL);
		
		_rectSrc = new Rect();
		_rectDst = new Rect();
		_rectText = new Rect();
		_rectButtonRestart = new RectF();
		_rectTopResult = new RectF();

        Resources res = _app.getResources();
		_bitmapBack	= BitmapFactory.decodeResource(res, R.drawable.background);
		_bitmapSquare = new Bitmap[SQUARE_COUNT];
		_bitmapSquare[SQUARE_FIELD] = BitmapFactory.decodeResource(res, R.drawable.ico_field);
		_bitmapSquare[SQUARE_2] = BitmapFactory.decodeResource(res, R.drawable.ico_azure);
		_bitmapSquare[SQUARE_4] = BitmapFactory.decodeResource(res, R.drawable.ico_blue);
		_bitmapSquare[SQUARE_8] = BitmapFactory.decodeResource(res, R.drawable.ico_violet);
		_bitmapSquare[SQUARE_16] = BitmapFactory.decodeResource(res, R.drawable.ico_purple_pizzazz);
		_bitmapSquare[SQUARE_32] = BitmapFactory.decodeResource(res, R.drawable.ico_red);
		_bitmapSquare[SQUARE_64] = BitmapFactory.decodeResource(res, R.drawable.ico_blaze_orange);
		_bitmapSquare[SQUARE_128] = BitmapFactory.decodeResource(res, R.drawable.ico_yellow);
		_bitmapSquare[SQUARE_256] = BitmapFactory.decodeResource(res, R.drawable.ico_lime);
		_bitmapSquare[SQUARE_512] = BitmapFactory.decodeResource(res, R.drawable.ico_harlequin);
		_bitmapSquare[SQUARE_1024] = BitmapFactory.decodeResource(res, R.drawable.ico_turquoise);

		_colors = new int[SQUARE_COUNT];
		_colors[SQUARE_FIELD] = res.getColor(R.color.colorWhite);
		_colors[SQUARE_2] = res.getColor(R.color.colorSquare2);
		_colors[SQUARE_4] = res.getColor(R.color.colorSquare4);
		_colors[SQUARE_8] = res.getColor(R.color.colorSquare8);
		_colors[SQUARE_16] = res.getColor(R.color.colorSquare16);
		_colors[SQUARE_32] = res.getColor(R.color.colorSquare32);
		_colors[SQUARE_64] = res.getColor(R.color.colorSquare64);
		_colors[SQUARE_128] = res.getColor(R.color.colorSquare128);
		_colors[SQUARE_256] = res.getColor(R.color.colorSquare256);
		_colors[SQUARE_512] = res.getColor(R.color.colorSquare512);
		_colors[SQUARE_1024] = res.getColor(R.color.colorSquare1024);
		_barColors = Arrays.copyOfRange(_colors, SQUARE_2, SQUARE_COUNT);
		_grads = new float[_barColors.length + 1];
		float left = 0.0f;
		for (int k = 0; k < _grads.length; k++, left += BAR_DELTA) {
			_grads[k] = left;
		}
		_grads[_grads.length - 1] = 1.0f;
		
		_paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
		_paintLine.setColor(0xFFFFFFFF);
		_paintLine.setStyle(Style.FILL);
		
		_paintTextButton = new Paint(Paint.ANTI_ALIAS_FLAG);
		_paintTextButton.setColor(0xFF000088);
		_paintTextButton.setStyle(Style.FILL);
		_paintTextButton.setTextSize(20.0f);
		_paintTextButton.setTextAlign(Align.CENTER);

        _paintTextResult = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        _paintTextResult.setColor(0x00FFFF);
        _paintTextResult.setStyle(Style.FILL);
        _paintTextResult.setTextSize(20.0f);
        _paintTextResult.setTextAlign(Align.CENTER);
		
		_paintRectButton = new Paint(Paint.ANTI_ALIAS_FLAG);
		_paintRectButton.setStyle(Style.FILL);

		SharedPreferences sharedPref = _app.getPreferences(Context.MODE_PRIVATE);
		_gameBestScore = sharedPref.getInt(_app.getString(R.string.str_saved_best_score), 0);

		gameRestart();
	}

	public void start() {
		_active = true;
		_refresh.sleep(UPDATE_TIME_MS);
	}

	public void onPause() {
		_active = false;
	}
	
	public void update() {
		if (!_active)
			  return;
		// send next update to game
		if (_active)
			_refresh.sleep(Math.max(UPDATE_TIME_MS - (System.currentTimeMillis() - _timePrev), 0));
	}

	public boolean onTouch(int x, int y, int evtType) {
		if (_gameState <= GAME_STATE_FIELD_APPEAR)
			return true;

		// check top bar
		if (y < _yBarLo && _whoPlays == PLAY_USER && _gameState == GAME_STATE_PLAY) {
			if (evtType == TOUCH_DOWN) {
				if (_timeCur - _timeTouch < TIME_TOUCH_AGAIN) {
					_topClickCount++;
					if (_topClickCount >= 3) {
						_whoPlays = PLAY_AUTO;
						Toast.makeText(_app, _strAuto, Toast.LENGTH_SHORT).show();
						startAIThread();
						if (!_isMoving) {
							_aiThread.startMove(_gameField);
							_moves = 0;
						}
					}
				} else {
					_topClickCount = _topClickCount == 0 ? 1 : 0;
				}
				_timeTouch = _timeCur;
				return true;
			}
		}
		
		// check buttons press
		if (evtType == TOUCH_DOWN) {
			if (_rectButtonRestart.contains(x, y)) {
				gameRestart();
				return true;
			}
		}
		
		// check game field
		if (!_isMoving && _gameState == GAME_STATE_PLAY && _whoPlays == PLAY_USER && _moves == 0) {
			if (evtType == TOUCH_DOWN) {
				_touchState = 1;
				_touchX = x;
				_touchY = y;
			} else if (evtType == TOUCH_UP) {
				_touchState = 0;
			} else {
				if (y == _touchY || x == _touchX) {
					if (x == _touchX && y == _touchY) return true;
				} else {
					float ratio = (float)Math.abs(x - _touchX) / Math.abs(y - _touchY);
					if (ratio < 1.2 && ratio > 0.83) return true;
				}
				if (_touchState == 1) {
					_moveDirection = getDirection(x, y);
					_isMoving = _gameField.startMove(_moveDirection, _timeCur, TIME_SQUARE_MOVE);
					if (_isMoving) _moves = 1;
				}
				_touchState = 0;
			}
		}
		return true;
	}

	public void onDraw(Canvas canvas) {
		int	opacityBackground;

		_timeCur = (int)(System.currentTimeMillis() & 0x3fffffff);
		_timePrev = _timeCur;

		if (_timeStateStart < 0) _timeStateStart = _timeCur;

		// change state
		opacityBackground = 255;
		if (_gameState == GAME_STATE_FIELD_APPEAR) {
			opacityBackground = getOpacityBackground(TIME_GAME_STATE_APPEAR);
		}

		if (_scrW < 0) prepareScreenValues(canvas);

		// Render game parts
		drawBackground(canvas, opacityBackground);

		if (_gameState > GAME_STATE_FIELD_APPEAR) {
			drawSquares(canvas);
			if (_gameState == GAME_STATE_PLAY) {
				checkMovedSquares();
			} else {
				opacityBackground = 255;
				if (_gameState == GAME_STATE_WIN_APPEAR || _gameState == GAME_STATE_LOSE_APPEAR) {
					opacityBackground = getOpacityBackground(TIME_DIALOG_APPEAR);
				}
				drawResult(canvas, opacityBackground);
			}
		}
	}

	protected void onDetachedFromWindow() {
		stopAIThread();
		super.onDetachedFromWindow();
	}

	private void gameRestart() {
		stopAIThread();
		_timeCur = (int)(System.currentTimeMillis());

		_gameState = GAME_STATE_FIELD_APPEAR;
		_timeCur = _timeStateStart = _timeBackStateStart = _timeTouch = -1;
		_touchState = 0;
		_touchX = _touchY = -1;
		_gameScore = 0;
		_isMoving = false;
		_curColor = SQUARE_2;
		_backgroundState = BACKGROUND_STATE_SIT;
		_topClickCount = 0;
		_moves = 0;
		_moveDirection = MOVE_LEFT;
		_whoPlays = PLAY_USER;

		for (int i = 0; i < NUM_CELLS * NUM_CELLS; ++i) {
			_gameField.removeSquare(i);
		}
		_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR);
		_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR);
	}

	private int getDirection(int x, int y) {
		int dx = Math.abs(x - _touchX), dy = Math.abs(y - _touchY);
		if (y - _touchY > dx) return MOVE_DOWN;
		else if (-y + _touchY > dx) return MOVE_UP;
		else if (x - _touchX > dy) return MOVE_RIGHT;
		else return MOVE_LEFT;
	}

	private void saveScore() {
		SharedPreferences sharedPref = _app.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(_app.getString(R.string.str_saved_best_score), _gameBestScore);
		editor.apply();
	}

	private void initScoreResults(boolean isWin) {
		stopAIThread();
		_gameBestScore = Math.max(_gameBestScore, _gameScore);
		_strScoreResult = _app.getString(R.string.str_score_result, _gameScore);
		_strResult = _app.getString(isWin ? R.string.str_win_result : R.string.str_lose_result);
		saveScore();
	}

	private void startLose() {
		_gameState = GAME_STATE_LOSE_APPEAR;
		_timeStateStart = _timeCur;
		initScoreResults(false);
	}

	private void startWin() {
		_gameState = GAME_STATE_WIN_APPEAR;
		_timeStateStart = _timeCur;
		initScoreResults(true);
	}

	private int getScreenCoordXByCell(int indexCell) {
		int c, x;
		c = Field.getColByCell(indexCell);
		x = (int)(c * _cellSide + _cellSide * 0.5f);
		return x;
	}

	private int getScreenCoordYByCell(int indexCell) {
		int r, y;
		r = Field.getRowByCell(indexCell);
		y = (int)(_yFieldUp + r * _cellSide + _cellSide * 0.5f);
		return y;
	}

	private void checkMovedSquares() {
		if (_gameState != GAME_STATE_PLAY) return;
		if (_isMoving && !_gameField.checkMoveCells()) {
			if (_moves > 0 && _moves < MOVE_TIMES) {
				_isMoving = _gameField.startMove(_moveDirection, _timeCur, TIME_SQUARE_MOVE);
				if (_isMoving) {
					_moves++;
					return;
				} else {
					_moves = 0;
				}
			} else if (_moves == MOVE_TIMES) {
				_moves = 0;
			}
			_isMoving = false;
			if (_curColor == SQUARE_WIN) {
				startWin();
				return;
			} else if (!_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR) || !_gameField.isPossibleToMove()) {
				startLose();
				return;
			}
			if (_whoPlays == PLAY_AUTO) {
				if (!_aiThread.isNeedMove() && !_aiThread.isCalculated()) {
					_aiThread.startMove(_gameField);
				} else if (_aiThread.isCalculated()) {
					_moveDirection = _aiThread.getBest();
					_aiThread.notCalculated();
					_isMoving = _gameField.startMove(_moveDirection, _timeCur, TIME_SQUARE_MOVE);
					_moves = 1;
				}
			}
		} else if (!_isMoving && _whoPlays == PLAY_AUTO && _aiThread.isCalculated() && _moves == 0) {
			_moveDirection = _aiThread.getBest();
			_aiThread.notCalculated();
			_isMoving = _gameField.startMove(_moveDirection, _timeCur, TIME_SQUARE_MOVE);
			_moves = 1;
		}
	}

	private void drawEmptyButton(Canvas canvas, RectF rect, int color1, int color2, int alpha) {
		int scrW = canvas.getWidth();
		float rectRad = scrW * 0.04f;
		float rectBord = scrW * 0.005f;

		RectF rectInside = new RectF(rect.left + rectBord, rect.top + rectBord, rect.right - rectBord, rect.bottom - rectBord);

		int[] colors = {0, 0};
		colors[0] = color1 | (alpha << 24);
		colors[1] = color2 | (alpha << 24);
		LinearGradient shader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, colors, null, Shader.TileMode.CLAMP);
		Paint paintInside = new Paint();
		paintInside.setAntiAlias(true);
		paintInside.setShader(shader);

		_paintRectButton.setColor(0xFFFFFF | (alpha<<24));
		canvas.drawRoundRect(rect, rectRad, rectRad, _paintRectButton);
		_paintRectButton.setColor(0x808080 | (alpha<<24));
		canvas.drawRoundRect(rectInside, rectRad, rectRad, paintInside);
	}

	private void prepareScreenValues(Canvas canvas) {
		// Get canvas (screen) size
		_scrW = canvas.getWidth();
		_scrH = canvas.getHeight();
		
		_xScale = _scrW / 768.0f;
		_yScale = _scrH / 1280.0f;

		// Prepare to Draw grid
		_cellSide = (float)(_scrW - 1) / NUM_CELLS;
		_yFieldUp = (_scrH - _scrW) * 0.5f;
		_yFieldLo = _yFieldUp + _scrW;

		_paintTextButton.setTextSize(_scrH * 0.02f);
        _paintTextResult.setTextSize(_scrW * 0.1f);

		// Low buttons
		_yButtonsLo = (_scrH + _yFieldLo) * 0.5f;
		_yBarLo = _yFieldUp * 0.5f - 80 * _yScale * 0.8f;
	}

	private void drawText(Canvas canvas, String str, float x, float y, float mult) {
		_paintTextButton.getTextBounds(str, 0, str.length(), _rectText);
		float h = _rectText.height();
		canvas.drawText(str, x, y + h * mult, _paintTextButton);
	}
	
	private void drawBackground(Canvas canvas, int opacityBackground) {
		int r, c, x, y, curColor1 = (_curColor + 1) % SQUARE_COUNT;
		int xPad = (int)(3.0f * _xScale);
		int yPad = (int)(3.0f * _yScale);
		float dt;
		
		_paintBitmap.setAlpha(opacityBackground);
		_paintLine.setAlpha(opacityBackground);
	
		// Draw back gradient
		_rectDst.set(0, 0, _scrW, _scrH);
		Paint paintInside = new Paint();
		paintInside.setAntiAlias(true);
		LinearGradient shaderRad = new LinearGradient(0, 0, 0, _scrH, _colors[curColor1], _colors[_curColor], Shader.TileMode.MIRROR);
		dt = _timeCur - _timeBackStateStart;
		if (_backgroundState == BACKGROUND_STATE_SIT || dt > TIME_BACKGROUND_STATE_CHANGE) {
			_backgroundState = BACKGROUND_STATE_SIT;
			paintInside.setShader(shaderRad);
			paintInside.setAlpha(opacityBackground);
			canvas.drawRect(_rectDst, paintInside);
		} else {
			dt /= TIME_BACKGROUND_STATE_CHANGE;
			shaderRad = new LinearGradient(0, 0, 0, _scrH,
					new int[] {_colors[curColor1], _colors[_curColor], _colors[_curColor - 1]},
					new float[] {0.0f, dt, 1.0f}, Shader.TileMode.MIRROR);
			paintInside.setShader(shaderRad);
			paintInside.setAlpha(opacityBackground);
			canvas.drawRect(_rectDst, paintInside);
		}

		// Draw progress
		paintInside.setShader(null);
		for (int k = 0; k < _barColors.length; ++k) {
			paintInside.setColor(_barColors[k]);
			paintInside.setAlpha(opacityBackground);
			canvas.drawRect(_scrW * _grads[k], 0, _scrW * _grads[k + 1], _yBarLo, paintInside);
		}
		paintInside.setColor(0xB4FFFFFF);
		if (_backgroundState == BACKGROUND_STATE_CHANGE) {
			canvas.drawRect(_scrW * (_grads[_curColor - 1] + dt * BAR_DELTA), 0, _scrW, _yBarLo, paintInside);
		} else if (_curColor != SQUARE_WIN) {
				canvas.drawRect(_scrW * _grads[_curColor], 0, _scrW, _yBarLo, paintInside);
		}

		// Draw background of the grid
        _paintBitmap.setAlpha(opacityBackground);
        drawBitmap(canvas, _bitmapBack, 0, (int)_yFieldUp, _scrW, (int)_yFieldLo);

		for (int k = 0; k < NUM_CELLS * NUM_CELLS; k++) {
			r = Field.getRowByCell(k);
			c = Field.getColByCell(k);
			x = (int) (c * _cellSide);
			y = (int) (r * _cellSide + _yFieldUp);
            drawBitmap(canvas, _bitmapSquare[SQUARE_FIELD],
                    x + xPad, y + yPad,
                    (int)(x + _cellSide - xPad), (int)(y + _cellSide - yPad));
		}

		// Prepare Restart button coordinates
		float btnW = 280 * _xScale, btnH = 80 * _yScale, btnX = _scrW * 0.5f, btnY = _yButtonsLo;
		_paintTextButton.setAlpha(opacityBackground);

		// render button Restart
		_rectButtonRestart.set(btnX - btnW * 0.5f, btnY - btnH * 0.5f, btnX + btnW * 0.5f, btnY + btnH * 0.5f);
		drawEmptyButton(canvas, _rectButtonRestart, 0x92DCFE, 0x1e80B0, opacityBackground);
		drawText(canvas, _strRestart, _rectButtonRestart.centerX(), _rectButtonRestart.centerY(), 0.5f);
		
		// Top Result Left
		float cell4Side = (float)(_scrW - 1) / 4;
		btnW = 4 * cell4Side / 3;
		float yc = _yFieldUp * 0.5f, xc = xPad + cell4Side / 3 + btnW * 0.5f;

		_rectTopResult.set(xPad + cell4Side / 3, yc - btnH * 0.75f, xPad + 5 * cell4Side / 3, yc + btnH * 0.75f);
		drawEmptyButton(canvas, _rectTopResult, 0x92DCFE, 0x1e80B0, opacityBackground);
		drawText(canvas, _strScore, xc, yc, -0.25f);
		drawText(canvas, String.valueOf(_gameScore), xc, yc, 1.25f);

		// Top Result Right
		xc = xPad + _scrW / 2.0f + cell4Side / 3 + btnW * 0.5f;

		_rectTopResult.set(xPad + _scrW / 2.0f + cell4Side / 3, yc - btnH * 0.75f, xPad + _scrW / 2.0f + 5 * cell4Side / 3, yc + btnH * 0.75f);
		drawEmptyButton(canvas, _rectTopResult, 0x92DCFE, 0x1e80B0, opacityBackground);
		drawText(canvas, _strBestScore, xc, yc, -0.25f);
		drawText(canvas, String.valueOf(_gameBestScore), xc, yc, 1.25f);
	}

	private void drawBitmap(Canvas canvas, Bitmap bmp, int ldst, int tdst, int rdst, int ddst) {
		_rectDst.set(ldst, tdst, rdst, ddst);
		_rectSrc.set(0, 0, bmp.getWidth(), bmp.getHeight());
		canvas.drawBitmap(bmp, _rectSrc, _rectDst, _paintBitmap);
	}

	private void drawAppearSquare(Canvas canvas, int k, int xPad, int yPad) {
		Square square = _gameField.getSquare(k);
		float t = (float)(_timeCur - square._timeStart) / (square._timeEnd - square._timeStart);
		if (t < 1.0f) {
			int r = Field.getRowByCell(k), c = Field.getColByCell(k);
			int x = (int)(c * _cellSide), y = (int)(r * _cellSide + _yFieldUp);
			double shift = (_cellSide / 4.0) * Math.cos(t * Math.PI * 0.5);
			drawBitmap(canvas, _bitmapSquare[square._indexBitmap],
					(int)(x + xPad + shift), (int)(y + yPad + shift),
					(int)(x + _cellSide - shift - xPad), (int)(y + _cellSide - shift - yPad));
		} else {
			square._state = Square.STATE_SIT;
			drawSitSquare(canvas, k, xPad, yPad);
		}
	}

	private void drawSquareAtPoint(Canvas canvas, Bitmap bmp, int k, int xPad, int yPad) {
		int r = Field.getRowByCell(k), c = Field.getColByCell(k);
		int x = (int)(c * _cellSide), y = (int)(r * _cellSide + _yFieldUp);
		drawBitmap(canvas, bmp,
				x + xPad, y + yPad,
				(int)(x + _cellSide - xPad), (int)(y + _cellSide - yPad));
	}

	private void drawSitSquare(Canvas canvas, int k, int xPad, int yPad) {
		drawSquareAtPoint(canvas, _bitmapSquare[_gameField.getSquare(k)._indexBitmap], k, xPad, yPad);
	}

	private void drawMoveSquare(Canvas canvas, int k, int xPad, int yPad) {
		Square square = _gameField.getSquare(k);
		int	x0, y0, x1, y1, xMin, yMin, xMax, yMax, xc, yc;

		x0 = getScreenCoordXByCell(square._cellSrc);
		y0 = getScreenCoordYByCell(square._cellSrc);
		x1 = getScreenCoordXByCell(square._cellDst);
		y1 = getScreenCoordYByCell(square._cellDst);
		float t = (float)(_timeCur - square._timeStart) / (square._timeEnd - square._timeStart);
		if (t < 1.0f) {
			xc = (int)(x0 * (1.0f - t) + x1 * t);
			yc = (int)(y0 * (1.0f - t) + y1 * t);
			xMin = (int)(xc - _cellSide * 0.5f + xPad);
			yMin = (int)(yc - _cellSide * 0.5f + yPad);
			xMax = (int)(xc + _cellSide * 0.5f - xPad);
			yMax = (int)(yc + _cellSide * 0.5f - yPad);
			drawBitmap(canvas, _bitmapSquare[square._indexBitmap], xMin, yMin, xMax, yMax);
		} else {
			int indexSrc = square._cellSrc;
			int indexDst = square._cellDst;
			Square squareDst = _gameField.getSquare(indexDst);

			if (squareDst == null) {
				square._cellSrc = indexDst;
				square._state = Square.STATE_SIT;
				_gameField.removeSquare(indexSrc);
				_gameField.insertSquare(square);
				drawSitSquare(canvas, indexDst, xPad, yPad);
			} else if (squareDst._cellDst == indexDst) {
				_gameField.removeSquare(indexSrc);
				squareDst._state = Square.STATE_PULSE;
				square._timeStart = _timeCur;
				squareDst._timeEnd = _timeCur + TIME_SQUARE_PULSE;
				if (squareDst._indexBitmap < SQUARE_WIN) squareDst._indexBitmap++;
                _gameScore += getScore(square._indexBitmap);
				if (squareDst._indexBitmap > _curColor && _backgroundState == BACKGROUND_STATE_SIT) {
					_backgroundState = BACKGROUND_STATE_CHANGE;
					_timeBackStateStart = _timeCur;
					_curColor = squareDst._indexBitmap;
				}
			} else {
				drawSquareAtPoint(canvas, _bitmapSquare[square._indexBitmap], indexDst, xPad, yPad);
			}
		}
	}

	private void drawPulseSquare(Canvas canvas, int k, int xPad, int yPad) {
		Square square = _gameField.getSquare(k);
		float t = (float)(_timeCur - square._timeStart) / (square._timeEnd - square._timeStart);
		if (t < 1.0f) {
			int r = Field.getRowByCell(k), c = Field.getColByCell(k);
			int x = (int)(c * _cellSide), y = (int)(r * _cellSide + _yFieldUp);
			double shift = 4 * Math.max(xPad, yPad) * Math.sin(t * Math.PI);
			drawBitmap(canvas, _bitmapSquare[square._indexBitmap],
					(int)(x + xPad - shift), (int)(y + yPad - shift),
					(int)(x + _cellSide + shift - xPad), (int)(y + _cellSide + shift - yPad));
		} else {
			square._state = Square.STATE_SIT;
			drawSitSquare(canvas, k, xPad, yPad);
		}
	}
	
	private void drawSquares(Canvas canvas) {
		int xPad = (int)(3.0f * _xScale), yPad = (int)(3.0f * _yScale);
		_paintBitmap.setAlpha(255);

		for (int k = 0; k < NUM_CELLS * NUM_CELLS; k++) {
			if (_gameField.getSquare(k) == null) continue;
			switch (_gameField.getSquare(k)._state) {
				case Square.STATE_APPEAR:
					drawAppearSquare(canvas, k, xPad, yPad);
					break;
				case Square.STATE_SIT:
					drawSitSquare(canvas, k, xPad, yPad);
					break;
				case Square.STATE_MOVE:
					drawMoveSquare(canvas, k, xPad, yPad);
					break;
				case Square.STATE_PULSE:
					drawPulseSquare(canvas, k, xPad, yPad);
					break;
				default:
					break;
			}
		}
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

    private int getOpacityBackground(int time) {
        long dt = _timeCur - _timeStateStart;
        int	opacityBackground = 255;
        if (dt > time) {
            _gameState++;
            _timeStateStart = -1;
        } else {
            // calculate background opacity
            opacityBackground = ((int)dt * 255) / time;
            if (opacityBackground > 255)
                opacityBackground = 255;
        }
        return opacityBackground;
    }

    private void startAIThread() {
		if (_aiThread == null) {
			_aiThread = new AIThread();
			_aiThread.isRunning(true);
			_aiThread.start();
		}
	}

    private void stopAIThread() {
		if (_aiThread != null && _aiThread.isRunning()) {
			_aiThread.isRunning(false);
			boolean retry = true;
			while (retry) {
				try {
					_aiThread.join();
					retry = false;
				} catch (InterruptedException e) {}
			}
			_aiThread = null;
		}
	}
}
