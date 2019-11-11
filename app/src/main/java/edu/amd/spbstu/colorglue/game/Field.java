package edu.amd.spbstu.colorglue.game;

import java.util.Arrays;
import java.util.Random;

import static edu.amd.spbstu.colorglue.game.GameConstants.*;


class Field {
    private static final int[][] _start_indices = {{0, 0}, {0, 0}, {NUM_CELLS - 1, 0}, {0, NUM_CELLS - 1}};
    private static final int[][] _outer_iterators = {{0, 1}, {1, 0}, {0, 1}, {1, 0}};

    private static final int[][] _directions = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};

    private Square[] _field;
    private Random _randomGen;

    Field() {
        _field = new Square[NUM_CELLS2];
        _randomGen = new Random();
    }

    Field(Field field) {
        _field = new Square[NUM_CELLS2];
        for (int k = 0; k < NUM_CELLS2; ++k) {
            if (field._field[k] != null) _field[k] = new Square(field._field[k]);
        }
        _randomGen = field._randomGen;
    }

    Square getSquare(int cellIndex) {
        if (checkBound2(cellIndex)) return _field[cellIndex];
        return null;
    }

    Square getSquare(int x, int y) {
        if (checkBound(x) && checkBound(y)) return _field[y * NUM_CELLS + x];
        return null;
    }

    boolean isPossibleToMove() {
        return checkEmptyCells() || checkMatches();
    }

    boolean addNewSquare(int timeCur, int timeAppear) {
        int[] emptyCells = getEmptyCells();
        if (emptyCells.length == 0) return false;
        int k = emptyCells[_randomGen.nextInt(emptyCells.length)];
        _field[k] = new Square();
        _field[k]._cellSrc = k;
        _field[k]._cellDst = -1;
        _field[k]._indexBitmap = SQUARE_2;
        _field[k]._state = Square.STATE_APPEAR;
        _field[k]._timeStart = timeCur;
        _field[k]._timeEnd = timeCur + timeAppear;
        return true;
    }

    void addNewSquare(int timeCur, int timeAppear, int cellIndex) {
        if (!checkBound2(cellIndex)) return;
        _field[cellIndex] = new Square();
        _field[cellIndex]._cellSrc = cellIndex;
        _field[cellIndex]._cellDst = -1;
        _field[cellIndex]._indexBitmap = SQUARE_2;
        _field[cellIndex]._state = Square.STATE_APPEAR;
        _field[cellIndex]._timeStart = timeCur;
        _field[cellIndex]._timeEnd = timeCur + timeAppear;
    }

    void addNewSquare() {
        int[] emptyCells = getEmptyCells();
        if (emptyCells.length == 0) return;
        int k = emptyCells[_randomGen.nextInt(emptyCells.length)];
        _field[k] = new Square();
        _field[k]._cellSrc = k;
        _field[k]._cellDst = -1;
        _field[k]._indexBitmap = SQUARE_2;
        _field[k]._state = Square.STATE_SIT;
    }

    void addNewSquareIndex(int cellIndex, int index) {
        if (!checkBound2(cellIndex) || index < SQUARE_2 || index > SQUARE_WIN) return;
        _field[cellIndex] = new Square();
        _field[cellIndex]._cellSrc = cellIndex;
        _field[cellIndex]._cellDst = -1;
        _field[cellIndex]._indexBitmap = index;
        _field[cellIndex]._state = Square.STATE_SIT;
    }

    void addNewSquareIndex(int timeCur, int timeAppear, int cellIndex, int index) {
        if (!checkBound2(cellIndex) || index < SQUARE_2 || index > SQUARE_WIN) return;
        _field[cellIndex] = new Square();
        _field[cellIndex]._cellSrc = cellIndex;
        _field[cellIndex]._cellDst = -1;
        _field[cellIndex]._indexBitmap = index;
        _field[cellIndex]._state = Square.STATE_APPEAR;
        _field[cellIndex]._timeStart = timeCur;
        _field[cellIndex]._timeEnd = timeCur + timeAppear;
    }

    void insertSquare(Square square) {
        _field[square._cellSrc] = square;
    }

    void removeSquare(int cellIndex) {
        _field[cellIndex] = null;
    }

    void clear() {
        for (int i = 0; i < NUM_CELLS2; ++i) {
            _field[i] = null;
        }
    }

    boolean checkMoveCells() {
        for (int k = 0; k < NUM_CELLS2; ++k) {
            if (_field[k] != null && _field[k]._state == Square.STATE_MOVE) return true;
        }
        return false;
    }

    boolean startMove(int direction, int timeCur, int timeMove) {
        int al_direction = (direction + 2) % 4, prevX, prevY;
        int sx = _start_indices[direction][0], sy = _start_indices[direction][1];
        int aidx = _directions[al_direction][0], aidy = _directions[al_direction][1];
        int odx = _outer_iterators[al_direction][0], ody = _outer_iterators[al_direction][1];
        Square square, prevSquare;
        int movements = 0;

        if ((direction & 1) == 0) {
            for (int y = sy; checkBound(y); y += ody) {
                prevX = findNotEmptySquareByXY(sx, y, al_direction)[0];
                if (!checkBound(prevX)) continue;
                for (int x = prevX + aidx, dstX = sx; checkBound(prevX);) {
                    x = findNotEmptySquareByXY(x, y, al_direction)[0];
                    square = getSquare(x, y);
                    prevSquare = getSquare(prevX, y);
                    if (square != null && prevSquare._indexBitmap == square._indexBitmap) {
                        if (dstX != prevX) {
                            startMoveSquare(prevSquare, y * NUM_CELLS + dstX, timeCur, timeMove);
                            startMoveSquare(square, prevSquare._cellDst, timeCur, timeMove);
                        } else {
                            startMoveSquare(square, prevSquare._cellSrc, timeCur, timeMove);
                            prevSquare._cellDst = prevSquare._cellSrc;
                        }
                        movements++;
                        prevX = findNotEmptySquareByXY(x + aidx, y, al_direction)[0];
                    } else {
                        if (prevX != dstX) {
                            startMoveSquare(prevSquare, y * NUM_CELLS + dstX, timeCur, timeMove);
                            movements++;
                        }
                        prevX = findNotEmptySquareByXY(x, y, al_direction)[0];
                    }
                    dstX += aidx;
                    x = prevX + aidx;
                }
            }
        } else {
            for (int x = sx; checkBound(x); x += odx) {
                prevY = findNotEmptySquareByXY(x, sy, al_direction)[1];
                if (!checkBound(prevY)) continue;
                for (int y = prevY + aidy, dstY = sy; checkBound(prevY);) {
                    y = findNotEmptySquareByXY(x, y, al_direction)[1];
                    square = getSquare(x, y);
                    prevSquare = getSquare(x, prevY);
                    if (square != null && prevSquare._indexBitmap == square._indexBitmap) {
                        if (dstY != prevY) {
                            startMoveSquare(prevSquare, dstY * NUM_CELLS + x, timeCur, timeMove);
                            startMoveSquare(square, prevSquare._cellDst, timeCur, timeMove);
                        } else {
                            startMoveSquare(square, prevSquare._cellSrc, timeCur, timeMove);
                            prevSquare._cellDst = prevSquare._cellSrc;
                        }
                        movements++;
                        prevY = findNotEmptySquareByXY(x, y + aidy, al_direction)[1];
                    } else {
                        if (prevY != dstY) {
                            startMoveSquare(prevSquare, dstY * NUM_CELLS + x, timeCur, timeMove);
                            movements++;
                        }
                        prevY = findNotEmptySquareByXY(x, y, al_direction)[1];
                    }
                    dstY += aidy;
                    y = prevY + aidy;
                }
            }
        }
        return movements > 0;
    }

    int[] moveMultiple(int direction) {
        int[] res = move(direction), newRes;
        int isMoved = res[0];
        for (int i = 0; i < MOVE_TIMES - 1 && isMoved != 0; ++i) {
            newRes = move(direction);
            isMoved = newRes[0];
            res[1] += newRes[1];
        }
        return res;
    }

    int[] move(int direction) {
        int al_direction = (direction + 2) % 4, prevX, prevY;
        int sx = _start_indices[direction][0], sy = _start_indices[direction][1];
        int aidx = _directions[al_direction][0], aidy = _directions[al_direction][1];
        int odx = _outer_iterators[al_direction][0], ody = _outer_iterators[al_direction][1];
        Square square, prevSquare;
        int movements = 0, score = 0;

        if ((direction & 1) == 0) {
            for (int y = sy; checkBound(y); y += ody) {
                prevX = findNotEmptySquareByXY(sx, y, al_direction)[0];
                if (!checkBound(prevX)) continue;
                for (int x = prevX + aidx, dstX = sx; checkBound(prevX);) {
                    x = findNotEmptySquareByXY(x, y, al_direction)[0];
                    square = getSquare(x, y);
                    prevSquare = getSquare(prevX, y);
                    if (square != null && prevSquare._indexBitmap == square._indexBitmap) {
                        score += getScore(square._indexBitmap);
                        if (dstX != prevX) {
                            moveSquare(prevSquare, y * NUM_CELLS + dstX);
                            moveSquare(square, y * NUM_CELLS + dstX);
                        } else {
                            moveSquare(square, prevSquare._cellSrc);
                            prevSquare._cellDst = prevSquare._cellSrc;
                        }
                        movements++;
                        prevX = findNotEmptySquareByXY(x + aidx, y, al_direction)[0];
                    } else {
                        if (prevX != dstX) {
                            moveSquare(prevSquare, y * NUM_CELLS + dstX);
                            movements++;
                        }
                        prevX = findNotEmptySquareByXY(x, y, al_direction)[0];
                    }
                    dstX += aidx;
                    x = prevX + aidx;
                }
            }
        } else {
            for (int x = sx; checkBound(x); x += odx) {
                prevY = findNotEmptySquareByXY(x, sy, al_direction)[1];
                if (!checkBound(prevY)) continue;
                for (int y = prevY + aidy, dstY = sy; checkBound(prevY);) {
                    y = findNotEmptySquareByXY(x, y, al_direction)[1];
                    square = getSquare(x, y);
                    prevSquare = getSquare(x, prevY);
                    if (square != null && prevSquare._indexBitmap == square._indexBitmap) {
                        score += getScore(square._indexBitmap);
                        if (dstY != prevY) {
                            moveSquare(prevSquare, dstY * NUM_CELLS + x);
                            moveSquare(square, dstY * NUM_CELLS + x);
                        } else {
                            moveSquare(square, prevSquare._cellSrc);
                            prevSquare._cellDst = prevSquare._cellSrc;
                        }
                        movements++;
                        prevY = findNotEmptySquareByXY(x, y + aidy, al_direction)[1];
                    } else {
                        if (prevY != dstY) {
                            moveSquare(prevSquare, dstY * NUM_CELLS + x);
                            movements++;
                        }
                        prevY = findNotEmptySquareByXY(x, y, al_direction)[1];
                    }
                    dstY += aidy;
                    y = prevY + aidy;
                }
            }
        }
        return new int[] {movements > 0 ? 1 : 0, score};
    }

    private void startMoveSquare(Square square, int cellDst, int timeCur, int timeMove) {
        square._cellDst = cellDst;
        square._state = Square.STATE_MOVE;
        square._timeStart = timeCur;
        square._timeEnd = timeCur + timeMove;
    }

    private void moveSquare(Square square, int cellDst) {
        _field[square._cellSrc] = null;
        if (_field[cellDst] == null) {
            square._cellSrc = cellDst;
            _field[cellDst] = square;
        } else {
            _field[cellDst]._indexBitmap++;
        }
    }

    private int[] findNotEmptySquareByXY(int x, int y, int direction) {
        int[] dir = _directions[direction];
        while (checkBound(y) && checkBound(x) && getSquare(x, y) == null) {
            x = x + dir[0];
            y = y + dir[1];
        }
        return new int[] {x, y};
    }

    private boolean checkEmptyCells() {
        for (int k = 0; k < NUM_CELLS2; ++k) {
            if (_field[k] == null) return true;
        }
        return false;
    }

    private int[] getEmptyCells() {
        int[] cells = new int[NUM_CELLS2];
        int countEmpty = 0;
        for (int k = 0; k < NUM_CELLS2; ++k) {
            if (_field[k] == null) cells[countEmpty++] = k;
        }
        return Arrays.copyOf(cells, countEmpty);
    }

    private boolean checkMatches() {
        Square square, otherSquare;
        int x, y, direction;
        int[] dir;

        for (x = 0; x < NUM_CELLS; x++) {
            for (y = 0; y < NUM_CELLS; y++) {
                square = getSquare(x, y);
                if (square != null) {
                    for (direction = MOVE_START; direction < MOVE_END; direction++) {
                        dir = _directions[direction];
                        otherSquare = getSquare(x + dir[0], y + dir[1]);
                        if (otherSquare != null && otherSquare._indexBitmap == square._indexBitmap) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    static int getRowByCell(int cellIndex) {
        return cellIndex / NUM_CELLS;
    }

    static int getColByCell(int cellIndex) {
        int r = cellIndex / NUM_CELLS;
        int c;
        c = +cellIndex - r * NUM_CELLS;
        if (c < 0)
            c += NUM_CELLS;
        return c;
    }

    private static boolean checkBound(int n) {
        return n >= 0 && n < NUM_CELLS;
    }

    private static boolean checkBound2(int n) {
        return n >= 0 && n < NUM_CELLS2;
    }
}
