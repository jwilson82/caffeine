package com.jrw.chess.caffeine.search;

import lombok.experimental.UtilityClass;

import static com.jrw.chess.caffeine.core.Piece.*;

@UtilityClass
public class Move {
    public final int NO_MOVE = 0;

    private final int SOURCE_MASK = 0b111111;
    private final int TARGET_MASK = 0b111111;
    private final int PROMOTION_MASK = 0b111;
    private final int SOURCE_SHIFT = 0;
    private final int TARGET_SHIFT = SOURCE_SHIFT + Integer.bitCount(SOURCE_MASK);
    private final int PROMOTION_SHIFT = TARGET_SHIFT + Integer.bitCount(TARGET_MASK);

    public int create(final int source, final int target) {
        return create(source, target, NO_PIECE);
    }

    public int create(final int source, final int target, final int promotion) {
        return (source << SOURCE_SHIFT) | (target << TARGET_SHIFT) | (promotion << PROMOTION_SHIFT);
    }

    public int source(final int move) {
        return (move >>> SOURCE_SHIFT) & SOURCE_MASK;
    }

    public int target(final int move) {
        return (move >>> TARGET_SHIFT) & TARGET_MASK;
    }

    public int promotion(final int move) {
        return (move >>> PROMOTION_SHIFT) & PROMOTION_MASK;
    }
}
