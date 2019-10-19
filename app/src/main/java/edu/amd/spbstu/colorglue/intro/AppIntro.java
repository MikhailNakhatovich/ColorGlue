package edu.amd.spbstu.colorglue.intro;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;

import edu.amd.spbstu.colorglue.ActivityMain;
import edu.amd.spbstu.colorglue.R;

import static edu.amd.spbstu.colorglue.Constants.*;


public class AppIntro {
    private static final int APP_STATE_START = 0;
    private static final int APP_STATE_CIRCLE_INC = 1;
    private static final int APP_STATE_APPLE_2ND_RADIUS	= 2;
    private static final int APP_STATE_APPLE_FILL_OPA = 3;
    private static final int APP_STATE_APPLE_FILL_SHADER = 4;
    private static final int APP_STATE_GRAFT = 5;
    private static final int APP_STATE_LEAF = 6;
    private static final int APP_STATE_FINISHED = 10;

    // parameters for animation
    private static final int TIME_CIRCLE_INC = 300;
    private static final int TIME_APPLE_INC = 300;
    private static final int TIME_SHADER_COLORED = 300;
    // should be power of 2
    private static final int TIME_LEAF = 256;
    private static final int NUM_SEG_APPLE = 60;

    // DATA
    private long _prevTime;

    private ActivityMain _ctx;
    private int _appState;
    private int _timeState;

    private Path _pathAppleOutline;
    private Paint _paintGreenEmpty;
    private Paint _paintGreenFill;
    private Paint _paintGraftFill;
    private Paint _paintLeafFill;
    private Paint _paintTextWhite;
    private Paint _paintTextYell;
    private Paint _paintBitmap;
    private Path _pathAppleGraft;
    private Path _pathAppleLeaf;

    private Paint _paintRectButton;
    private Paint _paintTextButton;

    private String _strDepth;
    private String _strUniversity1;
    private String _strUniversity2;
    private String _strWeb;
    private String _strStart;
    private String _strAmdUrl;

    // Apple body parameters
    private int _scrW, _scrH;
    private int _scrCenterX, _scrCenterY;
    private float _appleRadiusBase;
    private V2d _point;

    // Buttons rects
    private RectF _rectBtnStart;
    private RectF _rectBtnWeb;

    public AppIntro(ActivityMain ctx) {
        _ctx = ctx;
        _prevTime = -1;

        _appState = APP_STATE_START;

        _pathAppleOutline = new Path();
        _pathAppleGraft = new Path();
        _pathAppleLeaf = new Path();

        _point = new V2d();
        _paintGreenEmpty = new Paint();
        _paintGreenEmpty.setStyle(Style.STROKE);
        _paintGreenEmpty.setColor(0xFF207020);
        _paintGreenEmpty.setAntiAlias(true);
        _paintGreenEmpty.setStrokeWidth(3.0f);

        _paintGreenFill = new Paint();
        _paintGreenFill.setStyle(Style.FILL_AND_STROKE);
        _paintGreenFill.setColor(0xFF207020);
        _paintGreenFill.setAntiAlias(true);
        _paintGreenFill.setStrokeWidth(3.0f);
        _paintGreenFill.setAlpha(255);

        _paintGraftFill = new Paint();
        _paintGraftFill.setStyle(Style.FILL);
        _paintGraftFill.setColor(0xFF905000);
        _paintGraftFill.setAntiAlias(true);

        _paintLeafFill = new Paint();
        _paintLeafFill.setStyle(Style.FILL);
        _paintLeafFill.setColor(0xFF3aa142);
        _paintLeafFill.setAntiAlias(true);

        _paintTextWhite = new Paint();
        _paintTextWhite.setColor(0xFFFFFFFF);
        _paintTextWhite.setAntiAlias(true);
        _paintTextWhite.setStyle(Style.FILL);
        _paintTextWhite.setTextSize(24.0f);
        _paintTextWhite.setTextAlign(Align.CENTER);

        _paintBitmap = new Paint();
        _paintBitmap.setColor(0xFFFFFFFF);
        _paintBitmap.setStyle(Style.FILL);

        _paintTextYell = new Paint();
        _paintTextYell.setColor(0xFFFFFF00);
        _paintTextYell.setAntiAlias(true);
        _paintTextYell.setStyle(Style.FILL);
        _paintTextYell.setTextSize(14.0f);
        _paintTextYell.setTextAlign(Align.CENTER);

        _paintTextButton = new Paint();
        _paintTextButton.setColor(0xFF000088);
        _paintTextButton.setStyle(Style.FILL);
        _paintTextButton.setTextSize(20.0f);
        _paintTextButton.setTextAlign(Align.CENTER);
        _paintTextButton.setAntiAlias(true);

        _paintRectButton = new Paint();
        _paintRectButton.setStyle(Style.FILL);
        _paintRectButton.setAntiAlias(true);

        _rectBtnStart = new RectF();
        _rectBtnWeb = new RectF();

        Resources res = ctx.getResources();
        _strDepth = res.getString(R.string.str_depth);
        _strUniversity1 = res.getString(R.string.str_university1);
        _strUniversity2 = res.getString(R.string.str_university2);
        _strWeb = res.getString(R.string.str_toweb);
        _strStart = res.getString(R.string.str_start);
        _strAmdUrl = res.getString(R.string.str_amd_url);

        if ((TIME_LEAF & (TIME_LEAF - 1)) != 0) {
            Log.d(LOG_TAG, "!!!! Constant parameter TIME_LEAF is ont power of 2 !!!");
        }
    }

