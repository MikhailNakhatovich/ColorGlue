package edu.amd.spbstu.colorglue.game;

import static edu.amd.spbstu.colorglue.game.GameConstants.*;


class AI {
    private static final int COUNT_RUNS = 60;

    static int getBest(Field field) {
        float bestScore = 0, score;
        int bestMove = MOVE_LEFT;

        for (int i = MOVE_START; i < MOVE_END; i++) {
            score = multiRandomRun(field, i);

            if (score >= bestScore) {
                bestScore = score;
                bestMove = i;
            }
        }

        return bestMove;
    }

    private static float multiRandomRun(Field field, int direction) {
        float total = 0.0f;
        int res;

        for (int i = 0; i < COUNT_RUNS; i++) {
            res = randomRun(field, direction);
            if (res == -1) return -1;
            total += res;
        }

        return total / COUNT_RUNS;
    }

    private static int randomRun(Field field, int direction) {
        Field newField = new Field(field);
        int score = 0;
        int[] res = moveAndAddSquare(newField, direction);
        if (res[0] == 0) return -1;

        score += res[1];
        while (newField.isPossibleToMove()) {
            res = moveAndAddSquare(newField, (int)Math.floor(Math.random() * MOVE_END));
            if (res[0] == 0) break;
            score += res[1];
        }
        return score;
    }

    private static int[] moveAndAddSquare(Field field, int direction) {
        int[] res = field.moveMultiple(direction);
        if (res[0] > 0) field.addNewSquare();
        return res;
    }
}
