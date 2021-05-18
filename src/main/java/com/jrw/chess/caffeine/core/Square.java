package com.jrw.chess.caffeine.core;

import lombok.experimental.UtilityClass;

import static java.lang.Math.abs;

@UtilityClass
public class Square {
  public final int A1 = 0, B1 = 1, C1 = 2, D1 = 3, E1 = 4, F1 = 5, G1 = 6, H1 = 7;
  public final int A2 = 8, B2 = 9, C2 = 10, D2 = 11, E2 = 12, F2 = 13, G2 = 14, H2 = 15;
  public final int A3 = 16, B3 = 17, C3 = 18, D3 = 19, E3 = 20, F3 = 21, G3 = 22, H3 = 23;
  public final int A4 = 24, B4 = 25, C4 = 26, D4 = 27, E4 = 28, F4 = 29, G4 = 30, H4 = 31;
  public final int A5 = 32, B5 = 33, C5 = 34, D5 = 35, E5 = 36, F5 = 37, G5 = 38, H5 = 39;
  public final int A6 = 40, B6 = 41, C6 = 42, D6 = 43, E6 = 44, F6 = 45, G6 = 46, H6 = 47;
  public final int A7 = 48, B7 = 49, C7 = 50, D7 = 51, E7 = 52, F7 = 53, G7 = 54, H7 = 55;
  public final int A8 = 56, B8 = 57, C8 = 58, D8 = 59, E8 = 60, F8 = 61, G8 = 62, H8 = 63;
  public final int NO_SQUARE = 64;

  public int at(final int file, final int rank) {
    return rank * 8 + file;
  }

  public int fileDistance(final int source, final int target) {
    return abs(file(source) - file(target));
  }

  public int rankDistance(final int source, final int target) {
    return abs(rank(source) - rank(target));
  }

  private int file(final int square) {
    return square % 8;
  }

  private int rank(final int square) {
    return square / 8;
  }

  public final String string(final int square) {
    if (square == NO_SQUARE) return "-";

    return String.format("%s%s", File.string(file(square)), Rank.string(rank(square)));
  }

  public int parse(final String s) {
    for (int square = A1; square <= NO_SQUARE; square++) {
      if (string(square).equals(s)) return square;
    }
    throw new IllegalArgumentException("Cannot parse square - " + s);
  }
}
