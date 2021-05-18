package com.jrw.chess.caffeine.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Direction {
    public final int NORTH = 8;
    public final int SOUTH = -NORTH;
    public final int EAST = 1;
    public final int WEST = -EAST;
    public final int NORTHEAST = NORTH + EAST;
    public final int NORTHWEST = NORTH + WEST;
    public final int SOUTHEAST = SOUTH + EAST;
    public final int SOUTHWEST = SOUTH + WEST;
}
