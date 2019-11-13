package edu.amd.spbstu.colorglue.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.Arrays;

import edu.amd.spbstu.colorglue.ActivityMain;
import edu.amd.spbstu.colorglue.R;

import static edu.amd.spbstu.colorglue.Constants.*;
import static edu.amd.spbstu.colorglue.game.GameConstants.*;


public class ViewTutorial extends ViewGameBase {
	private static final int TIME_DELAY_TO_NEXT_STATE = TIME_BACKGROUND_STATE_CHANGE;

	private static final int TUTORIAL_STATE_BEGIN = 0;
	private static final int TUTORIAL_STATE_SWIPE = TUTORIAL_STATE_BEGIN + 1;
	private static final int TUTORIAL_STATE_SWIPE_COLOR = TUTORIAL_STATE_SWIPE + 1;
	private static final int TUTORIAL_STATE_NEW_COLOR = TUTORIAL_STATE_SWIPE_COLOR + 1;
	private static final int TUTORIAL_STATE_PROGRESS = TUTORIAL_STATE_NEW_COLOR + 1;
	private static final int TUTORIAL_STATE_NEW_COLOR_COMBO_TEXT = TUTORIAL_STATE_PROGRESS + 1;
	private static final int TUTORIAL_STATE_NEW_COLOR_COMBO = TUTORIAL_STATE_NEW_COLOR_COMBO_TEXT + 1;
	private static final int TUTORIAL_STATE_SCORE_TEXT = TUTORIAL_STATE_NEW_COLOR_COMBO + 1;
	private static final int TUTORIAL_STATE_SCORE = TUTORIAL_STATE_SCORE_TEXT + 1;
	private static final int TUTORIAL_STATE_LOSE = TUTORIAL_STATE_SCORE + 1;
	private static final int TUTORIAL_STATE_WIN = TUTORIAL_STATE_LOSE + 1;
	private static final int TUTORIAL_STATE_AI_TEXT = TUTORIAL_STATE_WIN + 1;
	private static final int TUTORIAL_STATE_AI = TUTORIAL_STATE_AI_TEXT + 1;
	private static final int TUTORIAL_STATE_END = TUTORIAL_STATE_AI + 1;
	private static final int TUTORIAL_STATE_COUNT = TUTORIAL_STATE_END + 1;

	private static final int[] _textStates = new int[] {TUTORIAL_STATE_BEGIN,
			TUTORIAL_STATE_SWIPE_COLOR, TUTORIAL_STATE_NEW_COLOR_COMBO_TEXT,
			TUTORIAL_STATE_SCORE_TEXT, TUTORIAL_STATE_LOSE, TUTORIAL_STATE_WIN,
			TUTORIAL_STATE_AI_TEXT, TUTORIAL_STATE_END};

	private static final int NEED_SCORE = 32;

	private int _tutorialState;

	private int _timeDelayState;

	private String[] _strTutorialTexts;

	private TextView _tvTutorialText = null;
	private ViewGroup _vg;

	private boolean _isReadyState;

