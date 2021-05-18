package com.jrw.chess.caffeine.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Piece {
  public final int PAWN = 0;
  public final int KNIGHT = 1;
  public final int BISHOP = 2;
  public final int ROOK = 3;
  public final int QUEEN = 4;
  public final int KING = 5;
  public final int NO_PIECE = 6;

  public String string(final int piece) {
    return switch (piece) {
      case PAWN -> "p";
      case KNIGHT -> "n";
      case BISHOP -> "b";
      case ROOK -> "r";
      case QUEEN -> "q";
      case KING -> "k";
      default -> "";
    };
  }
}
