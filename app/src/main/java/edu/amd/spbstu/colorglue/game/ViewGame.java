package edu.amd.spbstu.colorglue.game;

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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

import edu.amd.spbstu.colorglue.ActivityMain;
import edu.amd.spbstu.colorglue.R;

import static edu.amd.spbstu.colorglue.Constants.*;


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
	
	private static final int UPDATE_TIME_MS = 30;
	
	private static final int GAME_STATE_FIELD_APPEAR = 0;
	private static final int GAME_STATE_PLAY = 1;
	private static final int GAME_STATE_WIN_APPEAR = 2;
	private static final int GAME_STATE_WIN = 3;
	private static final int GAME_STATE_LOSE_APPEAR = 4;
	private static final int GAME_STATE_LOSE = 5;

	private static final int BACKGROUND_STATE_CHANGE = 0;
	private static final int BACKGROUND_STATE_SIT = 1;

	private static final int SQUARE_FIELD = 0;
	private static final int SQUARE_2 = 1;
	private static final int SQUARE_4 = 2;
	private static final int SQUARE_8 = 3;
	private static final int SQUARE_16 = 4;
	private static final int SQUARE_32 = 5;
	private static final int SQUARE_64 = 6;
	private static final int SQUARE_128 = 7;
	private static final int SQUARE_256 = 8;
	private static final int SQUARE_512 = 9;
	private static final int SQUARE_1024 = 10;

	private static final int SQUARE_COUNT = 11;
	
	private static final int NUM_CELLS = 4;

	private static final int MOVE_LEFT = 0;
	private static final int MOVE_UP = 1;
	private static final int MOVE_RIGHT = 2;
	private static final int MOVE_DOWN = 3;

	private static final int[][] _start_indices = {{0, 0}, {0, 0}, {3, 0}, {0, 3}};
	private static final int[][] _outer_iterators = {{0, 1}, {1, 0}, {0, 1}, {1, 0}};
	private static final int[][] _inner_iterators = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};

	private boolean _active = false;
	private ActivityMain _app;
	private RefreshHandler _refresh;
	
	private String _strRestart, _strScore, _strBestScore;
	
	private int _timeCur, _timePrev, _timeStateStart, _timeBackStateStart;

	private int _gameState, _backgroundState, _curColor;
	private int[] _colors;
	private Square[] _gameField;

	private Bitmap _bitmapBack;
	private Bitmap[] _bitmapSquare;

	private Paint _paintBitmap;
	private Paint _paintLine;
	private Paint _paintRectButton;
	private Paint _paintTextButton;

	private Rect _rectSrc;
	private Rect _rectDst;
	private RectF _rectButtonRestart;

	private Paint _paintButtonOutline;
	private Paint _paintButtonLeft;
	private Paint _paintButtonRight;

	private int _touchState;
	private boolean _is_moving;
	private int _touchX, _touchY;

	private float _yFieldUp;
	private float _yFieldLo;
	private float _yButtonsLo;
	private float _cellSide;
	private int _scrW, _scrH;
	private float _xScale, _yScale;

	private int _gameScore, _gameBestScore;

	private Random _randomGen;
	
	public ViewGame(ActivityMain app) {
		super(app);
		_app = app;
		_refresh = new RefreshHandler(this);
		setOnTouchListener(_app);
		
		_randomGen = new Random();
		
		_strRestart = app.getString(R.string.str_restart);
		_strScore = app.getString(R.string.str_score);
		_strBestScore = app.getString(R.string.str_best_score);

		_gameState = GAME_STATE_FIELD_APPEAR;
		_timeCur = _timeStateStart = -1;
		_touchState = 0;
		_gameScore = 0;
		_gameBestScore = 0;
		_scrW = -1;
		_timePrev = -1;
		
		// Init game field with random Squares
		_gameField = new Square[NUM_CELLS * NUM_CELLS];

		_paintBitmap = new Paint();
		_paintBitmap.setColor(0xFFFFFFFF);
		_paintBitmap.setStyle(Style.FILL);
		_paintBitmap.setAntiAlias(true);
		
		_rectSrc = new Rect();
		_rectDst = new Rect();
		_rectButtonRestart = new RectF();

		_bitmapBack	= BitmapFactory.decodeResource(app.getResources(), R.drawable.background);
		_bitmapSquare = new Bitmap[SQUARE_COUNT];
		_bitmapSquare[SQUARE_FIELD] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_field);
		_bitmapSquare[SQUARE_2] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_azure);
		_bitmapSquare[SQUARE_4] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_blue);
		_bitmapSquare[SQUARE_8] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_violet);
		_bitmapSquare[SQUARE_16] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_purple_pizzazz);
		_bitmapSquare[SQUARE_32] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_red);
		_bitmapSquare[SQUARE_64] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_blaze_orange);
		_bitmapSquare[SQUARE_128] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_yellow);
		_bitmapSquare[SQUARE_256] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_lime);
		_bitmapSquare[SQUARE_512] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_harlequin);
		_bitmapSquare[SQUARE_1024] = BitmapFactory.decodeResource(app.getResources(), R.drawable.ico_turquoise);

		_colors = new int[SQUARE_COUNT];
		_colors[SQUARE_FIELD] = 0xFFFFFFFF;
		_colors[SQUARE_2] = 0xFF0094FF;
		_colors[SQUARE_4] = 0xFF0026FF;
		_colors[SQUARE_8] = 0xFFB200FF;
		_colors[SQUARE_16] = 0xFFFF00DC;
		_colors[SQUARE_32] = 0xFFFF0000;
		_colors[SQUARE_64] = 0xFFFF6A00;
		_colors[SQUARE_128] = 0xFFFFD800;
		_colors[SQUARE_256] = 0xFFB6FF00;
		_colors[SQUARE_512] = 0xFF4CFF00;
		_colors[SQUARE_1024] = 0xFF00FFFF;
		
		_paintLine = new Paint();
		_paintLine.setColor(0xFFFFFFFF);
		_paintLine.setStyle(Style.FILL);
		_paintLine.setAntiAlias(true);
		
		_paintTextButton = new Paint();
		_paintTextButton.setColor(0xFF000088);
		_paintTextButton.setStyle(Style.FILL);
		_paintTextButton.setTextSize(20.0f);
		_paintTextButton.setTextAlign(Align.CENTER);
		_paintTextButton.setAntiAlias(true);
		
		_paintRectButton = new Paint();
		_paintRectButton.setStyle(Style.FILL);
		_paintRectButton.setAntiAlias(true);

		gameRestart();
	}

	private void addNewSquare(int cellIndex) {
		_gameField[cellIndex] = new Square();
		_gameField[cellIndex]._cellSrc = cellIndex;
		_gameField[cellIndex]._cellDst = -1;
		_gameField[cellIndex]._indexBitmap = SQUARE_2;
		_gameField[cellIndex]._state = Square.STATE_APPEAR;
		_gameField[cellIndex]._timeStart = _timeCur;
		_gameField[cellIndex]._timeEnd = _timeCur + TIME_SQUARE_APPEAR;
	}

	private void gameRestart() {
		_timeCur = (int)(System.currentTimeMillis());

		_gameState = GAME_STATE_FIELD_APPEAR;
		_timeCur = _timeStateStart = _timeBackStateStart = -1;
		_touchState = 0;
		_touchX = _touchY = -1;
		_gameScore = 0;
		_is_moving = false;
		_curColor = SQUARE_2;
		_backgroundState = BACKGROUND_STATE_SIT;

		for (int i = 0; i < NUM_CELLS * NUM_CELLS; ++i) {
			_gameField[i] = null;
		}
		int k = _randomGen.nextInt(NUM_CELLS * NUM_CELLS);
		int l = _randomGen.nextInt(NUM_CELLS * NUM_CELLS);
		while (l == k) l = _randomGen.nextInt(NUM_CELLS * NUM_CELLS);
		addNewSquare(k);
		addNewSquare(l);
	}
	
	public boolean performClick() {
		return super.performClick();
	}

	public void start() {
		_active = true;
		_refresh.sleep(UPDATE_TIME_MS);
	}

	public void onPause() {
		_active = false;
	}

	public void onDestroy() {}
	
	public void update() {
		if (!_active)
			  return;
		// send next update to game
		if (_active)
			_refresh.sleep(UPDATE_TIME_MS);
	}

	private void startLose() {
		_gameState = GAME_STATE_LOSE_APPEAR;
		_timeStateStart = _timeCur;
	}

	private void startWin() {
		_gameState = GAME_STATE_WIN_APPEAR;
		_timeStateStart = _timeCur;
	}

	private int getRowByCell(int cellIndex) {
		return cellIndex / NUM_CELLS;
	}

	private int getColByCell(int cellIndex) {
		int r = cellIndex / NUM_CELLS;
		int c;
		c = +cellIndex - r * NUM_CELLS;
		if (c < 0)
			c += NUM_CELLS;
		return c;
	}

	private int getScreenCoordXByCell(int indexCell) {
		int c, x;
		c = getColByCell(indexCell);
		x = (int)(c * _cellSide + _cellSide * 0.5f);
		return x;
	}

	private int getScreenCoordYByCell(int indexCell) {
		int r, y;
		r = getRowByCell(indexCell);
		y = (int)(_yFieldUp + r * _cellSide + _cellSide * 0.5f);
		return y;
	}

	private Square getSquareForCell(int c, int r) {
		if (!checkNumInGrid(c) || !checkNumInGrid(r)) return null;
		return _gameField[r * NUM_CELLS + c];
	}

	private boolean checkNumInGrid(int n) {
		return n >= 0 && n < NUM_CELLS;
	}

	private void moveSquare(Square square, int cellDst) {
		square._cellDst = cellDst;
		square._state = Square.STATE_MOVE;
		square._timeStart = _timeCur;
		square._timeEnd = _timeCur + TIME_SQUARE_MOVE;
	}

	private int findNotEmptySquareByXY(int x, int y, int d, int c) {
		int ans;
		if (c == 0) {
			for (ans = y; checkNumInGrid(ans) && getSquareForCell(x, ans) == null; ans += d);
		} else {
			for (ans = x; checkNumInGrid(ans) && getSquareForCell(ans, y) == null; ans += d);
		}
		return ans;
	}

	private boolean startMove(int direction) {
		int al_direction = (direction + 2) % 4, prevX, prevY;
		int sx = _start_indices[direction][0], sy = _start_indices[direction][1];
		int aidx = _inner_iterators[al_direction][0], aidy = _inner_iterators[al_direction][1];
		int odx = _outer_iterators[al_direction][0], ody = _outer_iterators[al_direction][1];
		Square square, prevSquare;
		int movements = 0;

		if ((direction & 1) == 0) {
			for (int y = sy; checkNumInGrid(y); y += ody) {
				prevX = findNotEmptySquareByXY(sx, y, aidx, 1);
				if (!checkNumInGrid(prevX)) continue;
				for (int x = prevX + aidx, dstX = sx; checkNumInGrid(prevX);) {
					x = findNotEmptySquareByXY(x, y, aidx, 1);
					square = getSquareForCell(x, y);
					prevSquare = getSquareForCell(prevX, y);
					if (square != null && prevSquare._indexBitmap == square._indexBitmap) {
						if (dstX != prevX) {
							moveSquare(prevSquare, y * NUM_CELLS + dstX);
							moveSquare(square, prevSquare._cellDst);
						} else {
							moveSquare(square, prevSquare._cellSrc);
							prevSquare._cellDst = prevSquare._cellSrc;
						}
						movements++;
						prevX = findNotEmptySquareByXY(x + aidx, y, aidx, 1);
					} else {
						if (prevX != dstX) moveSquare(prevSquare, y * NUM_CELLS + dstX);
						prevX = findNotEmptySquareByXY(x, y, aidx, 1);
					}
					dstX += aidx;
					x = prevX + aidx;
				}
			}
		} else {
			for (int x = sx; checkNumInGrid(x); x += odx) {
				prevY = findNotEmptySquareByXY(x, sy, aidy, 0);
				if (!checkNumInGrid(prevY)) continue;
				for (int y = prevY + aidy, dstY = sy; checkNumInGrid(prevY);) {
					y = findNotEmptySquareByXY(x, y, aidy, 0);
					square = getSquareForCell(x, y);
					prevSquare = getSquareForCell(x, prevY);
					if (square != null && prevSquare._indexBitmap == square._indexBitmap) {
						if (dstY != prevY) {
							moveSquare(prevSquare, dstY * NUM_CELLS + x);
							moveSquare(square, prevSquare._cellDst);
						} else {
							moveSquare(square, prevSquare._cellSrc);
							prevSquare._cellDst = prevSquare._cellSrc;
						}
						movements++;
						prevY = findNotEmptySquareByXY(x, y + aidy, aidy, 0);
					} else {
						if (prevY != dstY) moveSquare(prevSquare, dstY * NUM_CELLS + x);
						prevY = findNotEmptySquareByXY(x, y, aidy, 0);
					}
					dstY += aidy;
					y = prevY + aidy;
				}
			}
		}
		return movements > 0;
	}

	private int getMovements(int direction) {
		int al_direction = (direction + 2) % 4, prevX, prevY;
		int sx = _start_indices[direction][0], sy = _start_indices[direction][1];
		int aidx = _inner_iterators[al_direction][0], aidy = _inner_iterators[al_direction][1];
		int odx = _outer_iterators[al_direction][0], ody = _outer_iterators[al_direction][1];
		Square square, prevSquare;
		int movements = 0;

		if ((direction & 1) == 0) {
			for (int y = sy; checkNumInGrid(y); y += ody) {
				prevX = findNotEmptySquareByXY(sx, y, aidx, 1);
				if (!checkNumInGrid(prevX)) continue;
				for (int x = prevX + aidx, dstX = sx; checkNumInGrid(prevX);) {
					x = findNotEmptySquareByXY(x, y, aidx, 1);
					square = getSquareForCell(x, y);
					prevSquare = getSquareForCell(prevX, y);
					if (square != null && prevSquare._indexBitmap == square._indexBitmap) {
						movements++;
						prevX = findNotEmptySquareByXY(x + aidx, y, aidx, 1);
					} else {
						if (prevX != dstX) movements++;
						prevX = findNotEmptySquareByXY(x, y, aidx, 1);
					}
					dstX += aidx;
					x = prevX + aidx;
				}
			}
		} else {
			for (int x = sx; checkNumInGrid(x); x += odx) {
				prevY = findNotEmptySquareByXY(x, sy, aidy, 0);
				if (!checkNumInGrid(prevY)) continue;
				for (int y = prevY + aidy, dstY = sy; checkNumInGrid(prevY);) {
					y = findNotEmptySquareByXY(x, y, aidy, 0);
					square = getSquareForCell(x, y);
					prevSquare = getSquareForCell(x, prevY);
					if (square != null && prevSquare._indexBitmap == square._indexBitmap) {
						movements++;
						prevY = findNotEmptySquareByXY(x, y + aidy, aidy, 0);
					} else {
						if (prevY != dstY) movements++;
						prevY = findNotEmptySquareByXY(x, y, aidy, 0);
					}
					dstY += aidy;
					y = prevY + aidy;
				}
			}
		}
		return movements;
	}

	private boolean isPossibleToMove() {
		return (getMovements(MOVE_LEFT) + getMovements(MOVE_UP) + getMovements(MOVE_RIGHT) + getMovements(MOVE_DOWN)) > 0;
	}

	private void createNewSquare() {
		int[] emptyCells = new int[NUM_CELLS * NUM_CELLS];
		int countEmpty = 0;
		for (int k = 0; k < NUM_CELLS * NUM_CELLS; ++k) {
			if (_gameField[k] == null) emptyCells[countEmpty++] = k;
		}
		if (countEmpty == 0) startLose();
		else addNewSquare(emptyCells[_randomGen.nextInt(countEmpty)]);
	}
	
	private void checkMovedSquares() {
		Square square;
		boolean found = false;

		for (int k = 0; k < NUM_CELLS * NUM_CELLS; ++k) {
			square = _gameField[k];
			if (square != null && square._state == Square.STATE_MOVE) {
				found = true;
				break;
			}
		}
		if (_is_moving && !found) createNewSquare();
		_is_moving = found;
	}

	private int getDirection(int x, int y) {
		int dx = Math.abs(x - _touchX), dy = Math.abs(y - _touchY);
		if (y - _touchY > dx) return MOVE_DOWN;
		else if (-y + _touchY > dx) return MOVE_UP;
		else if (x - _touchX > dy) return MOVE_RIGHT;
		else return MOVE_LEFT;
	}

	public boolean onTouch(int x, int y, int evtType) {
		if (_gameState <= GAME_STATE_FIELD_APPEAR)
			return true;
		
		
		// check buttons press
		if (evtType == TOUCH_DOWN) {
			if (_rectButtonRestart.contains(x, y)) {
				gameRestart();
				return true;
			}
		}
		
		// check game field
		if (!_is_moving && _gameState == GAME_STATE_PLAY) {
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
					_is_moving = startMove(getDirection(x, y));
				}
				_touchState = 0;
			}
		}
		return true;
	}

	private void drawButton(Canvas canvas, RectF rectIn, String str, int color1, int color2, int alpha) {
		int 	scrW 	= canvas.getWidth();
		float	rectRad = scrW * 0.04f;
		float	rectBord = scrW * 0.005f;
		RectF rect = new RectF(rectIn);
		
		RectF rectInside = new RectF( rect.left + rectBord, rect.top + rectBord, rect.right - rectBord, rect.bottom - rectBord);

		int[] colors = {0, 0};
		colors[0] = color1 | (alpha << 24);
		colors[1] = color2 | (alpha << 24);
		LinearGradient shader = new LinearGradient(rect.left, rect.top, rect.left, rect.bottom, colors, null, Shader.TileMode.CLAMP);
		Paint paintInside = new Paint();
		paintInside.setAntiAlias(true);
		paintInside.setShader(shader);

		_paintRectButton.setColor(0xFFFFFF | (alpha<<24) );
		canvas.drawRoundRect(rect, rectRad, rectRad, _paintRectButton);
		_paintRectButton.setColor(0x808080 | (alpha<<24) );
		canvas.drawRoundRect(rectInside, rectRad, rectRad, paintInside);

		Rect rText = new Rect();
		_paintTextButton.getTextBounds(str, 0, str.length(), rText);
		float h = rText.height();
		float cx = rect.centerX();
		float cy = rect.centerY();
		_paintTextButton.setAlpha(alpha);
		canvas.drawText(str, cx, cy + h * 0.5f, _paintTextButton);
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

		float 	yc = _yFieldUp * 0.5f;
		float	btnW = 220 * _xScale;
		float	btnH =  60 * _yScale;
		float	h2 = btnH * 0.5f;

		_paintTextButton.setTextSize(_scrH * 0.02f);
		
		// outline painter
		_paintButtonOutline = new Paint();
		_paintButtonOutline.setStyle(Style.STROKE);
		_paintButtonOutline.setStrokeWidth(4.0f);
		_paintButtonOutline.setColor(0xaaaaaa);

		LinearGradient shader;
		// Top left button
		int[] colors = {0, 0};
		colors[0] = 0xFFAA88 | 0xFF000000;
		colors[1] = 0xAA4433 | 0xFF000000;
		shader = new LinearGradient(0.0f, yc - h2, 0.0f, yc + h2, colors, null, Shader.TileMode.REPEAT);
		_paintButtonLeft = new Paint();
		_paintButtonLeft.setAntiAlias(true);
		_paintButtonLeft.setShader(shader);
		_paintButtonLeft.setStyle(Style.FILL_AND_STROKE);
		_paintButtonLeft.setColor(0xaaaaaa);
		_paintButtonLeft.setAlpha(255);

		// Top right button
		shader = new LinearGradient(0.0f, yc - h2, 0.0f, yc + h2, colors, null, Shader.TileMode.CLAMP);
		_paintButtonRight = new Paint();
		_paintButtonRight.setAntiAlias(true);
		_paintButtonRight.setShader(shader);
		_paintButtonRight.setStyle(Style.FILL);
		_paintButtonRight.setStyle(Style.FILL_AND_STROKE);
		_paintButtonRight.setColor(0xaaaaaa);
		_paintButtonRight.setAlpha(255);

		// Low buttons
		_yButtonsLo = _scrH - (_scrH - _yFieldLo) * 0.5f;
	}
	
	private void drawBackground(Canvas canvas, int opacityBackground) {
		int	r, c, x, y;
		int xPad = (int)(3.0f * _xScale);
		int yPad = (int)(3.0f * _yScale);
		float dt;
		Bitmap bmp;
		
		_paintBitmap.setAlpha(opacityBackground);
		_paintLine.setAlpha(opacityBackground);
	
		// Draw back gradient

		_rectDst.set(0, 0, _scrW, _scrH);
		Paint paintInside = new Paint();
		paintInside.setAntiAlias(true);
		LinearGradient shaderRad = new LinearGradient(0, 0, 0, _scrH, _colors[_curColor - 1], _colors[_curColor], Shader.TileMode.MIRROR);
		dt = _timeCur - _timeBackStateStart;
		if (_backgroundState == BACKGROUND_STATE_SIT || dt > TIME_BACKGROUND_STATE_CHANGE) {
			_backgroundState = BACKGROUND_STATE_SIT;
			paintInside.setShader(shaderRad);
			canvas.drawRect(_rectDst, paintInside);
		} else {
			dt /= TIME_BACKGROUND_STATE_CHANGE;
			shaderRad = new LinearGradient(0, 0, 0, _scrH,
					new int[] {_colors[_curColor - 2], _colors[_curColor - 1], _colors[_curColor]},
					new float[] {0.0f, 1.0f - dt, 1.0f}, Shader.TileMode.MIRROR);
			paintInside.setShader(shaderRad);
			canvas.drawRect(_rectDst, paintInside);
		}

		// Draw background of the grid
		_rectDst.set(0, (int)_yFieldUp, _scrW, (int)_yFieldLo);
		_rectSrc.set(0, 0, _bitmapBack.getWidth(), _bitmapBack.getHeight() );
		_paintBitmap.setAlpha(opacityBackground);
		canvas.drawBitmap(_bitmapBack, _rectSrc, _rectDst, _paintBitmap);

		for (int k = 0; k < NUM_CELLS * NUM_CELLS; k++) {
			r = getRowByCell(k);
			c = getColByCell(k);
			x = (int) (c * _cellSide);
			y = (int) (r * _cellSide + _yFieldUp);

			bmp = _bitmapSquare[SQUARE_FIELD];

			_rectDst.set(x + xPad, y + yPad, (int)(x + _cellSide - xPad), (int)(y + _cellSide - yPad));
			_rectSrc.set(0, 0, bmp.getWidth(), bmp.getHeight());
			canvas.drawBitmap(bmp, _rectSrc, _rectDst, _paintBitmap);
		}

		
		// Prepare Restart button coordinates
		float	btnW = 280 * _xScale;
		float	btnH =  80 * _yScale;
		float	btnX = _scrW * 0.5f;
		float	btnY = _yButtonsLo;
		
		// render button Restart
		_rectButtonRestart.set(btnX - btnW * 0.5f, btnY - btnH * 0.5f, btnX + btnW * 0.5f, btnY + btnH * 0.5f);
		drawButton(canvas, _rectButtonRestart, _strRestart, 0x92DCFE, 0x1e80B0, opacityBackground);
		
		
		// ************** Top button Left ********************
		
		float 	yc = _yFieldUp * 0.5f;
		btnW = 220 * _xScale;
		btnH =  60 * _yScale;
		float	h2 = btnH * 0.5f;

		_paintButtonOutline.setAlpha(opacityBackground);
		_paintButtonLeft.setAlpha(opacityBackground);
		_paintButtonRight.setAlpha(opacityBackground);
		
		float	rectRad = _scrW * 0.04f;
		RectF rBtn = new RectF();
		rBtn.set(-rectRad, yc - h2, btnW, yc + h2);
		canvas.drawRoundRect(rBtn, rectRad , rectRad , _paintButtonLeft);
		canvas.drawRoundRect(rBtn, rectRad , rectRad , _paintButtonOutline);

		String str = _strScore + ": " + _gameScore;
		Rect rText = new Rect();
		_paintTextButton.getTextBounds(str, 0, str.length(), rText);
		float h = rText.height();
		float xc = btnW * 0.5f;
		_paintTextButton.setAlpha(opacityBackground);
		canvas.drawText(str, xc, yc + h * 0.5f, _paintTextButton);

		// *************************************
		// Top button Right 
		
		rBtn.set(_scrW - 1 - btnW, yc - h2, _scrW + rectRad, yc + h2);
		canvas.drawRoundRect(rBtn, rectRad , rectRad , _paintButtonRight);
		canvas.drawRoundRect(rBtn, rectRad , rectRad , _paintButtonOutline);
		
		str = _strBestScore + ": " + _gameBestScore;
		rText = new Rect();
		_paintTextButton.getTextBounds(str, 0, str.length(), rText);
		h = rText.height();
		xc = _scrW - 1 - btnW * 0.5f;
		_paintTextButton.setAlpha(opacityBackground);
		canvas.drawText(str, xc, yc + h * 0.5f, _paintTextButton);
	}

	private void drawBitmap(Canvas canvas, Bitmap bmp, int ldst, int tdst, int rdst, int ddst) {
		_rectDst.set(ldst, tdst, rdst, ddst);
		_rectSrc.set(0, 0, bmp.getWidth(), bmp.getHeight());
		canvas.drawBitmap(bmp, _rectSrc, _rectDst, _paintBitmap);
	}

	private void drawAppearSquare(Canvas canvas, int k, int xPad, int yPad) {
		Square square = _gameField[k];
		float t = (float)(_timeCur - square._timeStart) / (square._timeEnd - square._timeStart);
		if (t < 1.0f) {
			int r = getRowByCell(k), c = getColByCell(k);
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

	private void drawSitSquare(Canvas canvas, int k, int xPad, int yPad) {
		int r = getRowByCell(k), c = getColByCell(k);
		int x = (int)(c * _cellSide), y = (int)(r * _cellSide + _yFieldUp);
		drawBitmap(canvas, _bitmapSquare[_gameField[k]._indexBitmap],
				x + xPad, y + yPad,
				(int)(x + _cellSide - xPad), (int)(y + _cellSide - yPad));
	}

	private void drawMoveSquare(Canvas canvas, int k, int xPad, int yPad) {
		Square square = _gameField[k];
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
			Square squareDst = _gameField[indexDst];

			if (squareDst == null) {
				square._cellSrc = indexDst;
				square._state = Square.STATE_SIT;
				_gameField[indexSrc] = null;
				_gameField[indexDst] = square;
				drawSitSquare(canvas, indexDst, xPad, yPad);
			} else if (squareDst._cellDst == indexDst) {
				_gameField[indexSrc] = null;
				squareDst._state = Square.STATE_PULSE;
				square._timeStart = _timeCur;
				squareDst._timeEnd = _timeCur + TIME_SQUARE_PULSE;
				squareDst._indexBitmap++;
				if (squareDst._indexBitmap > _curColor && _backgroundState == BACKGROUND_STATE_SIT) {
					_backgroundState = BACKGROUND_STATE_CHANGE;
					_timeBackStateStart = _timeCur;
					_curColor = squareDst._indexBitmap;
					if (_curColor == SQUARE_1024) startWin();
				}
				_gameScore += (int) Math.pow(2 * square._indexBitmap, 2);
			}
		}
	}

	private void drawPulseSquare(Canvas canvas, int k, int xPad, int yPad) {
		Square square = _gameField[k];
		float t = (float)(_timeCur - square._timeStart) / (square._timeEnd - square._timeStart);
		if (t < 1.0f) {
			int r = getRowByCell(k), c = getColByCell(k);
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
			if (_gameField[k] == null) continue;
			switch (_gameField[k]._state) {
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
	
	public void onDraw(Canvas canvas) {
		long dt;
		int	opacityBackground;

		_timeCur = (int)(System.currentTimeMillis() & 0x3fffffff);
		if (_timePrev < 0) _timePrev = _timeCur;
		_timePrev = _timeCur;
		
		if (_timeStateStart < 0) _timeStateStart = _timeCur;
		
		// change state
		opacityBackground = 255;
		if (_gameState == GAME_STATE_FIELD_APPEAR)
		{
			dt = _timeCur - _timeStateStart;
			if (dt > TIME_GAME_STATE_APPEAR) {
				_gameState++;
				_timeStateStart = -1;
			} else {
				// calculate background opacity
				opacityBackground = ((int)dt * 255) / (TIME_GAME_STATE_APPEAR - 20);
				if (opacityBackground > 255)
					opacityBackground = 255;
			}
		}
		
		if (_scrW < 0) prepareScreenValues(canvas);
		
		// Render game parts
		drawBackground(canvas, opacityBackground);

		if (_gameState > GAME_STATE_FIELD_APPEAR) {
			drawSquares(canvas);
			if (_gameState == GAME_STATE_PLAY) {
				checkMovedSquares();
				if (!isPossibleToMove()) startLose();
			} else if (_gameState == GAME_STATE_LOSE_APPEAR) {
				_gameBestScore = Math.max(_gameBestScore, _gameScore);
				Toast.makeText(_app, "You lose!!!", Toast.LENGTH_LONG).show();
				_gameState = GAME_STATE_LOSE;
			} else if (_gameState == GAME_STATE_WIN_APPEAR) {
				_gameBestScore = Math.max(_gameBestScore, _gameScore);
				Toast.makeText(_app, "You win!!!", Toast.LENGTH_LONG).show();
				_gameState = GAME_STATE_WIN;
			}
		}
	} // onDraw method
}
