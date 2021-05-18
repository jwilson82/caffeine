package com.jrw.chess.caffeine.core;

import lombok.experimental.UtilityClass;

import static com.jrw.chess.caffeine.core.Color.*;
import static com.jrw.chess.caffeine.core.Direction.*;
import static com.jrw.chess.caffeine.core.File.*;
import static com.jrw.chess.caffeine.core.Square.*;

@UtilityClass
public class Bitboard {
  private final long NOT_A_FILE = ~ofFile(A);
  private final long NOT_H_FILE = ~ofFile(H);
  private final long[][] BETWEEN = new long[64][64];
  private final long[][] RAY = new long[64][64];

  public long ofColor(final int color) {
    return (color == LIGHT) ? 0x55AA55AA55AA55AAL : 0XAA55AA55AA55AA55L;
  }

  public long ofFile(final int file) {
    return 0x0101010101010101L << file;
  }

  public long ofRank(final int rank) {
    return 0x00000000000000FFL << (rank << 3);
  }

  public long ofSquare(final int square) {
    return (square == NO_SQUARE) ? 0L : 1L << square;
  }

  public long between(final int source, final int target) {
    return BETWEEN[source][target];
  }

  public long ray(final int source, final int target) {
    return RAY[source][target];
  }

  public boolean contains(final long bitboard, final int square) {
    return (bitboard & ofSquare(square)) != 0L;
  }

  public int peek(final long bitboard) {
    return Long.numberOfTrailingZeros(bitboard);
  }

  public long pop(final long bitboard) {
    return bitboard ^ Long.lowestOneBit(bitboard);
  }

  public long shift(long bitboard, final int direction) {
    bitboard = (direction > 0) ? bitboard << direction : bitboard >>> -direction;

    return switch (direction) {
      case EAST, NORTHEAST, SOUTHEAST -> bitboard & NOT_A_FILE;
      case WEST, NORTHWEST, SOUTHWEST -> bitboard & NOT_H_FILE;
      default -> bitboard;
    };
  }

  static {
    int[] directions = {NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST};

    for (int source = A1; source <= H8; source++) {
      for (int direction : directions) {
        for (long targets = shift(fill(ofSquare(source), direction), direction);
            targets != 0L;
            targets = pop(targets)) {
          int target = peek(targets);

          RAY[source][target] = shift(fill(ofSquare(source), direction), direction);
          BETWEEN[source][target] =
              RAY[source][target] & shift(fill(ofSquare(target), -direction), -direction);
        }
      }
    }
  }

  private long fill(long bitboard, final int direction) {
    for (int i = 0; i < 7; i++) {
      bitboard |= shift(bitboard, direction);
    }
    return bitboard;
  }
}
