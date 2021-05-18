package com.jrw.chess.caffeine.core;

import lombok.experimental.UtilityClass;

import static com.jrw.chess.caffeine.core.Direction.*;

@UtilityClass
public class Side {
    public final int WHITE = 0;
    public final int BLACK = 1;

    public int enemy(final int side) {
        return 1 - side;
    }

    public int forward(final int side) {
        return (side == WHITE) ? NORTH : SOUTH;
    }

    public String string(final int side) {
        return (side == WHITE) ? "w" : "b";
    }

    public int parse(final String s) {
        return switch (s) {
            case "w" -> WHITE;
            case "b" -> BLACK;
            default -> throw new IllegalArgumentException("Cannot parse side - " + s);
        };
    }
}