	public ViewTutorial(ActivityMain app) {
		super(app);

		_strTutorialTexts = new String[TUTORIAL_STATE_COUNT];
		_strTutorialTexts[TUTORIAL_STATE_BEGIN] = app.getString(R.string.str_tutorial_begin, _strRestart);
		_strTutorialTexts[TUTORIAL_STATE_SWIPE] = app.getString(R.string.str_tutorial_swipe);
		_strTutorialTexts[TUTORIAL_STATE_SWIPE_COLOR] = app.getString(R.string.str_tutorial_swipe_color);
		_strTutorialTexts[TUTORIAL_STATE_NEW_COLOR] = app.getString(R.string.str_tutorial_new_color);
		_strTutorialTexts[TUTORIAL_STATE_NEW_COLOR_COMBO_TEXT] = app.getString(R.string.str_tutorial_new_color_combo_text);
		_strTutorialTexts[TUTORIAL_STATE_NEW_COLOR_COMBO] = app.getString(R.string.str_tutorial_new_color_combo);
		_strTutorialTexts[TUTORIAL_STATE_SCORE_TEXT] = app.getString(R.string.str_tutorial_score_text);
		_strTutorialTexts[TUTORIAL_STATE_SCORE] = app.getString(R.string.str_tutorial_score, NEED_SCORE);
		_strTutorialTexts[TUTORIAL_STATE_PROGRESS] = app.getString(R.string.str_tutorial_progress);
		_strTutorialTexts[TUTORIAL_STATE_LOSE] = app.getString(R.string.str_tutorial_lose);
		_strTutorialTexts[TUTORIAL_STATE_WIN] = app.getString(R.string.str_tutorial_win);
		_strTutorialTexts[TUTORIAL_STATE_AI_TEXT] = app.getString(R.string.str_tutorial_ai_text, _strRestart);
		_strTutorialTexts[TUTORIAL_STATE_AI] = app.getString(R.string.str_tutorial_ai);
		_strTutorialTexts[TUTORIAL_STATE_END] = app.getString(R.string.str_tutorial_end);

		_tvTutorialText = new TextView(_app);
		_tvTutorialText.setTextColor(0xFFFFFFFF);
		_tvTutorialText.setText(_strTutorialTexts[_tutorialState]);
		_tvTutorialText.setGravity(Gravity.CENTER_HORIZONTAL);
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (_gameState == GAME_STATE_FIELD_APPEAR) return;
		drawTutorialText(canvas);
		checkReadyTask();
		checkDelayToTheNextState();
	}

	public boolean onTouch(int x, int y, int evtType) {
		if (_gameState <= GAME_STATE_FIELD_APPEAR)
			return true;

		// check restart press
		if (onTouchRestart(x, y, evtType)) return true;

		if (onTouchSimpleTap(x, y, evtType)) return true;

		// check tutorial for ai
		if (_tutorialState == TUTORIAL_STATE_AI) {
			onTouchTopBar(x, y, evtType);
			return true;
		}

		if (!_isReadyState && _tutorialState == TUTORIAL_STATE_NEW_COLOR_COMBO) {
			if (evtType == TOUCH_MOVE && getDirection(x, y) != MOVE_LEFT) {
				_touchState = 0;
			} else {
				onTouchMoving(x, y, evtType);
			}
			return true;
		}

		// check game field
		if (!_isReadyState && onTouchMoving(x, y, evtType)) return true;
		return true;
	}

	protected int getOpacityBackground(int time) {
		long dt = _timeCur - _timeStateStart;
		int	opacityBackground = 255;
		if (dt > time) {
			_gameState++;
			_timeStateStart = -1;
			if (_gameState == GAME_STATE_PLAY) {
				_tvTutorialText.setPadding(0, (_scrH - _scrW) / 2, 0, 0);
				_vg.addView(_tvTutorialText, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			}
		} else {
			// calculate background opacity
			opacityBackground = ((int)dt * 255) / time;
			if (opacityBackground > 255)
				opacityBackground = 255;
		}
		return opacityBackground;
	}

	protected void restart() {
		super.restart();

		_tutorialState = TUTORIAL_STATE_BEGIN;
		_timeDelayState = 0;
		_isReadyState = false;

		if (_tvTutorialText != null) {
			_vg.removeView(_tvTutorialText);
			_tvTutorialText.setText(_strTutorialTexts[_tutorialState]);
		}
	}

	protected void prepareScreenValues(Canvas canvas) {
		super.prepareScreenValues(canvas);
		_tvTutorialText.setTextSize(_scrH * 0.014f);
		_vg = (ViewGroup) _app.getWindow().getDecorView().getRootView();
	}

	private void drawTutorialText(Canvas canvas) {
		_rectDst.set(0, 0, _scrW, _scrH);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xB0000000);
		canvas.drawRect(_rectDst, paint);

		drawRestartButton(canvas, 255);

		switch (_tutorialState) {
			case TUTORIAL_STATE_SWIPE:
			case TUTORIAL_STATE_NEW_COLOR:
			case TUTORIAL_STATE_LOSE:
				drawSquares(canvas);
				break;
			case TUTORIAL_STATE_SCORE_TEXT:
				drawScores(canvas, 255);
				break;
			case TUTORIAL_STATE_SCORE:
				drawSquares(canvas);
				drawScores(canvas, 255);
				break;
			case TUTORIAL_STATE_AI_TEXT:
				drawProgressBar(canvas, 255);
				break;
			case TUTORIAL_STATE_PROGRESS:
			case TUTORIAL_STATE_NEW_COLOR_COMBO:
			case TUTORIAL_STATE_WIN:
			case TUTORIAL_STATE_AI:
				drawSquares(canvas);
				drawProgressBar(canvas, 255);
				break;
			default:
				break;
		}
	}