    void drawCanvas(Canvas canvas) {
        long curTime = System.currentTimeMillis();
        if (_prevTime == -1) _prevTime = curTime;
        int deltaTimeMs = (int)(curTime - _prevTime);
        _prevTime = curTime;
        if (deltaTimeMs > 300) deltaTimeMs = 300;

        if (_appState == APP_STATE_START) {
            initDrawCircle(canvas);
            _appState = APP_STATE_CIRCLE_INC;
        }

        switch (_appState) {
            case APP_STATE_CIRCLE_INC:
                drawCircleInc(canvas, deltaTimeMs);
                break;
            case APP_STATE_APPLE_2ND_RADIUS:
                drawAppleEmptyInc(canvas, deltaTimeMs);
                break;
            case APP_STATE_APPLE_FILL_OPA:
                drawAppleFillOpacity(canvas, deltaTimeMs);
                break;
            case APP_STATE_APPLE_FILL_SHADER:
                drawAppleFillShader(canvas, deltaTimeMs);
                break;
            case APP_STATE_GRAFT:
                drawAppleGraft(canvas, deltaTimeMs);
                break;
            case APP_STATE_LEAF:
                drawAppleLeaf(canvas, deltaTimeMs);
                break;
            default:
                break;
        }
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) _ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected();
    }


    // *****************************************************
    // Individual state draws
    // *****************************************************

    // t in [0..1]
    private void getCardioid(float t, float cx, float cy, float radiusBase, float radiusApple, V2d pointOut) {
        double phi = Math.PI * 2.0 * t;
        pointOut.x = (int)(cx + radiusBase * Math.sin(phi) + radiusApple * (Math.sin(phi) - Math.sin(2.0f * phi)));
        pointOut.y = (int)(cy - radiusBase * Math.cos(phi) - radiusApple * (Math.cos(phi) - Math.cos(2.0f * phi)));
    }

    private void acceptNewScreen(Canvas canvas) {
        _scrW = canvas.getWidth();
        _scrH = canvas.getHeight();

        _scrCenterX = _scrW >> 1;
        _scrCenterY = _scrH >> 1;
        int dimMin = (_scrW < _scrH)? _scrW : _scrH;
        int dimMax = (_scrW > _scrH)? _scrW : _scrH;

        _appleRadiusBase = (float)dimMin * 0.09f;
        _paintTextButton.setTextSize(dimMax * 0.02f);

        float textSize = _scrH * 0.03f;
        if (textSize < 20.0f)
            textSize = 20.0f;
        if (textSize > 40.0f)
            textSize = 40.0f;
        float ts = 32.0f;
        //TODO: check text size
        _paintTextWhite.setTextSize(ts);
        _paintTextYell.setTextSize(ts * 0.8f);
    }

    private void initDrawCircle(Canvas canvas) {
        acceptNewScreen(canvas);
        _timeState = 0;
    }

    private void drawButton(Canvas canvas, RectF rectIn, String str, int color1, int color2, int alpha) {
        int scrW = canvas.getWidth();
        float rectRad = scrW * 0.04f;
        float rectBord = scrW * 0.005f;
        RectF rect = new RectF(rectIn);

        RectF rectInside = new RectF(rect.left + rectBord, rect.top + rectBord, rect.right - rectBord, rect.bottom - rectBord);

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
    } // func

    private void updateState(int deltaTimeMs, int timeState) {
        _timeState += deltaTimeMs;
        if (_timeState > timeState) {
            _timeState = 0;
            _appState = _appState + 1;
        }
    }

    private void createPath(float radiusBase, float radiusApple) {
        _pathAppleOutline.reset();
        float t = 0.0f, tStep = 1.0f / NUM_SEG_APPLE;
        for (int a = 0; a < NUM_SEG_APPLE; a ++) {
            getCardioid(t, _scrCenterX, _scrCenterY, radiusBase, radiusApple, _point);
            t += tStep;
            if (a == 0) {
                _pathAppleOutline.moveTo(_point.x, _point.y);
            } else {
                _pathAppleOutline.lineTo(_point.x, _point.y);
            }
        }
        _pathAppleOutline.close();
    }

    private void drawCircleInc(Canvas canvas, int deltaTimeMs) {
        int r, g, b;
        r = g = b = 0;
        canvas.drawRGB(r, g, b);

        float rAnim = (float) _timeState / TIME_CIRCLE_INC;
        if (rAnim > 1.0f) rAnim = 1.0f;
        float radiusBase = 5.0f * (1.0f - rAnim) + _appleRadiusBase * rAnim;

        createPath(radiusBase, 0.0f);
        // draw path
        canvas.drawPath(_pathAppleOutline, _paintGreenEmpty);

        updateState(deltaTimeMs, TIME_CIRCLE_INC);
    } // func

    private void drawAppleEmptyInc(Canvas canvas, int deltaTimeMs) {
        canvas.drawRGB(0, 0, 0);

        // Setup apple shape (outline)
        float rAnim = (float) _timeState / TIME_APPLE_INC;
        if (rAnim > 1.0f) rAnim = 1.0f;
        float radiusBase = _appleRadiusBase;
        float radiusApple = 0.0f * (1.0f - rAnim) + radiusBase * rAnim;

        createPath(radiusBase, radiusApple);
        // draw path
        canvas.drawPath(_pathAppleOutline, _paintGreenEmpty);

        updateState(deltaTimeMs, TIME_APPLE_INC);
    } // func

    private void drawAppleFillOpacity(Canvas canvas, int deltaTimeMs) {
        canvas.drawRGB(0, 0, 0);

        // Setup apple shape (outline)
        float rAnim = (float) _timeState / TIME_APPLE_INC;
        if (rAnim > 1.0f) rAnim = 1.0f;
        float radiusBase = _appleRadiusBase;
        int	opa = (int)(rAnim * 255.0f);

        createPath(radiusBase, radiusBase);
        _paintGreenFill.setAlpha(opa);

        // draw path
        canvas.drawPath(_pathAppleOutline, _paintGreenFill);
        canvas.drawPath(_pathAppleOutline, _paintGreenEmpty);

        updateState(deltaTimeMs, TIME_APPLE_INC);
    } // func

    private void drawAppleFillShader(Canvas canvas, int deltaTimeMs) {
        canvas.drawRGB(0, 0, 0);

        // Setup apple shape (outline)
        float rAnim = (float) _timeState / TIME_SHADER_COLORED;
        if (rAnim > 1.0f) rAnim = 1.0f;
        float radiusBase = _appleRadiusBase;
        int	opa = 255;

        createPath(radiusBase, radiusBase);
        _paintGreenFill.setAlpha(opa);

        int[] colors = new int[2];
        float xSpot, ySpot, radGrad;

        int r = (int)(0x20 * (1.0f - rAnim) + 0xAA * rAnim);
        int g = (int)(0x70 * (1.0f - rAnim) + 0xFF * rAnim);
        int b = (int)(0x20 * (1.0f - rAnim) + 0xAA * rAnim);

        colors[0] = 0xFF000000 | (r<<16) | (g<<8) | b;
        colors[1] = 0xFF207020;

        xSpot = _scrCenterX + _appleRadiusBase * 0.5f;
        ySpot = _scrCenterY + _appleRadiusBase * 0.8f;
        radGrad = _appleRadiusBase * 2.5f;
        RadialGradient gradientRadial = new RadialGradient(xSpot, ySpot, radGrad, colors, null, Shader.TileMode.CLAMP);
        _paintGreenFill.setShader(gradientRadial);

        // draw path
        canvas.drawPath(_pathAppleOutline, _paintGreenFill);

        updateState(deltaTimeMs, TIME_SHADER_COLORED);
    } // func

    private void drawAppleGraft(Canvas canvas, int deltaTimeMs) {
        canvas.drawRGB(0, 0, 0);

        // Setup apple shape (outline)
        float rAnim = (float) _timeState / TIME_SHADER_COLORED;
        if (rAnim > 1.0f) rAnim = 1.0f;
        float radiusBase = _appleRadiusBase;
        int	opa = 255;

        createPath(radiusBase, radiusBase);
        _paintGreenFill.setAlpha(opa);

        // Path for graft
        float xLo, yLo, xHi, yHi, xMi, yMi;
        float X_RATIO = 0.2f;
        float Y_RATIO = 0.5f;

        xLo = _scrCenterX;
        yLo = _scrCenterY - _appleRadiusBase;
        xHi = xLo + _appleRadiusBase * 0.8f * rAnim;
        yHi = yLo - _appleRadiusBase * 1.6f * rAnim;
        xMi = xLo * (1.0f - X_RATIO) + xHi * X_RATIO;
        yMi = yLo * (1.0f - Y_RATIO) + yHi * Y_RATIO;

        _pathAppleGraft.reset();
        _pathAppleGraft.moveTo(xLo,  yLo);
        _pathAppleGraft.quadTo(xMi,  yMi, xHi, yHi);

        xHi = xLo + _appleRadiusBase * 1.0f * rAnim;
        yHi = yLo - _appleRadiusBase * 1.4f * rAnim;
        xMi = xLo * (1.0f - X_RATIO) + xHi * X_RATIO;
        yMi = yLo * (1.0f - Y_RATIO) + yHi * Y_RATIO;
        _pathAppleGraft.lineTo(xHi,  yHi);
        _pathAppleGraft.quadTo(xMi, yMi, xLo, yLo);
        _pathAppleGraft.close();

        int[] colors = new int[2];
        float	xSpot, ySpot, radGrad;

        colors[0] = 0xFFAAFFAA;
        colors[1] = 0xFF207020;

        xSpot = _scrCenterX + _appleRadiusBase * 0.5f;
        ySpot = _scrCenterY + _appleRadiusBase * 0.8f;
        radGrad	= _appleRadiusBase * 2.5f;
        RadialGradient gradientRadial = new RadialGradient(xSpot, ySpot, radGrad, colors, null, Shader.TileMode.CLAMP);
        _paintGreenFill.setShader(gradientRadial);

        // draw path
        canvas.drawPath(_pathAppleOutline, _paintGreenFill);
        canvas.drawPath(_pathAppleGraft, _paintGraftFill);

        updateState(deltaTimeMs, TIME_SHADER_COLORED);
    } // func

    private void drawAppleLeaf(Canvas canvas, int deltaTimeMs)
    {
        canvas.drawRGB(0, 0, 0);

        // Setup apple shape (outline)
        int tAnim = _timeState & (TIME_LEAF - 1);
        int	animPhase = _timeState / TIME_LEAF;
        if ( animPhase == 0) {
            // increase phase
            tAnim = tAnim * 256 / TIME_LEAF;
        } else {
            tAnim = 255;
        }
        int tText;
        if (animPhase == 0) {
            tText = 0;
        } else if (animPhase == 1) {
            tText = (_timeState - TIME_LEAF) * 255 / TIME_LEAF;
        } else {
            tText = 255;
        }
        _paintLeafFill.setAlpha(tAnim);
        _paintTextWhite.setAlpha(tText);
        _paintTextYell.setAlpha(tText);

        float radiusBase = _appleRadiusBase;
        int	opa = 255;

        createPath(radiusBase, radiusBase);
        _paintGreenFill.setAlpha(opa);

        // Path for graft
        float xLo, yLo, xHi, yHi, xMi, yMi;
        float X_RATIO = 0.2f;
        float Y_RATIO = 0.5f;

        xLo = _scrCenterX;
        yLo = _scrCenterY - _appleRadiusBase;
        xHi = xLo + _appleRadiusBase * 0.8f;
        yHi = yLo - _appleRadiusBase * 1.6f;
        xMi = xLo * (1.0f - X_RATIO) + xHi * X_RATIO;
        yMi = yLo * (1.0f - Y_RATIO) + yHi * Y_RATIO;

        _pathAppleGraft.reset();
        _pathAppleGraft.moveTo(xLo,  yLo);
        _pathAppleGraft.quadTo(xMi,  yMi, xHi, yHi);

        xHi = xLo + _appleRadiusBase * 1.0f;
        yHi = yLo - _appleRadiusBase * 1.4f;
        xMi = xLo * (1.0f - X_RATIO) + xHi * X_RATIO;
        yMi = yLo * (1.0f - Y_RATIO) + yHi * Y_RATIO;
        _pathAppleGraft.lineTo(xHi,  yHi);
        _pathAppleGraft.quadTo(xMi, yMi, xLo, yLo);
        _pathAppleGraft.close();

        // internal green filler as radial gradient
        int[] colors = new int[2];
        float	xSpot, ySpot, radGrad;

        colors[0] = 0xFFAAFFAA;
        colors[1] = 0xFF207020;

        xSpot = _scrCenterX + _appleRadiusBase * 0.5f;
        ySpot = _scrCenterY + _appleRadiusBase * 0.8f;
        radGrad	= _appleRadiusBase * 2.5f;
        RadialGradient gradientRadial = new RadialGradient(xSpot, ySpot, radGrad, colors, null, Shader.TileMode.CLAMP);
        _paintGreenFill.setShader(gradientRadial);

        // path for leaf
        float xL, yL, xR, yR, xU0, yU0, xU1, yU1, xD0, yD0, xD1, yD1;

        float LeafLen = _appleRadiusBase * 1.35f;
        xR = yR = 0;
        xL = - LeafLen;
        yL = 0;
        float radR = LeafLen * 0.45f;
        float radL = LeafLen * 0.4f;

        float xLeafSource = _scrCenterX + _appleRadiusBase * 0.32f;
        float yLeafSource = _scrCenterY - _appleRadiusBase - _appleRadiusBase * 0.90f;
        xU0 = -(float) Math.cos(80 * 3.1415f / 180.0f) * radR;
        yU0 = -(float) Math.sin(80 * 3.1415f / 180.0f) * radR;
        xD0 = -(float) Math.cos(80 * 3.1415f / 180.0f) * radR;
        yD0 = +(float) Math.sin(80 * 3.1415f / 180.0f) * radR;
        xU1 = xL + (float) Math.cos(40 * 3.1415f / 180.0f) * radL;
        yU1 = yL - (float) Math.sin(40 * 3.1415f / 180.0f) * radL;
        xD1 = xL + (float) Math.cos(15 * 3.1415f / 180.0f) * radL;
        yD1 = yL + (float) Math.sin(15 * 3.1415f / 180.0f) * radL;

        // Rotate leaf
        float aCos = (float) Math.cos(-20.0f * 3.1415f / 180.0f);
        float aSin = (float) Math.sin(-20.0f * 3.1415f / 180.0f);
        float xx, yy;

        xx = xL * aCos + yL * aSin;
        yy =-xL * aSin + yL * aCos;
        xL = xx; yL = yy;
        xx = xR * aCos + yR * aSin;
        yy =-xR * aSin + yR * aCos;
        xR = xx; yR = yy;
        xx = xU0 * aCos + yU0 * aSin;
        yy =-xU0 * aSin + yU0 * aCos;
        xU0 = xx; yU0 = yy;
        xx = xU1 * aCos + yU1 * aSin;
        yy =-xU1 * aSin + yU1 * aCos;
        xU1 = xx; yU1 = yy;
        xx = xD0 * aCos + yD0 * aSin;
        yy =-xD0 * aSin + yD0 * aCos;
        xD0 = xx; yD0 = yy;
        xx = xD1 * aCos + yD1 * aSin;
        yy =-xD1 * aSin + yD1 * aCos;
        xD1 = xx; yD1 = yy;

        // Translate leaf
        xL  += xLeafSource; yL  += yLeafSource;
        xR  += xLeafSource; yR  += yLeafSource;
        xU0 += xLeafSource; yU0 += yLeafSource;
        xD0 += xLeafSource; yD0 += yLeafSource;
        xU1 += xLeafSource; yU1 += yLeafSource;
        xD1 += xLeafSource; yD1 += yLeafSource;

        _pathAppleLeaf.reset();
        _pathAppleLeaf.moveTo(xR,  yR);
        _pathAppleLeaf.cubicTo(xU0,  yU0, xU1, yU1, xL, yL);
        _pathAppleLeaf.cubicTo(xD1, yD1, xD0, yD0, xR, yR);
        _pathAppleLeaf.close();

        // draw path
        canvas.drawPath(_pathAppleOutline, _paintGreenFill);
        canvas.drawPath(_pathAppleGraft, _paintGraftFill);
        canvas.drawPath(_pathAppleLeaf, _paintLeafFill);

        if (_timeState > TIME_LEAF) {
            // draw titles
            Rect r = new Rect();

            _paintTextWhite.getTextBounds(_strDepth, 0, _strDepth.length(), r);
            float h = r.height();
            float vOff = 0.0f;
            //if (_scrH > _scrW)
            vOff = h;
            canvas.drawText(_strDepth, 	 0, _strDepth.length(), _scrCenterX, vOff + h , _paintTextWhite);
            canvas.drawText(_strUniversity1,0, _strUniversity1.length(), _scrCenterX, vOff + h * 2.5f, _paintTextYell);
            canvas.drawText(_strUniversity2,0, _strUniversity2.length(), _scrCenterX, vOff + h * 2.5f + h, _paintTextYell);
        }
        if ((_timeState > 2 * TIME_LEAF)) {
            opa = 255;
            if (_timeState < 3 * TIME_LEAF) opa = (_timeState - 2 * TIME_LEAF) * 256 / TIME_LEAF;
            _paintBitmap.setAlpha(opa);
            _paintTextWhite.setAlpha(opa);

            final boolean hasWeb = isConnectedToInternet();
            final float BUTTON_SCALE = 4.1f;
            final float BUTTON_W_H_RATIO = 0.3f;

            int bw = (int)(_appleRadiusBase * BUTTON_SCALE);
            int bh = (int)(bw * BUTTON_W_H_RATIO);
            int bwHalf = (bw >> 1);
            if (_scrH > _scrW) {
                // vertical buttons layout
                _rectBtnStart.set(_scrCenterX - bwHalf, _scrH - bh * 2, _scrCenterX + bwHalf, _scrH - bh);
                _rectBtnWeb.set(  _scrCenterX - bwHalf, _scrH - bh * 4, _scrCenterX + bwHalf, _scrH - bh * 3);
            } else {
                // horizontal buttons layout
                _rectBtnStart.set(_scrCenterX - bwHalf, _scrH - bh, _scrCenterX + bwHalf, _scrH);
                _rectBtnWeb.set(  -1000, -1000, -500, -500);
                if (hasWeb) {
                    _rectBtnStart.set(  _scrCenterX - bw, _scrH - bh, _scrCenterX, _scrH);
                    _rectBtnWeb.set(_scrCenterX, _scrH - bh, _scrCenterX + bw, _scrH);
                    _rectBtnStart.offset( - bh * 0.5f, 0.0f);
                    _rectBtnWeb.offset( + bh * 0.5f, 0.0f);
                }
            }
            drawButton(canvas, _rectBtnStart, _strStart, 0x92DCFE, 0x1e80B0, opa);
            if (hasWeb) {
                drawButton(canvas, _rectBtnWeb, _strWeb, 0x92DCFE, 0x1e80B0, opa);
            }
        }

        // update state
        _timeState += deltaTimeMs;
    } // func

    boolean onTouch(int x, int y, int touchType) {
        if (touchType != TOUCH_DOWN)
            return false;

        if (_rectBtnStart.contains(x,  y)) {
            _ctx.setView(VIEW_GAME);
            return false;
        }

        if (isConnectedToInternet() && _rectBtnWeb.contains(x,  y)) {
            // go to web
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(_strAmdUrl));
            _ctx.startActivity(browserIntent);
            return false;
        }

        _ctx.setView(VIEW_GAME);
        return true;
    } // onTouch
}
