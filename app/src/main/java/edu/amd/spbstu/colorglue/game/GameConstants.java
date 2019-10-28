package edu.amd.spbstu.colorglue.game;

class GameConstants {
    static final int NUM_CELLS = 4, NUM_CELLS2 = 16;

    static final int MOVE_LEFT = 0;
    static final int MOVE_UP = 1;
    static final int MOVE_RIGHT = 2;
    static final int MOVE_DOWN = 3;
    static final int MOVE_START = MOVE_LEFT, MOVE_END = 4;

    static final int SQUARE_FIELD = 0;
    static final int SQUARE_2 = 1;
    static final int SQUARE_4 = 2;
    static final int SQUARE_8 = 3;
    static final int SQUARE_16 = 4;
    static final int SQUARE_32 = 5;
    static final int SQUARE_64 = 6;
    static final int SQUARE_128 = 7;
    static final int SQUARE_256 = 8;
    static final int SQUARE_512 = 9;
    static final int SQUARE_1024 = 10;
    static final int SQUARE_COUNT = 11;
    static final int SQUARE_WIN = SQUARE_1024;

    static int getScore(int index) {
        return 2 * (int) Math.pow(2, index);
    }
}