	private boolean onTouchSimpleTap(int x, int y, int evtType) {
		if (evtType == TOUCH_DOWN) {
			if (_tutorialState == TUTORIAL_STATE_END) {
				_vg.removeView(_tvTutorialText);
				_app.endTutorial();
				return true;
			} else if (Arrays.binarySearch(_textStates, _tutorialState) >= 0) {
				increaseState();
				return true;
			}
		}
		return false;
	}

	private void increaseState() {
		_vg.removeView(_tvTutorialText);
		_tutorialState++;
		_isReadyState = false;
		_gameField.clear();
		_gameScore = 0;
		_gameState = GAME_STATE_PLAY;

		int top;

		switch (_tutorialState) {
			case TUTORIAL_STATE_SWIPE:
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, NUM_CELLS + 1);
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, 2 * NUM_CELLS + 2);

				top = (_scrH - _scrW) / 2;
				break;
			case TUTORIAL_STATE_NEW_COLOR:
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, 0);
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, NUM_CELLS2 - 1);
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, NUM_CELLS - 1);
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, NUM_CELLS2 - NUM_CELLS);

				top = (int) ((_scrH - _scrW) * 0.75f);
				break;
			case TUTORIAL_STATE_SCORE_TEXT:
				top = (int) (_yFieldUp * 0.5f + 80 * 0.75f * _yScale);
				break;
			case TUTORIAL_STATE_SCORE:
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, NUM_CELLS - 1);
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, NUM_CELLS2 - NUM_CELLS);

				top = (int) (_yFieldUp * 0.5f + 80 * 0.75f * _yScale);
				break;
			case TUTORIAL_STATE_PROGRESS:
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, 0);
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR, NUM_CELLS2 - 1);

				top = (int) _yBarLo;
				break;
			case TUTORIAL_STATE_NEW_COLOR_COMBO:
				_gameField.curColor(SQUARE_8);
				for (int ind = 0; ind < NUM_CELLS; ++ind) {
					_gameField.addNewSquareIndex(_timeCur, TIME_SQUARE_APPEAR, ind, SQUARE_2);
				}
				_gameField.addNewSquareIndex(_timeCur, TIME_SQUARE_APPEAR, NUM_CELLS2 - NUM_CELLS, SQUARE_2);
				for (int k = SQUARE_2, ind = NUM_CELLS2 - NUM_CELLS + 1; ind < NUM_CELLS2; ++ind, ++k) {
					_gameField.addNewSquareIndex(_timeCur, TIME_SQUARE_APPEAR, ind, k);
				}

				top = (int) ((_scrH - _scrW) * 0.8f);
				break;
			case TUTORIAL_STATE_LOSE:
				setLoseCombination4();

				top = (int) (_yBarLo * 0.5f);
				break;
			case TUTORIAL_STATE_WIN:
				setWinCombination4();

				top = (int) _yBarLo;
				break;
			case TUTORIAL_STATE_AI_TEXT:
				top = (int) _yBarLo;
				break;
			case TUTORIAL_STATE_AI:
				_cntMoves = 0;
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR);
				_gameField.addNewSquare(_timeCur, TIME_SQUARE_APPEAR);
				startAIThread();

				top = (int) _yBarLo;
				break;
			default:
				top = (_scrH - _scrW) / 2;
				break;
		}

		_tvTutorialText.setText(_strTutorialTexts[_tutorialState]);
		_tvTutorialText.setPadding(0, top, 0, 0);
		_vg.addView(_tvTutorialText, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	private void checkReadyTask() {
		if (_isReadyState) return;
		if (Arrays.binarySearch(_textStates, _tutorialState) >= 0) {
			_isReadyState = false;
			return;
		}
		switch (_tutorialState) {
			case TUTORIAL_STATE_SWIPE:
				if (_gameField.getSquare(NUM_CELLS + 1) == null
						|| _gameField.getSquare(2 * NUM_CELLS + 2) == null) {
					setReadyState();
				}
				break;
			case TUTORIAL_STATE_NEW_COLOR:
				if (_gameField.curColor() == SQUARE_4) setReadyState();
				break;
			case TUTORIAL_STATE_SCORE:
				if (_gameScore > NEED_SCORE) setReadyState();
				break;
			case TUTORIAL_STATE_PROGRESS:
				if (_gameField.curColor() >= SQUARE_8) setReadyState();
				break;
			case TUTORIAL_STATE_NEW_COLOR_COMBO:
				if (_gameField.curColor() >= SQUARE_16) setReadyState();
				break;
			case TUTORIAL_STATE_AI:
				if (_gameField.curColor() >= SQUARE_16) {
					stopAIThread();
					setReadyState();
				}
				break;
			default:
				break;
		}
	}

	private void setReadyState() {
		_isReadyState = true;
		_timeDelayState = _timeCur;
	}

	private void checkDelayToTheNextState() {
		if (_isReadyState && _timeCur - _timeDelayState >= TIME_DELAY_TO_NEXT_STATE) {
			increaseState();
		}
	}

	private void setLoseCombination4() {
		_gameScore = 1900;
		_gameField.curColor(SQUARE_128);
		_gameState = GAME_STATE_LOSE;
		_gameField.addNewSquareIndex(0, SQUARE_2);
		_gameField.addNewSquareIndex(1, SQUARE_4);
		_gameField.addNewSquareIndex(2, SQUARE_8);
		_gameField.addNewSquareIndex(3, SQUARE_4);
		_gameField.addNewSquareIndex(4, SQUARE_4);
		_gameField.addNewSquareIndex(5, SQUARE_8);
		_gameField.addNewSquareIndex(6, SQUARE_32);
		_gameField.addNewSquareIndex(7, SQUARE_2);
		_gameField.addNewSquareIndex(8, SQUARE_8);
		_gameField.addNewSquareIndex(9, SQUARE_32);
		_gameField.addNewSquareIndex(10, SQUARE_128);
		_gameField.addNewSquareIndex(11, SQUARE_16);
		_gameField.addNewSquareIndex(12, SQUARE_32);
		_gameField.addNewSquareIndex(13, SQUARE_64);
		_gameField.addNewSquareIndex(14, SQUARE_2);
		_gameField.addNewSquareIndex(15, SQUARE_64);
	}

	private void setWinCombination4() {
		_gameScore = 9284;
		_gameField.curColor(SQUARE_1024);
		_gameState = GAME_STATE_WIN;
		_gameField.addNewSquareIndex(0, SQUARE_2);
		_gameField.addNewSquareIndex(1, SQUARE_16);
		_gameField.addNewSquareIndex(2, SQUARE_2);
		_gameField.addNewSquareIndex(4, SQUARE_1024);
		_gameField.addNewSquareIndex(5, SQUARE_4);
		_gameField.addNewSquareIndex(8, SQUARE_8);
	}
}
