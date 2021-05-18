package com.jrw.chess.caffeine.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class File {
    public final int A = 0;
    public final int H = 7;

    public String string(final int file) {
        return Character.toString((char) ('a' + file));
    }
}
