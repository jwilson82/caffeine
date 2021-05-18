package com.jrw.chess.caffeine.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Rank {
    public final int ONE = 0;
    public final int TWO = 1;
    public final int THREE = 2;
    public final int FOUR = 3;
    public final int FIVE = 4;
    public final int SIX = 5;
    public final int SEVEN = 6;
    public final int EIGHT = 7;

    public String string(final int rank) {
        return Integer.toString(1 + rank);
    }
}
